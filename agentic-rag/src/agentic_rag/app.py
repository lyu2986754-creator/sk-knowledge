"""Agentic RAG 的 HTTP 服务。

接口形态**刻意与 Java RAG 完全一致**（路径、参数、SSE 事件名都一样）。
这样 RAG 前端切换模式时只需要换 baseURL，两步式调用与 EventSource 全部复用。

启动：
    python -m agentic_rag.app
"""

from __future__ import annotations

import asyncio
import json
import time
import uuid
from pathlib import Path
from typing import Any

from fastapi import FastAPI, Form, Header, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, StreamingResponse
import httpx

from . import agent
from .clients import RagClient
from .config import ConfigError, Settings, load_dotenv, load_settings
from .tools import CorpusCache

SESSION_TTL_SECONDS = 300

app = FastAPI(title="Agentic RAG", version="0.1.0")
app.add_middleware(
    CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"]
)

_settings: Settings | None = None

# sessionId -> 会话数据。本地单用户工具，内存足够；进程重启即清空。
_sessions: dict[str, dict[str, Any]] = {}

# 语料缓存提升到模块级：条文切分与字面匹配都基于原始文档，
# 每次对话重新拉一遍既慢又浪费。文档更新由 TTL 兜底。
_corpus_cache = CorpusCache()


def settings() -> Settings:
    global _settings
    if _settings is None:
        _settings = load_settings()
    return _settings


def _sse(event: str, data: Any) -> str:
    payload = data if isinstance(data, str) else json.dumps(data, ensure_ascii=False)
    # SSE 规范：data 里**不能有裸换行**，必须拆成多行 `data:`。
    # 否则接收方会把换行之后的文字当成未知字段直接丢弃——
    # 表现为答案在换行处被悄悄截断，而且不报任何错。
    body = "".join(f"data:{line}\n" for line in payload.split("\n"))
    return f"event:{event}\n{body}\n"


def _purge_expired() -> None:
    deadline = time.time() - SESSION_TTL_SECONDS
    for key in [k for k, v in _sessions.items() if v["created_at"] < deadline]:
        _sessions.pop(key, None)


@app.get("/api/health")
async def health() -> dict:
    return {"status": "ok", "service": "agentic-rag", "sessions": len(_sessions)}


async def _proxy_get(request: Request, path: str) -> JSONResponse:
    """把只读的元数据类接口原样转发给 Java。

    为什么需要：本服务要成为 Java 的**完整替身**，前端才能只切一个 baseURL。
    知识库文档列表、会话历史这些数据本来就属于 Java，本服务不复制一份，
    直接转发即可——少一份数据就少一处不一致。
    """
    async with httpx.AsyncClient(base_url=settings().rag_base_url, timeout=60.0) as client:
        response = await client.get(
            path,
            params=dict(request.query_params),
            headers={"Authorization": request.headers.get("authorization", "")},
        )
    return JSONResponse(response.json(), status_code=response.status_code)


@app.get("/api/knowledge/doc")
async def proxy_knowledge_doc(request: Request) -> JSONResponse:
    return await _proxy_get(request, "/api/knowledge/doc")


@app.get("/api/chat/window")
async def proxy_chat_window(request: Request) -> JSONResponse:
    return await _proxy_get(request, "/api/chat/window")


@app.get("/api/chat/completions")
async def proxy_chat_completions(request: Request) -> JSONResponse:
    return await _proxy_get(request, "/api/chat/completions")


@app.post("/api/login")
async def login(username: str = Form(...), password: str = Form(...)) -> dict:
    """把登录转发给 Java，复用它的 JWT。本服务不维护自己的账号体系。"""
    async with RagClient(settings()) as rag:
        return await rag.login(username, password)


