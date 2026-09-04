# sk-knowledge 企业知识库

基于 **Vue3 + SpringBoot + SpringAI + Weaviate + Temporal** 的企业级知识库系统，支持文档上传、语义切块、向量检索与 RAG 对话。

> ⚠️ 本项目为课程/演示项目。仓库**不包含任何真实密钥**：后端 `src/main/resources/application-dev.yml` 已被 `.gitignore` 忽略，请复制 `application-dev.yml.example` 并按本地环境填写后使用。

## 目录结构

```
.
├── sk-knowledge/            # 后端 (SpringBoot + SpringAI + Temporal + Weaviate)
│   ├── src/
│   ├── script/              # 数据库建表 SQL / docker-compose 编排
│   └── pom.xml
├── sk-knowledge-frontend/  # 前端 (Vue3 + Vite)
│   ├── src/
│   └── package.json
├── 架构图.drawio            # 系统架构图 (draw.io 源文件)
└── 架构图.png              # 架构图预览
```

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue3 / Vite / Axios / Naive UI |
| 网关 | Spring Security + JWT |
| 业务 | SpringBoot / MyBatis-Plus / MapStruct |
| 编排 | Temporal（文档处理工作流 + Saga 补偿） |
| AI | Spring AI（ChatClient / Embedding / Rerank） |
| 存储 | MySQL 8.4 / Redis 7 / MinIO / MongoDB 8 / Weaviate 1.33 |

## 快速开始

### 后端
```bash
cd sk-knowledge
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
# 编辑 application-dev.yml 填入数据库 / Redis / MinIO / LLM API Key 等
./mvnw spring-boot:run
```

### 前端
```bash
cd sk-knowledge-frontend
npm install
npm run dev
```

### 依赖中间件（可选）
```bash
cd sk-knowledge/script/docker
docker-compose up -d   # MySQL / Redis / MinIO / Weaviate / Temporal / MongoDB
```

## 架构图

参见根目录 `架构图.png`（由 `架构图.drawio` 编辑生成）。
