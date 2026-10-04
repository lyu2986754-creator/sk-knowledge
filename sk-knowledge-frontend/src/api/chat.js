import { post,get } from '@/utils/request';

// 发送聊天消息
// baseURL 可选：Agentic 模式走另一个服务，其接口形态与 Java 完全一致，
// 因此只需覆盖 baseURL，其余代码（两步式调用、EventSource）都不用改。
export function chat(params, baseURL) {
  return post('/chat/completions', params, baseURL ? { baseURL } : {});
}

// 查询窗口历史信息
export function getChatWindow(params) {
  return get('/chat/window', params);
}

// 查询窗口历史信息
export function getChatMessage(params) {
  return get('/chat/completions', params);
}
