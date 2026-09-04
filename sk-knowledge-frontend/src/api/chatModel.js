import { get, post, put, del } from '@/utils/request';

// 获取聊天模型分页列表
export function getChatModelPage(params) {
  return get('/chat/model', params);
}

export function getChatModels(param) {
  return get('/chat/models', param);
}

// 获取单个聊天模型详情
export function getChatModelDetail(id) {
  return get(`/chat/model/${id}`);
}

// 创建聊天模型
export function createChatModel(data) {
  return post('/chat/model', data);
}

// 更新聊天模型
export function updateChatModel(data) {
  return put(`/chat/model`, data);
}

// 删除聊天模型
export function deleteChatModel(id) {
  return del(`/chat/model/${id}`);
}
