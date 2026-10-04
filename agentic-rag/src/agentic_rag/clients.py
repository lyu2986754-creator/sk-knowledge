"""对外部服务的调用：上游 Java RAG 与 LLM。"""

from __future__ import annotations

import asyncio
import json
from dataclasses import dataclass
from typing import Any

import httpx

from .config import Settings

# 值得重试的 HTTP 状态：限流与服务器端临时故障。
# 4xx 不在此列——重试一个 401 只是把失败延后，没有任何成功可能。
RETRYABLE_STATUS = frozenset({429, 500, 502, 503, 504})


class UpstreamError(RuntimeError):
    """上游服务返回了非成功响应。"""


class RetryableError(UpstreamError):
    """临时性故障，值得重试（超时、连接失败、5xx、限流、非法 JSON）。"""


async def _with_retry(operation, *, settings: Settings, what: str):
    """按配置重试可恢复错误，耗尽后抛 UpstreamError。

    只重试 RetryableError。4xx 与业务错误直接抛出——那类错误重试无意义。
    退避用 2 的幂（1s、2s…），避免上游刚抖动就被连续打穿。
    """
    attempt = 0
    while True:
        try:
            return await operation()
        except RetryableError as exc:
            if attempt >= settings.max_retries:
                raise UpstreamError(
                    f"{what}重试 {settings.max_retries} 次后仍失败：{exc}"
                ) from exc
            delay = settings.retry_backoff_s * (2**attempt)
            print(f"[retry] {what}失败（{exc}），{delay:.0f}s 后第 {attempt + 1} 次重试")
            await asyncio.sleep(delay)
            attempt += 1


@dataclass(frozen=True, slots=True)
class Chunk:
    rank: int
    content: str
    source: str

    @classmethod
    def from_payload(cls, raw: dict) -> Chunk:
        return cls(
            rank=int(raw.get("rank") or 0),
            content=str(raw.get("content") or ""),
            source=str(raw.get("source") or ""),
        )


