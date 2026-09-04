import { get, post } from '@/utils/request';

// 获取向量库列表
export function getVectorModels() {
  return get('/vector/db')
}

// 获取嵌入模型列表
export function getEmbeddingModels(type) {
  return get('/chat/models',type)
}

// 创建知识库
export function createKnowledge(data) {
  return post('/knowledge/base', data)
}

// 查询知识库
export function getKnowledgeBase() {
  return get('/knowledge/base')
}

// 删除知识库
export function deleteKnowledgeBase(id) {
  return delete('/knowledge/base', id)
}

