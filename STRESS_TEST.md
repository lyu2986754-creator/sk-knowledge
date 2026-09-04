# 压力测试报告 / Stress Test Report — sk-knowledge

> 对应学长评审建议第 4 点："一定要做压力测试，至少测主要的三四个接口，QPS、延迟之类的。"

## 1. 测试环境

| 项 | 内容 |
|----|------|
| 被测系统 | sk-knowledge 企业知识库（SpringBoot + SpringAI + Weaviate + MySQL + Redis） |
| 部署地址 | 本地 `http://localhost:8082` |
| 测试工具 | `stress_test.py`（Python 标准库，零第三方依赖，ThreadPoolExecutor 并发） |
| 鉴权方式 | 先 `POST /api/login` 获取 JWT，注入 `Authorization: Bearer <token>` 头 |
| 中间件 | MySQL / Redis / MinIO / Weaviate / Temporal（Docker 容器） |

## 2. 被测接口（核心 4 个）

| # | 接口 | 说明 |
|---|------|------|
| 1 | `GET  /api/knowledge/base` | 知识库列表查询 |
| 2 | `GET  /api/vector/db` | 向量库状态查询 |
| 3 | `GET  /api/chat/window` | 对话窗口查询 |
| 4 | `POST /api/chat/completions` | 对话补全（仅创建会话，不调用 LLM） |

## 3. 测试方法

梯度并发压测：并发线程数 `10 / 50 / 100`，每个接口 `20 请求/线程`，记录 QPS、平均/ P90 / P99 / 最大延迟、错误率。

## 4. 测试结果

| 并发数 | 系统总 QPS | 知识库列表(avg) | 向量库(avg) | 对话窗口(avg / p99 / max) | 对话补全(avg) | 错误率 |
|--------|-----------|----------------|------------|--------------------------|--------------|--------|
| **10**  | 77.5  | 164 ms  | 155 ms | 156 ms / 784 ms / 882 ms | 40 ms  | **0%** |
| **50**  | 112.8 | 597 ms  | 587 ms | 515 ms / 1.3 s / 1.8 s   | 71 ms  | **0%** |
| **100** | 101.3 | 884 ms  | 1740 ms| 1223 ms / 4.6 s / 6.5 s  | 98 ms  | **0%** |

## 5. 结论

- **最优并发约 50**：并发从 50 提升到 100 时，系统总 QPS 不升反降（112 → 101），说明已触及吞吐天花板。
- **性能瓶颈**在 *向量库查询* 与 *对话窗口查询*（底层 Weaviate + 数据库串行调用）；对话补全接口很轻量（40~98 ms），不是瓶颈。
- **稳定性良好**：全程 **0 错误**（HTTP 200），系统"压不垮"，但高并发下响应延迟显著上升，属于容量不足而非崩溃。
- **优化方向**：为热点查询（知识库列表 / 对话窗口）引入 Redis 缓存、调优数据库连接池、耗时长查询异步化。

## 6. 复现方式

```bash
# 自动登录 + 梯度压测（需提供有效账号）
BASE_URL=http://localhost:8082 USERNAME=<账号> PASSWORD=<密码> \
  python stress_test.py --threads 50 --reqs 20

# 不带鉴权（接口会返回 210 参数校验异常，仅测边缘吞吐）
BASE_URL=http://localhost:8082 python stress_test.py --threads 10 --reqs 20
```

详细数据见 `stress_report_t10.json` / `stress_report_t50.json` / `stress_report_t100.json`。