class RagClient:
    """上游 Java RAG 的客户端。

    只用到它的两个能力：登录（复用它的 JWT）与只读检索。
    问答链路不经过这里——本服务自己编排、自己生成。
    """

    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._http = httpx.AsyncClient(
            base_url=settings.rag_base_url, timeout=httpx.Timeout(120.0)
        )

    async def __aenter__(self) -> RagClient:
        return self

    async def __aexit__(self, *_: object) -> None:
        await self._http.aclose()

    async def _get_json(
        self,
        path: str,
        *,
        token: str,
        what: str,
        params: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        """带重试的只读 GET，返回校验过 code 的响应体。

        Agent 循环里的每一次工具调用都会走这里，因此这是重试的唯一入口：
        临时抖动（超时、5xx）自动重试，鉴权失败等 4xx 立即抛出。
        """

        async def call() -> dict[str, Any]:
            try:
                response = await self._http.get(
                    path, params=params, headers={"Authorization": token}
                )
            except httpx.TransportError as exc:
                raise RetryableError(f"{what}请求未送达：{exc}") from exc
            if response.status_code in RETRYABLE_STATUS:
                raise RetryableError(f"{what} HTTP {response.status_code}")
            if response.status_code != 200:
                raise UpstreamError(
                    f"{what}失败 HTTP {response.status_code}: {response.text[:200]}"
                )
            body = response.json()
            if body.get("code") != 200:
                raise UpstreamError(f"{what}失败：{body.get('msg')!r}")
            return body

        return await _with_retry(call, settings=self._settings, what=what)

    async def login(self, username: str, password: str) -> dict[str, Any]:
        """把表单登录原样转发给 Java，响应也原样返回。

        本服务不做自己的认证：账号体系只有一套，少一层就少一处不一致。
        登录**不重试**——账号/密码错了重试也没用，只会让用户多等。
        """
        response = await self._http.post(
            "/api/login", data={"username": username, "password": password}
        )
        if response.status_code != 200:
            raise UpstreamError(f"登录失败 HTTP {response.status_code}: {response.text[:200]}")
        body = response.json()
        if body.get("code") != 200:
            raise UpstreamError(f"登录失败：{body.get('msg')!r}")
        return body

    async def retrieve(self, query: str, knowledge_id: int, top_k: int, token: str) -> list[Chunk]:
        """只读检索。这一步是 Agent 循环的"工具"。"""
        body = await self._get_json(
            "/api/retrieve",
            params={"query": query, "knowledgeId": knowledge_id, "topK": top_k},
            token=token,
            what="检索",
        )
        return [Chunk.from_payload(item) for item in (body.get("data") or [])]

    async def save_round(
        self,
        *,
        window_id: str | None,
        knowledge_id: int,
        model_id: int,
        question: str,
        answer: str,
        token: str,
    ) -> dict[str, Any]:
        """把一轮已完成的问答交回 Java 落库。

        为什么不直接写 MongoDB：会话数据的形状（窗口创建、标题、消息结构）
        只应有一处定义。交给 Java 落库，前端的历史列表就不需要区分
        "这一条是朴素模式生成的还是 Agentic 生成的"。
        """
        response = await self._http.post(
            "/api/chat/round",
            json={
                "windowId": window_id or "",
                "knowledgeId": knowledge_id,
                "modelId": model_id,
                "question": question,
                "answer": answer,
            },
            headers={"Authorization": token},
        )
        if response.status_code != 200:
            raise UpstreamError(f"保存会话失败 HTTP {response.status_code}: {response.text[:200]}")
        body = response.json()
        if body.get("code") != 200:
            raise UpstreamError(f"保存会话失败：{body.get('msg')!r}")
        return body.get("data") or {}

    async def read_document_text(self, doc_id: int, token: str) -> dict[str, Any]:
        """读取文档的原始文本。

        返回的是**未被切碎**的全文。按条文号取完整原文、按关键词字面匹配，
        都必须基于它——向量库里的片段做不到这两件事。
        """
        body = await self._get_json(
            f"/api/knowledge/doc/{doc_id}/text", token=token, what="读取文档原文"
        )
        return body.get("data") or {}

    async def list_documents(self, knowledge_id: int, token: str) -> list[dict[str, Any]]:
        """列出知识库下的文档（含 MinIO 地址与原始文件名）。

        `list_sources` 工具与语料缓存都依赖它。
        """
        body = await self._get_json(
            "/api/knowledge/doc",
            params={"pageNum": 1, "pageSize": 200, "knowledgeId": knowledge_id},
            token=token,
            what="查询文档列表",
        )
        data = body.get("data") or {}
        listed = data.get("list") if isinstance(data, dict) else data
        return list(listed or [])

    async def load_history(self, window_id: str, token: str) -> list[dict[str, Any]]:
        """读取某个会话窗口的历史消息（按时间顺序）。

        数据来自 Java —— 会话数据的唯一所有者，本服务不自己存一份。
        没有历史（新会话）时返回空列表，不视为错误。

        刻意**不走重试**：历史读不到只会让指代消解退化，不影响回答；
        为它重试 2 次会把整轮问答的延迟白推高几秒。
        """
        if not window_id:
            return []
        response = await self._http.get(
            "/api/chat/completions",
            params={"windowId": window_id},
            headers={"Authorization": token},
        )
        if response.status_code != 200:
            # 历史读不到不该让整轮问答失败，降级为"没有历史"
            return []
        body = response.json()
        if body.get("code") != 200:
            return []
        return list(body.get("data") or [])


class LlmClient:
    """OpenAI 兼容的对话补全客户端（本项目用 DeepSeek）。"""

    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._http = httpx.AsyncClient(
            base_url=settings.llm_base_url, timeout=httpx.Timeout(180.0)
        )

    async def __aenter__(self) -> LlmClient:
        return self

    async def __aexit__(self, *_: object) -> None:
        await self._http.aclose()

    async def complete_json(
        self, system: str, user: str, *, temperature: float = 0.1
    ) -> tuple[dict[str, Any], dict[str, Any]]:
        """要求模型输出 JSON 对象，返回 (解析结果, usage)。

        用 response_format=json_object 而不是靠自己解析文本：
        Agent 循环里每一轮都要拿结构化决策，解析失败一次就断一轮。
        仍然解析失败时按可恢复错误重试——这多半是模型偶发输出截断，
        比整轮失败更值得再试一次。
        """

        async def call() -> tuple[dict[str, Any], dict[str, Any]]:
            try:
                response = await self._http.post(
                    "/v1/chat/completions",
                    headers={"Authorization": f"Bearer {self._settings.llm_api_key}"},
                    json={
                        "model": self._settings.llm_model,
                        "messages": [
                            {"role": "system", "content": system},
                            {"role": "user", "content": user},
                        ],
                        "temperature": temperature,
                        "response_format": {"type": "json_object"},
                    },
                )
            except httpx.TransportError as exc:
                raise RetryableError(f"LLM 请求未送达：{exc}") from exc
            if response.status_code in RETRYABLE_STATUS:
                raise RetryableError(f"LLM HTTP {response.status_code}")
            if response.status_code != 200:
                raise UpstreamError(
                    f"LLM 调用失败 HTTP {response.status_code}: {response.text[:300]}"
                )
            body = response.json()
            content = body["choices"][0]["message"]["content"]
            try:
                parsed = json.loads(content)
            except json.JSONDecodeError as exc:
                raise RetryableError(f"LLM 没有返回合法 JSON：{content[:200]!r}") from exc
            return parsed, body.get("usage") or {}

        return await _with_retry(call, settings=self._settings, what="LLM 决策调用")
