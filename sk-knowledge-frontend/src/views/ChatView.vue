<template>
  <n-layout has-sider>
    <n-layout-sider width="250" class="sidebar" show-trigger="false">
      <div class="sidebar-header">
        <h2>对话历史</h2>
      </div>
      <div class="sidebar-content">
        <n-button block type="primary" @click="createNewSession" style="margin-bottom: 16px;">新对话</n-button>
        <div class="session-list">
          <div v-for="session in chatSessions" :key="session.key" class="session-item"
            :class="{ active: activeSession === session.key }" @click="chatSession(session.key)">
            {{ session.label }}
          </div>
        </div>
      </div>
    </n-layout-sider>

    <n-layout>
      <n-layout-header class="header">
        <div class="header-content">
          <h1>{{ currentSession?.label || '对话' }}</h1>
        </div>
      </n-layout-header>

      <n-layout-content class="content">
        <n-scrollbar ref="messagesContainer" style="height: calc(100vh - 400px);">
          <n-space vertical size="large" style="padding: 16px 0">
            <div v-for="(msg, index) in messages" :key="index" class="chat-message-wrapper"
              :class="msg.role === 'user' ? 'user-message' : 'ai-message'">
              <div class="chat-message-avatar" :style="{
                background: msg.role === 'user' ? 'var(--primary-color)' : '#18A058',
                color: 'white'
              }">
                {{ msg.role === 'user' ? '我' : 'AI' }}
              </div>
              <div class="chat-message-content">
                <div class="chat-message-header">
                  <span class="chat-message-author">{{ msg.role === 'user' ? '我' : 'AI 助手' }}</span>
                  <span class="chat-message-time">{{ dayjs(msg.createTime).fromNow() }}</span>
                </div>
                <div v-html="formatMessage(msg.content)" class="message-content"></div>
              </div>
            </div>
          </n-space>
        </n-scrollbar>

        <div class="input-area">
          <n-input v-model:value="userInput" type="textarea" placeholder="输入您的问题..."
            :autosize="{ minRows: 3, maxRows: 8 }" style="width: 100%; margin-bottom: 12px;"
            @keydown.enter.exact.prevent="sendMessage" />
          <div style="display: flex; justify-content: flex-end; align-items: center; gap: 12px;">
            <span style="white-space: nowrap">模式：</span>
            <n-select v-model:value="mode" :options="modeOptions" style="width: 140px;">
            </n-select>
            <span style="white-space: nowrap">模型：</span>
            <n-select v-model:value="selectedModelId" :options="modelOptions" placeholder="选择模型" style="width: 170px;">
            </n-select>
            <span style="white-space: nowrap">知识库：</span>
            <n-select v-model:value="knowledgeId" :options="knowledgeOptions" placeholder="选择知识库" style="width: 170px;">
            </n-select>
            <n-button type="primary" @click="sendMessage" :loading="isLoading" style="height: 40px;">
              发送
            </n-button>
          </div>
        </div>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, reactive, watch } from 'vue';
import { NButton, NInput, NScrollbar, NLayout, NLayoutSider, NLayoutContent, NLayoutHeader, NSpace, useMessage, NSelect } from 'naive-ui';
import { chat } from '@/api/chat';
import { getChatModels } from '@/api/chatModel';
import { getKnowledgeBase } from '@/api/knowledgeBase';
import { getChatWindow, getChatMessage } from '@/api/chat';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

dayjs.locale('zh-cn');
dayjs.extend(relativeTime);

//提示信息
const message = useMessage();

// 聊天会话
const chatSessions = ref([{}]);

const activeSession = ref();
const currentSession = computed(() => {
  return chatSessions.value.find(session => session.key === activeSession.value);
});

// 消息列表
const messages = ref([]);

const userInput = ref('');
const isLoading = ref(false);
const messagesContainer = ref(null);

// 模型相关
const modelOptions = ref([]);
const selectedModelId = ref('');

