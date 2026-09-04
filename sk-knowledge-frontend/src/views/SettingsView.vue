<template>
  <div class="settings-container">
    <n-card title="系统设置" :bordered="false">
      <n-tabs type="line" animated>
        <!-- 基本设置 -->
        <n-tab-pane name="basic" tab="基本设置">
          <n-form :model="settings" ref="formRef" label-placement="left">
            <n-form-item label="系统名称" path="siteName">
              <n-input v-model:value="settings.siteName" placeholder="请输入系统名称" />
            </n-form-item>
            
            <n-form-item label="系统Logo">
              <n-upload action="#" :show-file-list="false" :custom-request="handleLogoUpload">
                <n-avatar round :size="80" :src="settings.logo" style="cursor: pointer">
                  {{ settings.siteName?.charAt(0) || 'SK' }}
                </n-avatar>
              </n-upload>
            </n-form-item>
            
            <n-form-item label="主题" path="theme">
              <n-radio-group v-model:value="settings.theme">
                <n-radio-button value="light">浅色</n-radio-button>
                <n-radio-button value="dark">深色</n-radio-button>
                <n-radio-button value="system">系统</n-radio-button>
              </n-radio-group>
            </n-form-item>
            
            <n-form-item>
              <n-button type="primary" @click="handleSubmit" :loading="submitting">
                保存设置
              </n-button>
            </n-form-item>
          </n-form>
        </n-tab-pane>
        
        <!-- AI设置 -->
        <n-tab-pane name="ai" tab="AI设置">
          <n-form :model="aiSettings" ref="aiFormRef" label-placement="left">
            <n-form-item label="AI模型" path="model">
              <n-select
                v-model:value="aiSettings.model"
                :options="modelOptions"
                placeholder="请选择AI模型"
              />
            </n-form-item>
            
            <n-form-item label="API Key" path="apiKey">
              <n-input
                v-model:value="aiSettings.apiKey"
                type="password"
                show-password-on="click"
                placeholder="请输入API Key"
              />
            </n-form-item>
            
            <n-form-item>
              <n-space>
                <n-button type="primary" @click="handleAiSubmit" :loading="aiSubmitting">
                  保存设置
                </n-button>
                <n-button @click="testConnection">
                  测试连接
                </n-button>
              </n-space>
            </n-form-item>
          </n-form>
        </n-tab-pane>
        
        <!-- 关于 -->
        <n-tab-pane name="about" tab="关于">
          <n-card size="small">
            <n-space vertical>
              <n-space align="center">
                <n-avatar :size="60" :src="settings.logo">
                  {{ settings.siteName?.charAt(0) || 'SK' }}
                </n-avatar>
                <div>
                  <n-h2 style="margin: 0">{{ settings.siteName || 'SK-Knowledge' }}</n-h2>
                  <n-text depth="3">版本 v1.0.0</n-text>
                </div>
              </n-space>
              
              <n-divider />
              
              <n-text depth="3">
                © 2026 SK-Knowledge 企业知识库
              </n-text>
            </n-space>
          </n-card>
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useMessage } from 'naive-ui';

const message = useMessage();
const formRef = ref(null);
const aiFormRef = ref(null);

// 基本设置
const settings = reactive({
  siteName: 'SK-Knowledge',
  logo: '',
  theme: 'light'
});

// AI设置
const aiSettings = reactive({
  model: 'gpt-3.5-turbo',
  apiKey: ''
});

const modelOptions = [
  { label: 'GPT-3.5 Turbo', value: 'gpt-3.5-turbo' },
  { label: 'GPT-4', value: 'gpt-4' },
  { label: 'Claude 2', value: 'claude-2' },
  { label: '文心一言', value: 'ernie-bot' }
];

// 提交状态
const submitting = ref(false);
const aiSubmitting = ref(false);

// 处理Logo上传
const handleLogoUpload = async () => {
  try {
    // 模拟上传
    await new Promise(resolve => setTimeout(resolve, 1000));
    settings.logo = `https://picsum.photos/200/200?t=${Date.now()}`;
    message.success('Logo上传成功');
  } catch (error) {
    console.error('上传失败:', error);
    message.error('Logo上传失败');
  }
};

// 测试API连接
const testConnection = async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 1500));
    message.success('连接成功，API可用');
  } catch (error) {
    message.error('连接失败，请检查API Key');
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    submitting.value = true;
    await new Promise(resolve => setTimeout(resolve, 1000));
    message.success('设置保存成功');
  } catch (error) {
    console.error('保存失败:', error);
    message.error('保存失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 提交AI设置
const handleAiSubmit = async () => {
  try {
    aiSubmitting.value = true;
    await new Promise(resolve => setTimeout(resolve, 1000));
    message.success('AI设置保存成功');
  } catch (error) {
    console.error('保存失败:', error);
    message.error('保存失败，请重试');
  } finally {
    aiSubmitting.value = false;
  }
};
</script>

<style scoped>
.settings-container {
  height: 100%;
  padding: 16px;
}

.n-form {
  max-width: 600px;
  margin: 0 auto;
}
</style>
