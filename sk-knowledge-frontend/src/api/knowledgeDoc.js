import { get, del,put } from '@/utils/request';



// 查询文档
export function getKnowledgeDocPage(params) {
  return get('/knowledge/doc', params);
}

// 回收站文档
export function getTrashPage(params) {
  return get('/knowledge/trash', params);
}


// 删除文档
export function deleteDoc(id) {
  return del(`/knowledge/doc/${id}`);
}

// 恢复文档
export function recoverDoc(id) {
  return put(`/knowledge/trash/${id}`);
}