// 模式切换：朴素 RAG（直连 Java）与 Agentic RAG（经 Python 编排服务）。
// 两个服务的接口形态完全一致，所以只需要切 baseURL——两步式调用与
// EventSource 全部复用。朴素模式**不经过** Python，因此没有代理开销，
// 两种模式的延迟可以直接比较。
const MODE_STORAGE_KEY = 'rag_mode';
const API_BASES = {
  naive: import.meta.env.VITE_API_BASE_URL,
  agentic: import.meta.env.VITE_AGENTIC_API_BASE_URL
};
const modeOptions = [
  { label: '朴素 RAG', value: 'naive' },
  { label: 'Agentic RAG', value: 'agentic' }
];
const mode = ref(localStorage.getItem(MODE_STORAGE_KEY) || 'naive');
watch(mode, (value) => localStorage.setItem(MODE_STORAGE_KEY, value));
const apiBase = () => API_BASES[mode.value] || API_BASES.naive;

// 格式化消息内容
const formatMessage = (content) => {
  if (!content) return '';
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>');
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTo({ top: messagesContainer.value.scrollHeight, behavior: 'smooth' });
    }
  });
};

//切换session 
const chatSession = async (key) => {
  if (activeSession.value === key) {
    return;
  }
  activeSession.value = key;
  clearMessages();

  try {
    const data = await getChatMessage({ windowId: key });
    
    // 使用 reactive 转换每个消息对象
    messages.value = data.map(item => reactive({
      id: item.id,
      windowId: item.windowId,
      knowledgeId: item.knowledgeId,
      modelId: item.modelId,
      role: item.role,
      content: item.content,
      createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss')
    }));
  } catch (error) {
    console.error('查询窗口历史消息失败:', error);
  }
}

// 获取模型列表
const loadModelList = async () => {
  try {
    const chatModels = await getChatModels({ type: 'chat' });

    modelOptions.value = chatModels.map(item => ({
      label: item.name,
      value: item.id
    }));
    // 设置默认选中第一个模型
    if (modelOptions.value.length > 0) {
      selectedModelId.value = modelOptions.value[0].value;
    }
  } catch (error) {
    console.error('加载模型列表失败:', error);
  }
};

// 知识库相关
const knowledgeOptions = ref([]);

// 获取知识库列表
const loadKnowledgeList = async () => {
  try {
    const knowledgeBases = await getKnowledgeBase();

    knowledgeOptions.value = knowledgeBases.map(item => ({
      label: item.knowledgeName,
      value: item.id
    }));
  } catch (error) {
    console.error('加载知识库列表失败:', error);
  }
};

//查询窗口历史消息
const queryWindowMessages = async () => {
  try {
    const data = await getChatWindow();
    chatSessions.value = data.map(item => ({
      key: item.id,
      label: item.tittle
    }));
  } catch (error) {
    console.error('查询窗口历史消息失败:', error);
  }
}