@app.post("/api/chat/completions")
async def create_session(
    payload: dict[str, Any],
    authorization: str | None = Header(default=None),
) -> dict:
    """收下问题与 token，生成 sessionId。与 Java 的两步式调用保持一致。"""
    _purge_expired()
    question = str(payload.get("content") or "").strip()
    knowledge_id = payload.get("knowledgeId")
    if not question or knowledge_id is None:
        raise HTTPException(status_code=400, detail="缺少 content 或 knowledgeId")
    if not authorization:
        raise HTTPException(status_code=401, detail="缺少 Authorization 头")

    session_id = str(uuid.uuid4())
    _sessions[session_id] = {
        "question": question,
        "knowledge_id": int(knowledge_id),
        "model_id": int(payload.get("modelId") or 0),
        "token": authorization,
        # 保留前端传来的原值。空字符串表示"这是一轮新对话"——
        # 不要在这里编一个临时 id 冒充窗口 id，否则落库时会拿它去查一个
        # 不存在的窗口（实测踩过：Java 侧查不到就报错，整轮对话丢失）。
        "window_id": str(payload.get("windowId") or ""),
        "created_at": time.time(),
    }
    return {"code": 200, "msg": "成功", "data": session_id}


@app.get("/api/chat/stream/{session_id}/{knowledge_id}")
async def stream(session_id: str, knowledge_id: int, request: Request) -> StreamingResponse:
    session = _sessions.get(session_id)
    if session is None:
        raise HTTPException(status_code=404, detail="sessionId 不存在或已过期")

    cfg = settings()

    async def event_source():
        question = session["question"]
        token = session["token"]
        window_id = session["window_id"]
        assistant_id = uuid.uuid4().hex[:24]
        user_question_id = uuid.uuid4().hex[:24]

        title = question[:18] or "新对话"

        try:
            async with RagClient(cfg) as rag:
                from .clients import LlmClient

                # 读取本窗口的历史对话。**只用于消解追问里的指代**，不参与生成——
                # 生成阶段只依据当轮召回到的内容，历史进来只会增加噪声与 token。
                raw_history = await rag.load_history(window_id, token)
                history = agent.build_history(raw_history, cfg.history_turns)

                async with LlmClient(cfg) as llm:
                    # 整体超时是**外层总闸**：httpx 的单次超时管不住
                    # "每次都慢但都没超时"的累积（最多 3 轮 × 每轮 1 次决策 + 1 次检索）。
                    # 超时即整轮失败，由下面的 except 转成前端可见的错误气泡。
                    try:
                        result = await asyncio.wait_for(
                            agent.run(
                                question,
                                knowledge_id=session["knowledge_id"],
                                token=token,
                                rag=rag,
                                llm=llm,
                                settings=cfg,
                                cache=_corpus_cache,
                                history=history,
                            ),
                            timeout=cfg.overall_timeout_s,
                        )
                    except asyncio.TimeoutError as exc:
                        raise RuntimeError(
                            f"整轮问答超过总体超时 {cfg.overall_timeout_s:.0f}s"
                        ) from exc

                # 落库：复用 Java 的会话数据结构，前端历史列表因此不需要区分
                # 这一条是朴素模式还是 Agentic 模式生成的。失败不影响回答，只记日志。
                try:
                    saved = await rag.save_round(
                        window_id=window_id,
                        knowledge_id=session["knowledge_id"],
                        model_id=session.get("model_id") or 0,
                        question=question,
                        answer=result.answer,
                        token=token,
                    )
                    window_id = str(saved.get("chatWindowId") or window_id)
                    title = str(saved.get("chatWindowTittle") or title)
                except Exception as save_exc:  # noqa: BLE001
                    print(f"[warn] 会话落库失败（不影响回答）：{save_exc}")
                if not window_id:
                    # 落库失败时给一个本地 id，保证前端会话列表仍可用。
                    # 代价：这条会话刷新页面后无法从历史里恢复——但比整轮回答丢失好。
                    window_id = uuid.uuid4().hex[:24]
        except Exception as exc:  # noqa: BLE001 - 失败必须让前端可见，不能静默
            yield _sse(
                "chatContextIdVo",
                {
                    "userQuestionId": user_question_id,
                    "assistantId": assistant_id,
                    "chatWindowId": window_id,
                    "chatWindowTittle": title,
                },
            )
            yield _sse("content", f"[Agentic 模式执行失败] {type(exc).__name__}: {exc}")
            yield _sse("done", {"id": assistant_id, "role": "assistant", "content": ""})
            return

        # 与 Java 一致的第一帧：前端靠它建会话、建 AI 气泡。
        # 放在这里而不是请求最开始，是因为要等落库拿到**真实的** windowId 与标题——
        # 前端的历史列表直接用这个 id，用临时的会导致刷新后找不到。
        yield _sse(
            "chatContextIdVo",
            {
                "userQuestionId": user_question_id,
                "assistantId": assistant_id,
                "chatWindowId": window_id,
                "chatWindowTittle": title,
            },
        )

        # 逐字推送，与 Java 的流式观感一致
        for index in range(0, len(result.answer), 24):
            yield _sse("content", result.answer[index : index + 24])
            await asyncio.sleep(0)

        # 诊断事件：过程层归因的数据来源，事件名与 Java 保持一致。
        # result.context 里存的已经是**去重后的新增内容**，所以这里天然没有重复。
        # source 保留原始 MinIO 地址（评测平台靠它判断片段来自哪部法律），
        # 认不出来时退回可读的 label，便于人工排查。
        context_items: list[dict[str, Any]] = [
            {"rank": index, "content": item.content, "source": item.source or item.label}
            for index, item in enumerate(result.context, start=1)
        ]
        yield _sse(
            "diagnostics",
            {
                "retrievedChunks": context_items,
                "promptTokens": result.prompt_tokens or None,
                "completionTokens": result.completion_tokens or None,
                "generateMs": result.generate_ms,
                "firstTokenMs": None,
                # ---- Agentic 模式独有的过程信号 ----
                # 评测平台据此评估：工具选择是否正确、检索是否冗余、
                # 以及"该停的时候停了吗"（stop_decision_quality）。
                "agentSteps": [
                    {
                        "index": step.index,
                        "tool": step.tool,
                        "params": step.params,
                        "reason": step.reason,
                        "returned": step.returned,
                        "added": step.added,
                        "producedNothingNew": step.produced_nothing_new,
                    }
                    for step in result.steps
                ],
                "stepCount": len(result.steps),
                # 用过哪些工具、按什么顺序——工具选择评分的基础数据
                "toolsUsed": [step.tool for step in result.steps],
                # 本轮带进来的历史（只进决策器，不进生成），用于排查指代消解是否生效
                "historyTurns": [
                    {"question": turn.question, "citedArticles": list(turn.cited_articles)}
                    for turn in (history or [])
                ],
                # 因模型认为"够了"而停，还是因轮数用尽而停
                "stoppedBy": result.stopped_by,
                # 停止决策信号：最后一次决策认为"够了"吗？
                # 用于评估"该停的时候停了吗"——这是 Agentic 独有的过程信号。
                "finalEnough": result.final_enough,
                # ---- 健壮性开关的取值快照 ----
                # 评测平台必须知道这些，否则"这次 token 高"无法区分
                # 是模型失控还是预算设得宽。与 project.md R3（可复现）同一要求。
                "tokenBudget": cfg.token_budget,
                "totalTokens": result.total_tokens,
                "overallTimeoutS": cfg.overall_timeout_s,
            },
        )
        yield _sse(
            "done",
            {
                "id": assistant_id,
                "windowId": window_id,
                "knowledgeId": session["knowledge_id"],
                "role": "assistant",
                "content": result.answer,
            },
        )

    return StreamingResponse(
        event_source(),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )


def main() -> int:
    import uvicorn

    root = Path(__file__).resolve().parents[2]
    load_dotenv(root / ".env")
    try:
        cfg = settings()
    except ConfigError as exc:
        print(f"配置错误：{exc}")
        return 2

    print(f"Agentic RAG 启动：http://127.0.0.1:{cfg.port}")
    print(f"  上游 RAG : {cfg.rag_base_url}")
    print(f"  模型     : {cfg.llm_model}   最多 {cfg.max_steps} 轮检索")
    print(
        f"  预算     : token ≤ {cfg.token_budget} / 轮，总体超时 {cfg.overall_timeout_s:.0f}s，"
        f"可恢复故障重试 {cfg.max_retries} 次"
    )
    uvicorn.run(app, host="127.0.0.1", port=cfg.port, log_level="info")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
