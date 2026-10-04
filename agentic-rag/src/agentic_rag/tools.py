"""Agent 可以调用的工具（语义检索以外的那几个）。

为什么需要它们：
    向量检索返回的是**被切碎的片段**，这决定了它做不到两件事——
      1. 按条文号取出**完整**的条文原文（片段可能在半途被截断）
      2. 字面精确匹配（对条文编号、专有名词这类内容天然不敏感）
    所以需要能在**原始文档**上工作的工具。

为什么在本地做而不是每种匹配做一个 Java 接口：
    原始文本通过一个接口（/api/knowledge/doc/{id}/text）取回后缓存在本地，
    条文切分与字面匹配都是纯文本处理。放在本地做，逻辑好改也好测；
    而把每种匹配方式都做成 Java 接口，会让检索逻辑散落在两个语言里。

    语料总量很小（三部法律合计两万余字），缓存一次即可。

设计约定：工具返回**结构化的 Match 列表**，而不是拼好的字符串。
    拼字符串会把"来源是哪份文档"这个信息丢掉，而评测平台判断
    "片段来自哪部法律"正需要它。
"""

from __future__ import annotations

import re
import time
from dataclasses import dataclass

from .clients import RagClient

ARTICLE_RE = re.compile(r"第[一二三四五六七八九十百零]+条")

# 语料文件名（不含扩展名）到法律全称
LAW_NAMES: dict[str, str] = {
    "pipl": "个人信息保护法",
    "dsl": "数据安全法",
    "csl": "网络安全法",
}

CACHE_TTL_SECONDS = 300

# 定义类条文的标记。
# 法律里的术语定义有固定写法（"本法下列用语的含义""……，是指……"），
# 识别出来能显著提升"查某个术语是什么意思"这类问题的命中质量。
DEFINITION_MARKERS = ("本法下列用语", "是指", "的含义是", "系指")


@dataclass(frozen=True, slots=True)
class Match:
    """一条命中：某部法律的某一条完整原文。"""

    law: str  # 法律代号，如 pipl
    law_title: str  # 法律全称
    article_no: str  # 条文号，如 第十三条
    body: str  # 条文正文（不含条文号）
    source: str  # MinIO 地址。评测平台靠它判断片段来自哪部法律，必须带上

    @property
    def label(self) -> str:
        return f"{self.law_title} {self.article_no}"

    def render(self) -> str:
        return f"【{self.label}】\n{self.article_no}{self.body}"


@dataclass(frozen=True, slots=True)
class Document:
    doc_id: int
    doc_name: str
    url: str
    law: str
    law_title: str
    text: str

    @property
    def articles(self) -> dict[str, str]:
        return split_articles(self.text)


def split_articles(text: str) -> dict[str, str]:
    """把法律全文切成 {条文号: 该条完整正文}。

    取的是**完整条文**而不是检索片段——这正是本工具存在的理由。
    """
    matches = list(ARTICLE_RE.finditer(text))
    if not matches:
        return {}
    articles: dict[str, str] = {}
    for index, match in enumerate(matches):
        start = match.end()
        end = matches[index + 1].start() if index + 1 < len(matches) else len(text)
        # 同名条文取首次出现，避开目录里的重复
        articles.setdefault(match.group(), text[start:end].strip())
    return articles


class CorpusCache:
    """按 knowledgeId 缓存知识库文档的原始文本。"""

    def __init__(self, ttl_seconds: int = CACHE_TTL_SECONDS) -> None:
        self._ttl = ttl_seconds
        self._cache: dict[int, tuple[float, list[Document]]] = {}

    async def documents(self, rag: RagClient, token: str, knowledge_id: int) -> list[Document]:
        cached = self._cache.get(knowledge_id)
        if cached and time.time() - cached[0] < self._ttl:
            return cached[1]

        listed = await rag.list_documents(knowledge_id, token)
        documents: list[Document] = []
        for item in listed:
            doc_id = item.get("id")
            doc_name = str(item.get("docName") or "")
            if doc_id is None or not doc_name:
                continue
            payload = await rag.read_document_text(int(doc_id), token)
            stem = doc_name.rsplit(".", 1)[0]
            documents.append(
                Document(
                    doc_id=int(doc_id),
                    doc_name=doc_name,
                    url=str(item.get("url") or ""),
                    law=stem,
                    law_title=LAW_NAMES.get(stem, stem),
                    text=str(payload.get("text") or ""),
                )
            )
        self._cache[knowledge_id] = (time.time(), documents)
        return documents


