# 部署：被评测对象 sk-knowledge 的依赖环境

本目录为评测平台提供**可复现的依赖环境**。评测平台自身是 Python 服务，但被评测的
RAG 服务 `sk-knowledge` 需要六个中间件才能运行，这里把它们一次性定义好。

## 端口对齐约定

`docker-compose.yml` 的端口**严格对齐**
`sk-knowledge/src/main/resources/application-dev.yml`，因此 Java 侧无需任何改动：

| 服务 | 宿主端口 | 容器端口 |
| --- | --- | --- |
| MySQL | 13306 | 3306 |
| Redis | 16379 | 6379 |
| MongoDB | 27018 | 27017 |
| Weaviate | 18088 | 8080 |
| MinIO | 19000 | 9000（控制台 19001） |
| Temporal | 17233 | 7233（UI 18081） |

> 本机另有原生 MySQL（3306）与 Redis（6379）在运行，端口不冲突、互不影响。
> 之所以不复用它们，是为了让环境可通过 Docker 完整复现。

## 首次启动

```powershell
# 1. 拉取镜像（Docker Hub 在国内不可达，走加速器再重打标签）
.\deploy\pull-images.ps1

# 2. 启动中间件
docker compose -f .\deploy\docker-compose.yml up -d

# 3. 把 API key 注入数据库（密钥只从环境变量读，不进版本库）
.\deploy\seed-api-key.ps1
```

MySQL 首次启动时会自动执行 `initdb/` 下的脚本：

- `00-databases.sql` —— 预建 Temporal 需要的两个库
- `10-sk_knowledge.sql` —— sk-knowledge 的表结构与初始数据（**已脱敏，不含任何密钥**）

## 关于密钥

`10-sk_knowledge.sql` 中 `chat_model.api_key` 全部是占位符 `REPLACE_WITH_ENV_KEY`。
真实密钥通过 `seed-api-key.ps1` 在启动后注入，取值来自 `eval/.env` 的
`SK_LLM_API_KEY`。这样做的好处是：schema 可以安全地公开，密钥永远只存在于本机。

## 注意

若 `docker compose up` 报 `No such image`，说明镜像还没拉全，先跑 `pull-images.ps1`。