const id = ref()
const windowId = ref()
const knowledgeId = ref()
const modelId = ref()
const content = ref('') // 使用 const 声明响应式 ref

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim()) return;

  // 知识库是必选项。未选择时知识库 id 会拼进 SSE 的 URL 成为空段，
  // 后端异步任务失败但 SSE 已经返回 200，前端不会收到任何事件，
  // 表现为"点了发送没反应且不报错"。这里直接拦下。
  if (!knowledgeId.value) {
    message.warning('请先选择知识库');
    return;
  }

  isLoading.value = true;

  try {
    
    // 添加用户消息 - 使用 reactive 创建响应式消息对象
    const userMessage = reactive({
      id: id.value,
      windowId: windowId.value,
      knowledgeId: knowledgeId.value,
      modelId: selectedModelId.value || modelId.value,
      role: 'user',
      content: userInput.value,
      createTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
    });
    messages.value.push(userMessage);

    userInput.value = '';

    scrollToBottom();

    const length = messages.value.length
    const lastMessage = messages.value[length - 1]
    //将最后的提问信息发送给后端
    const base = apiBase()
    const sessionId = await chat(lastMessage, base)

    let eventSource = new EventSource(
      `${base}/chat/stream/${sessionId}/${knowledgeId.value}`
    );
    
    // 存储 AI 回复消息对象的引用
    let aiMessage = null;

    // 标记是否已正常收到 done。后端在 done 之后会关闭连接，
    // 浏览器可能因此触发 error —— 那不是失败，不能误报。
    let finished = false;

    eventSource.onmessage = (e) => console.log(e.data);
    eventSource.addEventListener("chatContextIdVo", (e) => {
      //将后端返回的json字符串转换为对象
      const chatContextIdVo = JSON.parse(e.data);
      //将提问的id赋值给lastMessage.id
      lastMessage.id = chatContextIdVo.userQuestionId

      if (windowId.value != chatContextIdVo.chatWindowId) {
        windowId.value = chatContextIdVo.chatWindowId

        if (chatSessions.value.length > 0 && chatSessions.value[0].key == '1') {
          chatSessions.value[0].label = chatContextIdVo.chatWindowTittle
          chatSessions.value[0].key = chatContextIdVo.chatWindowId
        } else {
          chatSessions.value.unshift({
            key: windowId.value,
            label: chatContextIdVo.chatWindowTittle
          })
        }
        activeSession.value = windowId.value
      }

      // 创建 AI 消息并保存引用 - 使用 reactive 创建响应式消息对象
      aiMessage = reactive({
        id: chatContextIdVo.assistantId,
        windowId: windowId.value,
        knowledgeId: knowledgeId.value,
        modelId: modelId.value,
        role: 'assistant',
        content: content.value || '', // 确保初始值不为空
        createTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
      });
      messages.value.push(aiMessage);
      isLoading.value = false;
      scrollToBottom();

    });
    
    eventSource.addEventListener("content", (e) => { 
      // 更新响应式的 content ref
      content.value += e.data;
      // 同时更新 AI 消息对象的 content，这样页面上就能实时显示
      
      if (aiMessage) {
        aiMessage.content = content.value;
        // 强制触发视图更新
        messages.value = [...messages.value];
        scrollToBottom(); // 每次更新后滚动到底部
        
      }

    });
   
    eventSource.addEventListener("done", (e) => {
      finished = true
      content.value = ''
      eventSource.close();
    });
    eventSource.onerror = (e) => {
      eventSource.close();
      if (finished) return;
      // 后端在异步任务里失败时，SSE 已经返回 200 但不会推送任何事件，
      // 界面会停在"没反应"的状态。必须显式提示并结束 loading。
      console.error("SSE error:", e);
      message.error('回复失败，请检查模型配置或稍后重试');
      isLoading.value = false;
    };

  } catch (error) {
    isLoading.value = false;
    console.log(error);
    
    message.error('发送消息失败');
  } finally {
    isLoading.value = false;
  }

};


// 清空消息
const clearMessages = () => {
  messages.value = [];
};

// 创建新会话
const createNewSession = () => {
  const newKey = '1';
  if (activeSession.value == newKey) {
    return;
  }
  chatSessions.value.unshift({ label: '新对话', key: newKey });
  activeSession.value = newKey;

  id.value = ''
  windowId.value = ''
  knowledgeId.value = ''

  clearMessages();
};

onMounted(() => {
  scrollToBottom();
  loadModelList();
  loadKnowledgeList();
  queryWindowMessages();
});
</script>

<style scoped>
.sidebar {
  background: #f8f8f8;
  border-right: 1px solid #e0e0e0;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.sidebar-content {
  padding: 16px;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.session-item:hover {
  background: #e8e8e8;
}

.session-item.active {
  background: #e6f4ff;
  color: var(--primary-color);
  font-weight: 500;
}

.header {
  background: white;
  border-bottom: 1px solid #e0e0e0;
}

.header-content {
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
}

.header-content h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.content {
  padding: 0 24px 24px;
  background: #f5f5f5;
}

.input-area {
  margin-top: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-content {
  line-height: 1.6;
  font-size: 14px;
}

.message-content code {
  background: #f0f0f0;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', Courier, monospace;
}

/* 自定义聊天消息样式 */
.chat-message-wrapper {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.user-message {
  flex-direction: row-reverse;
}

/* 移除ai-message的row-reverse，使其默认使用row布局显示在左边 */
.ai-message {
  /* flex-direction: row-reverse; */
}

/* 为user-message添加文本右对齐样式 */
.chat-message-wrapper.user-message .chat-message-content {
  text-align: right;
}

.chat-message-wrapper.user-message .chat-message-header {
  justify-content: flex-end;
  gap: 12px;
}

.chat-message-wrapper.user-message .chat-message-time {
  order: -1;
}

/* 移除ai-message的特殊样式，使其保持默认的左对齐 */
.chat-message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  font-size: 14px;
  flex-shrink: 0;
}

.chat-message-content {
  flex: 1;
  max-width: calc(100% - 100px);
}

.chat-message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}

.chat-message-author {
  font-weight: 500;
  color: #333;
}

.chat-message-footer {
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 200px;
  }

  .chat-message-content {
    max-width: calc(100% - 80px);
  }
}
</style>