def pick(documents: list[Document], law: str | None) -> list[Document]:
    """按法律代号过滤；不指定或认不出时返回全部。

    认不出就返回全部是刻意的：宁可多查，也不要因为模型把代号写错就查了个空。
    """
    if not law:
        return documents
    key = law.strip().lower()
    selected = [d for d in documents if d.law.lower() == key or d.law_title == law.strip()]
    return selected or documents


def list_sources(documents: list[Document]) -> str:
    """列出知识库里有哪些法律，以及各自的条文规模。"""
    if not documents:
        return "知识库中没有文档。"
    lines = ["知识库中的文档："]
    for doc in documents:
        lines.append(
            f"- {doc.law_title}（代号 {doc.law}，{len(doc.articles)} 条，{len(doc.text)} 字）"
        )
    lines.append("\n提示：以上是知识库的**全部**内容。不在这里的内容一律无法回答。")
    return "\n".join(lines)


def read_article(
    documents: list[Document], article_no: str, law: str | None = None
) -> tuple[list[Match], str]:
    """按条文号取出**完整**原文。返回 (命中列表, 提示语)。"""
    target = article_no.strip()
    if not ARTICLE_RE.fullmatch(target):
        return [], f"条文号格式不对：{article_no!r}，应形如「第十三条」。"

    matches: list[Match] = []
    for doc in pick(documents, law):
        body = doc.articles.get(target)
        if body:
            matches.append(
                Match(
                    law=doc.law,
                    law_title=doc.law_title,
                    article_no=target,
                    body=body,
                    source=doc.url,
                )
            )
    if not matches:
        scope = f"（限定在 {law}）" if law else ""
        return [], f"知识库中找不到 {target}{scope}，可能超出了该法律的条文范围。"
    return matches, ""


def lexical_search(
    documents: list[Document], keyword: str, law: str | None = None, limit: int = 8
) -> tuple[list[Match], str]:
    """按关键词做**字面**匹配，按相关度排序后返回包含它的完整条文。

    为什么要排序而不是按文档顺序取前 N 条：
        高频词（如"网络运营者"）在几十条条文里都出现，而它**真正被定义的**
        那一条（第七十六条）在文档里靠后。按文档顺序取前 N 条会恰好把它漏掉
        ——实测踩过。所以先按下面的依据排序，再取前 N 条。
    """
    needle = keyword.strip()
    if not needle:
        return [], "关键词为空。"

    scored: list[tuple[tuple, Match]] = []
    total = 0
    for doc in pick(documents, law):
        for article_no, body in doc.articles.items():
            if needle in body or needle in article_no:
                total += 1
                match = Match(
                    law=doc.law,
                    law_title=doc.law_title,
                    article_no=article_no,
                    body=body,
                    source=doc.url,
                )
                scored.append((_rank_key(match, needle), match))

    if not scored:
        scope = f"（限定在 {law}）" if law else ""
        return [], f"没有任何条文的原文里包含「{needle}」{scope}。"

    # Python 的排序是稳定的，同分时保持文档顺序
    scored.sort(key=lambda item: item[0], reverse=True)
    matches = [match for _, match in scored[:limit]]
    note = f"共命中 {total} 条，已按相关度排序并列出前 {len(matches)} 条。"
    return matches, note


def _rank_key(match: Match, needle: str) -> tuple:
    """字面匹配的排序依据，元组越大的越靠前。

    排序依据按重要性从高到低：

      1. **关键词就是条文号** —— 等价于精确查找，最强信号
      2. **定义类条文** —— 术语的定义有固定写法，命中它质量最高
      3. **关键词出现在条文开头** —— 比埋在一长串列举里更可能是该条的主题
      4. **出现次数** —— 出现越多越可能是核心内容
    """
    body = match.body
    return (
        # 1. 关键词即条文号
        1 if needle in match.article_no else 0,
        # 2. 定义类条文
        1 if any(marker in body for marker in DEFINITION_MARKERS) else 0,
        # 3. 关键词靠近条文开头
        1 if needle in body[:60] else 0,
        # 4. 出现次数
        body.count(needle),
    )
