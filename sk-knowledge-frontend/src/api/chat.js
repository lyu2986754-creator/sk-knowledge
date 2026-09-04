import { post,get } from '@/utils/request';

// 发送聊天消息
export function chat(params) {
  return post('/chat/completions', params);
}

// 查询窗口历史信息
export function getChatWindow(params) {
  return get('/chat/window', params);
}

// 查询窗口历史信息
export function getChatMessage(params) {
  return get('/chat/completions', params);
}
