"""Agent 循环：让模型自己决定「用哪个工具、查什么、查几次」

为什么不用 LangChain / LangGraph 这类框架（project.md R5）：
    循环控制、工具选择、终止条件正是本项目要观测和评分的对象。
    框架会把它们藏起来，而它们恰恰是最需要看得清楚的地方。

四种工具，按「动作类型」区分（这不是巧合，是为了让工具选择可判定）：

    search          语义检索 —— 概念性提问
    lexical_search  字面匹配 —— 条文编号、专有名词
    read_article    精确定位 —— 读某一条的完整原文
    list_sources    元信息   —— 知识库里到底有什么

为什么要去重（实测驱动）：
    多轮检索存在大量重复——10 个 case 累计 67 条重复片段，
    最严重的一条第二轮返回的 10 条与第一轮**完全相同**。
    去重后上下文条数 370 → 314（−15%），且"本轮查空了"本身是有用信号。

为什么不做成"判断"与"生成"两次调用（实测否决）：
    曾经拆开过，token 从 50,683 涨到 74,819（+48%）而指标还退了一点。
    要的"停止决策"信号只需要一个 `enough` 字段，拆分属于过度设计。已回滚。
"""

from __future__ import annotations

import re
import time
from dataclasses import dataclass, field

from . import tools
from .clients import Chunk, LlmClient, RagClient
from .config import Settings

DECISION_PROMPT = """你是一个检索增强问答 Agent。你有四个工具，每一轮选**一个**来用。

## 工具

1. **search** —— 语义检索。适合**概念性**提问。
   {{"enough": false, "tool": "search", "query": "检索语句", "reason": "..."}}

2. **lexical_search** —— 字面匹配。适合**条文编号、专有名词**这类精确内容。
   语义检索对这类内容天然不敏感，所以要用它。
   {{"enough": false, "tool": "lexical_search", "keyword": "要匹配的字符串（如 第十三条、网络运营者）", "law": "可选，如 pipl", "reason": "..."}}

3. **read_article** —— 按条文号读**完整原文**。已知具体条文号时首选它，
   因为检索返回的片段可能被截断。
   {{"enough": false, "tool": "read_article", "articleNo": "第十三条", "law": "可选，如 pipl", "reason": "..."}}

4. **list_sources** —— 列出知识库里有哪些法律。想知道"能查什么"时用。
   {{"enough": false, "tool": "list_sources", "reason": "..."}}

## 什么时候可以作答

{{"enough": true, "answer": "最终答案", "reason": "为什么认为已经够了"}}

- **第一轮必须调用工具**（enough 必须为 false）——现在手上什么都没有。
- 资料足以回答时就 enough=true 直接作答，**不要再查**。该停不停会浪费成本。

## 工具怎么选（这是最容易做错的地方）

- 问题里出现**具体条文号**（如"第13条"）→ 优先用 **read_article**，
  而不是 search。实测：直接拿"第13条规定了什么"去语义检索，
  会因为向量检索对编号不敏感而**漏掉第十三条**（召回的是第53条、第23条、第10条）。
- 问题里出现**精确术语**（如"网络运营者"的定义）→ **lexical_search**。
- 问题是**概念性**的（如"处理个人信息要遵循哪些原则"）→ **search**。
- 不确定知识库里有什么 → **list_sources**。

## 避免无效重复

如果上一轮工具**没有带来新内容**，说明这个角度查过了。
下一轮必须**换工具或换完全不同的角度**，不要重复或微调同样的调用——那是浪费。

## 作答要求

- **只依据工具返回的内容作答**，不要用你自己的知识补充。
- 内容里没有依据时，明确说「知识库中没有相关内容」，**不要编造**。
- 可以引用条文编号，例如「根据第二十一条」。

最多 {max_steps} 轮。
"""

TOOL_NAMES = ("search", "lexical_search", "read_article", "list_sources")

# 预算用尽时的收尾指令。此时不再检索，只把已有内容整理成答案——
# 宁可给一个基于部分证据的答案，也不要把前面几轮检索的成果整个丢掉。
BUDGET_EXHAUSTED_INSTRUCTION = (
    "\n\n**token 预算已用尽：立即停止检索（enough 必须为 true），"
    "只依据上面已获得的内容给出最终 answer。**"
)

# 从答案文本里抽条文号。用于把历史压缩成"引用了哪些条文"，
# 纯正则抽取，零额外 LLM 调用。
_CITED_ARTICLE_RE = re.compile(r"第[一二三四五六七八九十百零\d]+条")


@dataclass(frozen=True, slots=True)
class Turn:
    """一轮历史对话。

    刻意**只存「问题 + 答案里引用过的条文号」，不存答案正文**：
      - RAG Agent 的历史，核心用途是消解追问里的指代，不是让模型"记住内容"
      - 答案正文占 token 且可以重新检索，而"用户之前问过什么"无法重来
      - 条文号正是指代最常指向的对象，且能零成本抽取
    """

    question: str
    cited_articles: tuple[str, ...]


