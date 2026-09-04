#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
sk-knowledge 接口压测脚本 (stdlib only，无需 pip 安装)
==================================================
针对学长建议的"主要 3-4 个接口"做并发压测，输出 QPS / 延迟 / 错误率。

用法:
  # 默认打本地 localhost:8082
  python stress_test.py

  # 指向 VM 上的后端 (改成你的实际 IP:端口)
  BASE_URL=http://192.168.56.101:8082 python stress_test.py

  # 自定义并发与每接口请求数
  python stress_test.py --threads 100 --reqs 50

  # 加鉴权 (手动给 JWT)
  BASE_URL=http://x:8082 TOKEN=eyJxxx python stress_test.py

  # 自动登录取 token (推荐)：用真实账号密码，脚本先登录再压
  BASE_URL=http://localhost:8082 USERNAME=admin PASSWORD=123456 python stress_test.py

依赖: 仅 Python 标准库 (3.7+)
"""
import sys, os, time, json, threading, argparse
import concurrent.futures as cf
from urllib.request import Request, urlopen
from urllib.parse import urlencode
from urllib.error import URLError, HTTPError
import statistics

# ===================== 配置区 =====================
BASE_URL = os.environ.get("BASE_URL", "http://localhost:8082").rstrip("/")
TOKEN = os.environ.get("TOKEN", "")
USERNAME = os.environ.get("USERNAME", "")
PASSWORD = os.environ.get("PASSWORD", "")


def login() -> str:
    """登录 /api/login 拿 JWT。成功返回 data 字段的 token；失败抛异常。"""
    url = BASE_URL + "/api/login"
    body = urlencode({"username": USERNAME, "password": PASSWORD}).encode()
    req = Request(url, data=body, method="POST")
    req.add_header("Content-Type", "application/x-www-form-urlencoded")
    try:
        with urlopen(req, timeout=30) as r:
            payload = json.loads(r.read().decode("utf-8"))
    except HTTPError as e:
        raise RuntimeError(f"登录被拒绝 (HTTP {e.code})，请确认账号密码。")
    except URLError as e:
        raise RuntimeError(f"连不上登录接口: {e.reason}")
    if payload.get("code") not in (0, 200):
        raise RuntimeError(f"登录失败: {payload}")
    tok = payload.get("data")
    if not tok:
        raise RuntimeError("登录成功但未返回 token (data 字段为空)。")
    print(f"[login] 获取 token 成功 (长度 {len(tok)})")
    return tok


HEADERS = {"Content-Type": "application/json"}
if TOKEN:
    HEADERS["Authorization"] = f"Bearer {TOKEN}"

# 学长说的"主要 3-4 个接口" —— 取自真实 Controller 路径
ENDPOINTS = [
    {"name": "知识库列表 GET /api/knowledge/base",  "method": "GET",  "path": "/api/knowledge/base"},
    {"name": "向量库状态 GET /api/vector/db",        "method": "GET",  "path": "/api/vector/db"},
    {"name": "对话窗口 GET /api/chat/window",        "method": "GET",  "path": "/api/chat/window"},
    {"name": "对话补全 POST /api/chat/completions",  "method": "POST", "path": "/api/chat/completions",
     "body": json.dumps({"sessionId": 1, "knowledgeId": 1, "message": "你好，请介绍一下人工智能"})},
]

# 并发参数
THREADS = 50
REQUESTS_PER_THREAD = 20

lock = threading.Lock()
results = []  # (name, latency_ms, status_code)


def hit(ep):
    url = BASE_URL + ep["path"]
    data = ep.get("body")
    if data is not None:
        data = data.encode("utf-8")
    req = Request(url, data=data, method=ep["method"])
    for k, v in HEADERS.items():
        req.add_header(k, v)
    t0 = time.perf_counter()
    try:
        with urlopen(req, timeout=30) as r:
            status = r.status
            _ = r.read()  # 读完 body 才算完整响应
    except HTTPError as e:
        status = e.code
    except URLError as e:
        status = -1  # 连接失败/超时
    except Exception:
        status = -2
    dt = (time.perf_counter() - t0) * 1000.0
    with lock:
        results.append((ep["name"], dt, status))


def pct(values, p):
    if not values:
        return 0.0
    s = sorted(values)
    k = (len(s) - 1) * p
    f = int(k)
    c = min(f + 1, len(s) - 1)
    if f == c:
        return s[f]
    return s[f] + (s[c] - s[f]) * (k - f)


def analyze(elapsed):
    total = len(results)
    ok = [r for r in results if 200 <= r[2] < 400]
    fail = total - len(ok)
    qps = total / elapsed if elapsed > 0 else 0

    print("\n" + "=" * 72)
    print(f"压测报告  |  总请求={total}  成功={len(ok)}  失败={fail}  "
          f"耗时={elapsed:.1f}s  QPS≈{qps:.1f}")
    print("=" * 72)
    hdr = f"{'接口':<42}{'cnt':>5}{'ok%':>7}{'QPS':>8}{'avg':>8}{'p90':>8}{'p99':>8}{'max':>9}"
    print(hdr)
    print("-" * 72)

    by_name = {}
    for name, dt, st in results:
        by_name.setdefault(name, []).append((dt, st))
    for ep in ENDPOINTS:
        name = ep["name"]
        arr = by_name.get(name, [])
        if not arr:
            continue
        lats = [a[0] for a in arr]
        oks = sum(1 for a in arr if 200 <= a[1] < 400)
        q = len(arr) / elapsed if elapsed > 0 else 0
        print(f"{name:<42}{len(arr):>5}{100*oks/len(arr):>6.1f}%{q:>8.1f}"
              f"{statistics.mean(lats):>7.1f}ms{pct(lats,0.90):>7.1f}ms"
              f"{pct(lats,0.99):>7.1f}ms{max(lats):>8.1f}ms")
    print("=" * 72)

    # 落盘报告
    try:
        with open(os.path.join(os.path.dirname(__file__), "stress_report.json"), "w", encoding="utf-8") as f:
            json.dump({
                "base_url": BASE_URL,
                "threads": THREADS,
                "requests_per_thread": REQUESTS_PER_THREAD,
                "elapsed_s": round(elapsed, 2),
                "total": total, "ok": len(ok), "fail": fail,
                "qps": round(qps, 2),
                "per_endpoint": {
                    name: {
                        "count": len(arr),
                        "ok_pct": round(100 * sum(1 for a in arr if 200 <= a[1] < 400) / len(arr), 2),
                        "avg_ms": round(statistics.mean([a[0] for a in arr]), 2),
                        "p90_ms": round(pct([a[0] for a in arr], 0.90), 2),
                        "p99_ms": round(pct([a[0] for a in arr], 0.99), 2),
                        "max_ms": round(max(a[0] for a in arr), 2),
                    } for name, arr in by_name.items()
                }
            }, f, ensure_ascii=False, indent=2)
        print("报告已写入 stress_report.json")
    except Exception as e:
        print(f"(报告落盘失败: {e})")


def main():
    global THREADS, REQUESTS_PER_THREAD
    ap = argparse.ArgumentParser()
    ap.add_argument("--threads", type=int, default=THREADS)
    ap.add_argument("--reqs", type=int, default=REQUESTS_PER_THREAD)
    args = ap.parse_args()
    THREADS, REQUESTS_PER_THREAD = args.threads, args.reqs

    # 自动登录拿 token（提供了 USERNAME/PASSWORD 且未手动给 TOKEN 时）
    global TOKEN, HEADERS
    if not TOKEN and USERNAME and PASSWORD:
        TOKEN = login()
        HEADERS["Authorization"] = f"Bearer {TOKEN}"
    elif not TOKEN:
        print("[提示] 未提供 TOKEN 也未提供 USERNAME/PASSWORD，将不带鉴权压测（接口可能返回 210）。")

    per_ep = THREADS * REQUESTS_PER_THREAD
    tasks = []
    for ep in ENDPOINTS:
        tasks += [ep] * per_ep
    total = len(tasks)

    print(f"目标: {BASE_URL}")
    print(f"并发线程={THREADS}  每接口请求={per_ep}  接口数={len(ENDPOINTS)}  总请求={total}")

    t_start = time.perf_counter()
    with cf.ThreadPoolExecutor(max_workers=THREADS) as ex:
        list(ex.map(hit, tasks))
    elapsed = time.perf_counter() - t_start
    analyze(elapsed)


if __name__ == "__main__":
    main()
