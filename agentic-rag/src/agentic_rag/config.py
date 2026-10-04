"""配置。所有密钥只从环境变量读，禁止硬编码。"""

from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path


class ConfigError(RuntimeError):
    """环境变量缺失或非法。"""


@dataclass(frozen=True, slots=True)
class Settings:
    port: int
    rag_base_url: str
    llm_base_url: str
    llm_api_key: str
    llm_model: str
    max_steps: int
    top_k: int
    history_turns: int
    # ---- 健壮性三件套（数值依据见 project.md 与本目录 README）----
    # 一轮问答允许消耗的 token 上限（prompt + completion 跨步骤累加）。
    # 超限则停止检索、用已有内容作答；**软上限**，见 agent.py 说明。
    token_budget: int
    # 整轮问答的总时长上限（秒）。与 httpx 单次调用超时是两层：
    # 单次超时管不住"每次都慢但不超时"的累积。
    overall_timeout_s: float
    # 可恢复故障（超时 / 连接失败 / 5xx / 限流 / LLM 返回非法 JSON）的重试次数。
    # 与 Java 侧 Spring AI 的 maximumAttempts=3 对齐（即重试 2 次）。
    max_retries: int
    # 首次重试前的等待秒数，之后按 2 的幂退避（1s、2s…）。
    retry_backoff_s: float
    debug: bool


def _require(name: str) -> str:
    value = os.environ.get(name, "").strip()
    if not value:
        raise ConfigError(f"缺少环境变量 {name}，请参考 agentic-rag/.env.example")
    return value


def load_settings() -> Settings:
    return Settings(
        port=int(os.environ.get("AGENTIC_PORT") or "8092"),
        rag_base_url=_require("RAG_BASE_URL").rstrip("/"),
        llm_base_url=os.environ.get("DEEPSEEK_BASE_URL", "https://api.deepseek.com").rstrip("/"),
        llm_api_key=_require("DEEPSEEK_API_KEY"),
        llm_model=os.environ.get("DEEPSEEK_MODEL", "deepseek-chat"),
        max_steps=int(os.environ.get("AGENT_MAX_STEPS") or "3"),
        top_k=int(os.environ.get("AGENT_TOP_K") or "10"),
        # 带回几轮对话历史。只喂给决策器（用于消解追问里的指代），不喂给生成。
        # 默认 4 是起点，应当在多轮评测里用数据确定，而不是拍脑袋。
        history_turns=int(os.environ.get("AGENT_HISTORY_TURNS") or "4"),
        # 12,000 = 实测单条最大 3,720 的 3.2 倍。压到 8k 会误伤长问题
        # （prompt 逐轮增长，3 轮最坏 5–6k）；放到 12k 以上基本已是失控，拦不拦没区别。
        token_budget=int(os.environ.get("AGENT_TOKEN_BUDGET") or "12000"),
        # 60s = 实测单条最大 4.7s 的约 13 倍。
        overall_timeout_s=float(os.environ.get("AGENT_OVERALL_TIMEOUT") or "60"),
        max_retries=int(os.environ.get("AGENT_MAX_RETRIES") or "2"),
        retry_backoff_s=float(os.environ.get("AGENT_RETRY_BACKOFF") or "1"),
        debug=os.environ.get("AGENT_DEBUG", "0") not in ("", "0", "false", "False"),
    )


def load_dotenv(path: Path) -> None:
    """极简 .env 加载：KEY=VALUE、# 注释、两侧引号。已存在的环境变量优先。"""
    if not path.is_file():
        return
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        key = key.strip()
        value = value.strip().strip('"').strip("'")
        if key and key not in os.environ:
            os.environ[key] = value