def build_history(messages: list[dict], limit: int) -> list[Turn]:
    """把 Java 返回的消息列表压成最近 limit 轮。"""
    turns: list[Turn] = []
    pending_question: str | None = None
    for message in messages:
        role = str(message.get("role") or "")
        content = str(message.get("content") or "").strip()
        if role == "user":
            pending_question = content
        elif role == "assistant" and pending_question:
            # dict.fromkeys 去重且保持顺序
            cited = tuple(dict.fromkeys(_CITED_ARTICLE_RE.findall(content)))[:5]
            turns.append(Turn(question=pending_question, cited_articles=cited))
            pending_question = None
    if limit <= 0:
        return []
    return turns[-limit:]


def _format_dialogue(history: list[Turn]) -> str:
    if not history:
        return ""
    lines = ["## 之前的对话（用于理解当前问题里的指代）", ""]
    for index, turn in enumerate(history, 1):
        lines.append(f"第 {index} 轮　用户问：{turn.question}")
        if turn.cited_articles:
            lines.append(f"　　　　　回答中引用了：{'、'.join(turn.cited_articles)}")
    lines.append("")
    lines.append(
        "如果当前问题里有「它」「这个」「那」这类指代，请结合上面的对话"
        "把指代补全，**再产出检索语句**（例如把「那它有什么例外」补成"
        "「个人信息保护法第十三条有什么例外情形」）。但不要直接回答之前的问题。"
    )
    return "\n".join(lines)


@dataclass(frozen=True, slots=True)
class ContextItem:
    """累积上下文里的一条。

    `label` 给人看（让模型知道内容来自哪里）；
    `source` 给评测平台用（原始 MinIO 地址，检索评分靠它判断片段来自哪部法律）。
    """

    label: str
    content: str
    source: str = ""


@dataclass(frozen=True, slots=True)
class ToolStep:
    index: int
    tool: str
    params: dict
    reason: str
    returned: int
    added: int
    preview: str

    @property
    def produced_nothing_new(self) -> bool:
        return self.added == 0


@dataclass(slots=True)
class AgentResult:
    answer: str
    steps: list[ToolStep] = field(default_factory=list)
    context: list[ContextItem] = field(default_factory=list)
    prompt_tokens: int = 0
    completion_tokens: int = 0
    generate_ms: int = 0
    stopped_by: str = ""  # enough_decided / max_steps / token_budget
    final_enough: bool | None = None

    @property
    def total_tokens(self) -> int:
        return self.prompt_tokens + self.completion_tokens


def _format_context(items: list[ContextItem]) -> str:
    if not items:
        return "（还没有拿到任何内容）"
    blocks = []
    for index, item in enumerate(items, start=1):
        header = f"[{index}] {item.label}" if item.label else f"[{index}]"
        blocks.append(f"{header}\n{item.content}")
    return "\n\n".join(blocks)


def _format_history(steps: list[ToolStep]) -> str:
    if not steps:
        return "（还没有调用过工具）"
    lines = []
    for step in steps:
        args = ", ".join(f"{k}={v}" for k, v in step.params.items() if v)
        note = ""
        if step.produced_nothing_new:
            detail = f"（{step.preview}）" if step.preview else ""
            note = f" —— **没有带来新内容{detail}，这个角度已经查过了**"
        lines.append(
            f"第 {step.index} 轮：{step.tool}({args}) 返回 {step.returned} 条，"
            f"新增 {step.added} 条{note}"
        )
    return "\n".join(lines)


def _build_user_prompt(
    question: str,
    items: list[ContextItem],
    steps: list[ToolStep],
    step: int,
    max_steps: int,
    dialogue: str = "",
) -> str:
    tail = "\n\n**这是最后一轮，你必须在本次输出中给出 answer。**" if step >= max_steps else ""
    prefix = f"{dialogue}\n\n" if dialogue else ""
    return (
        f"{prefix}用户问题：{question}\n\n"
        f"已调用的工具：\n{_format_history(steps)}\n\n"
        f"当前已获得的内容：\n{_format_context(items)}\n\n"
        f"现在是第 {step}/{max_steps} 轮。请输出你的决策 JSON。{tail}"
    )


def _chunks_to_items(chunks: list[Chunk], label_prefix: str = "") -> list[ContextItem]:
    return [
        ContextItem(label=f"{label_prefix}检索片段", content=chunk.content) for chunk in chunks
    ]


async def _dispatch(
    decision: dict,
    *,
    rag: RagClient,
    token: str,
    knowledge_id: int,
    settings: Settings,
    cache: tools.CorpusCache,
) -> tuple[str, dict, list[ContextItem], str]:
    """执行一次工具调用，返回 (工具名, 参数, 结果条目, 摘要)。"""
    tool = str(decision.get("tool") or "").strip()
    if tool not in TOOL_NAMES:
        return "", {}, [], f"未知工具：{tool!r}"

    if tool == "search":
        query = str(decision.get("query") or "").strip()
        if not query:
            return "", {}, [], "search 缺少 query"
        chunks = await rag.retrieve(query, knowledge_id, settings.top_k, token)
        items = [
            ContextItem(label=f"语义检索片段 #{i}", content=c.content, source=c.source)
            for i, c in enumerate(chunks, start=1)
        ]
        return tool, {"query": query}, items, f"返回 {len(chunks)} 条语义检索片段"

    documents = await cache.documents(rag, token, knowledge_id)

    if tool == "list_sources":
        text = tools.list_sources(documents)
        return tool, {}, [ContextItem(label="知识库清单", content=text)], "知识库清单"

    if tool == "read_article":
        article_no = str(decision.get("articleNo") or "").strip()
        law = str(decision.get("law") or "").strip() or None
        if not article_no:
            return "", {}, [], "read_article 缺少 articleNo"
        matches, note = tools.read_article(documents, article_no, law)
        items = [
            ContextItem(label=m.label, content=m.render(), source=m.source) for m in matches
        ]
        return (
            tool,
            {"articleNo": article_no, "law": law or ""},
            items,
            note or f"找到 {len(matches)} 条完整原文",
        )

    keyword = str(decision.get("keyword") or "").strip()
    law = str(decision.get("law") or "").strip() or None
    if not keyword:
        return "", {}, [], "lexical_search 缺少 keyword"
    matches, note = tools.lexical_search(documents, keyword, law)
    items = [ContextItem(label=m.label, content=m.render(), source=m.source) for m in matches]
    return (
        tool,
        {"keyword": keyword, "law": law or ""},
        items,
        note or f"字面匹配到 {len(matches)} 条",
    )


async def run(
    question: str,
    *,
    knowledge_id: int,
    token: str,
    rag: RagClient,
    llm: LlmClient,
    settings: Settings,
    cache: tools.CorpusCache | None = None,
    history: list[Turn] | None = None,
) -> AgentResult:
    started = time.perf_counter()
    result = AgentResult(answer="")
    system = DECISION_PROMPT.format(max_steps=settings.max_steps)
    cache = cache or tools.CorpusCache()
    seen: set[str] = set()
    # 历史只进决策器，不进生成阶段——决定生成的只是当轮召回的内容。
    dialogue = _format_dialogue(history or [])

    for step in range(1, settings.max_steps + 1):
        # 预算刹车必须在**发起调用之前**检查：LLM 请求一旦发出就无法中途掐断，
        # 所以这里管的是"要不要再开新一轮"，而不是"绝不超过 N token"。
        # 因此预算的语义是**软上限**——超限的那一轮本身仍会完整计费。
        if result.total_tokens >= settings.token_budget:
            result.stopped_by = "token_budget"
            break

        decision, usage = await llm.complete_json(
            system,
            _build_user_prompt(
                question, result.context, result.steps, step, settings.max_steps, dialogue
            ),
        )
        result.prompt_tokens += int(usage.get("prompt_tokens") or 0)
        result.completion_tokens += int(usage.get("completion_tokens") or 0)

        enough = bool(decision.get("enough"))
        result.final_enough = enough
        answer = str(decision.get("answer") or "").strip()
        reason = str(decision.get("reason") or "")

        if enough and answer and step > 1:
            result.answer = answer
            result.stopped_by = "enough_decided"
            break

        tool, params, items, preview = await _dispatch(
            decision,
            rag=rag,
            token=token,
            knowledge_id=knowledge_id,
            settings=settings,
            cache=cache,
        )

        if not tool:
            # 既没给出可执行工具，也没有答案，只能收手
            if answer:
                result.answer = answer
            result.stopped_by = "enough_decided"
            break

        fresh: list[ContextItem] = []
        for item in items:
            fingerprint = item.content.strip()
            if not fingerprint or fingerprint in seen:
                continue
            seen.add(fingerprint)
            fresh.append(item)
        result.context.extend(fresh)

        result.steps.append(
            ToolStep(
                index=step,
                tool=tool,
                params=params,
                reason=reason,
                returned=len(items),
                added=len(fresh),
                preview=preview,
            )
        )

        if step >= settings.max_steps and answer:
            result.answer = answer
            result.stopped_by = "max_steps"
            break

    if not result.answer and result.stopped_by == "token_budget":
        # 预算用尽：不再检索，用已有上下文强制收一个答案。
        # 这次调用的 token 仍计入总量（软上限的实际含义）。
        decision, usage = await llm.complete_json(
            system,
            _build_user_prompt(
                question, result.context, result.steps, settings.max_steps, settings.max_steps, dialogue
            )
            + BUDGET_EXHAUSTED_INSTRUCTION,
        )
        result.prompt_tokens += int(usage.get("prompt_tokens") or 0)
        result.completion_tokens += int(usage.get("completion_tokens") or 0)
        result.answer = str(decision.get("answer") or "").strip()

    if not result.answer:
        result.stopped_by = result.stopped_by or "max_steps"
        result.answer = "知识库中没有相关内容。"

    result.generate_ms = int((time.perf_counter() - started) * 1000)
    return result
