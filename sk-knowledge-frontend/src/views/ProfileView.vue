<template>
  <div class="profile-container">
    <n-card title="个人中心" :bordered="false">
      <n-grid cols="1 s:1 m:2 l:3" :x-gap="24" :y-gap="24">
        <!-- 用户信息 -->
        <n-gi>
          <n-card title="基本信息" size="small" hoverable>
            <n-form
              :model="userInfo"
              :rules="rules"
              ref="formRef"
              label-placement="left"
              label-width="auto"
              require-mark-placement="right-hanging"
            >
              <n-form-item label="头像" path="avatar">
                <n-upload
                  action="#"
                  :show-file-list="false"
                  :custom-request="handleAvatarUpload"
                >
                  <n-avatar
                    round
                    :size="80"
                    :src="userInfo.avatar"
                    style="cursor: pointer"
                    class="avatar-upload"
                  >
                    {{ userInfo.name?.charAt(0) || 'U' }}
                  </n-avatar>
                </n-upload>
              </n-form-item>
              
              <n-form-item label="用户名" path="name">
                <n-input v-model:value="userInfo.name" placeholder="请输入用户名" />
              </n-form-item>
              
              <n-form-item label="邮箱" path="email">
                <n-input v-model:value="userInfo.email" placeholder="请输入邮箱" />
              </n-form-item>
              
              <n-form-item label="手机号" path="phone">
                <n-input v-model:value="userInfo.phone" placeholder="请输入手机号" />
              </n-form-item>
              
              <n-form-item>
                <n-button type="primary" @click="handleSubmit" :loading="submitting">
                  保存修改
                </n-button>
              </n-form-item>
            </n-form>
          </n-card>
        </n-gi>
        
        <!-- 安全设置 -->
        <n-gi>
          <n-card title="安全设置" size="small" hoverable>
            <n-list>
              <n-list-item>
                <n-thing title="账户密码">
                  <template #description>
                    <n-text depth="3">当前密码强度：强</n-text>
                  </template>
                </n-thing>
                <template #suffix>
                  <n-button text type="primary" @click="showPasswordModal = true">
                    修改
                  </n-button>
                </template>
              </n-list-item>
              
              <n-list-item>
                <n-thing title="密保问题">
                  <template #description>
                    <n-text depth="3">未设置密保问题</n-text>
                  </template>
                </n-thing>
                <template #suffix>
                  <n-button text type="primary">
                    设置
                  </n-button>
                </template>
              </n-list-item>
              
              <n-list-item>
                <n-thing title="绑定手机">
                  <template #description>
                    <n-text depth="3">已绑定手机：{{ userInfo.phone || '未绑定' }}</n-text>
                  </template>
                </n-thing>
                <template #suffix>
                  <n-button text type="primary" @click="showPhoneModal = true">
                    {{ userInfo.phone ? '修改' : '绑定' }}
                  </n-button>
                </template>
              </n-list-item>
              
              <n-list-item>
                <n-thing title="绑定邮箱">
                  <template #description>
                    <n-text depth="3">已绑定邮箱：{{ userInfo.email || '未绑定' }}</n-text>
                  </template>
                </n-thing>
                <template #suffix>
                  <n-button text type="primary" @click="showEmailModal = true">
                    {{ userInfo.email ? '修改' : '绑定' }}
                  </n-button>
                </template>
              </n-list-item>
            </n-list>
          </n-card>
        </n-gi>
        
        <!-- 账号绑定 -->
        <n-gi>
          <n-card title="账号绑定" size="small" hoverable>
            <n-list>
              <n-list-item>
                <n-thing>
                  <template #avatar>
                    <n-icon size="24" color="#1DA1F2">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M23.953 4.57a10 10 0 01-2.825.775 4.958 4.958 0 002.163-2.723c-.951.555-2.005.959-3.127 1.184a4.92 4.92 0 00-8.384 4.482C7.69 8.095 4.067 6.13 1.64 3.162a4.822 4.822 0 00-.666 2.475c0 1.71.87 3.213 2.188 4.096a4.904 4.904 0 01-2.228-.616v.06a4.923 4.923 0 003.946 4.827 4.996 4.996 0 01-2.212.085 4.936 4.936 0 004.604 3.417 9.867 9.867 0 01-6.102 2.105c-.39 0-.779-.023-1.17-.067a13.995 13.995 0 007.557 2.209c9.053 0 13.998-7.496 13.998-13.985 0-.21 0-.42-.015-.63A9.935 9.935 0 0024 4.59z"/>
                      </svg>
                    </n-icon>
                  </template>
                  <template #header>Twitter</template>
                  <template #description>
                    <n-text depth="3">未绑定</n-text>
                  </template>
                </n-thing>
                <template #suffix>
                  <n-button text type="primary">
                    绑定
                  </n-button>
                </template>
              </n-list-item>
              
              <n-list-item>
                <n-thing>
                  <template #avatar>
                    <n-icon size="24" color="#1877F2">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M22.675 0H1.325C.593 0 0 .593 0 1.325v21.351C0 23.407.593 24 1.325 24H12.82v-9.294H9.692v-3.622h3.128V8.413c0-3.1 1.893-4.788 4.659-4.788 1.325 0 2.463.099 2.795.143v3.24l-1.918.001c-1.504 0-1.795.715-1.795 1.763v2.313h3.587l-.467 3.622h-3.12V24h6.116c.73 0 1.323-.593 1.323-1.325V1.325C24 .593 23.407 0 22.675 0z"/>
                      </svg>
                    </n-icon>
                  </template>
                  <template #header>Facebook</template>
                  <template #description>
                    <n-text depth="3">未绑定</n-text>
                  </template>
                </n-thing>
                <template #suffix>
                  <n-button text type="primary">
                    绑定
                  </n-button>
                </template>
              </n-list-item>
              
              <n-list-item>
                <n-thing>
                  <template #avatar>
                    <n-icon size="24" color="#000">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                      </svg>
                    </n-icon>
                  </template>
                  <template #header>GitHub</template>
                  <template #description>
                    <n-text depth="3">未绑定</n-text>
                  </template>
                </n-thing>
                <template #suffix>
                  <n-button text type="primary">
                    绑定
                  </n-button>
                </template>
              </n-list-item>
            </n-list>
          </n-card>
        </n-gi>
      </n-grid>
    </n-card>

    <!-- 修改密码弹窗 -->
    <n-modal v-model:show="showPasswordModal" title="修改密码" preset="dialog">
      <n-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
        <n-form-item label="当前密码" path="currentPassword">
          <n-input
            v-model:value="passwordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item label="新密码" path="newPassword">
          <n-input
            v-model:value="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password-on="click"
          />
        </n-form-item>
        <n-form-item label="确认新密码" path="confirmPassword">
          <n-input
            v-model:value="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password-on="click"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="showPasswordModal = false">取消</n-button>
          <n-button type="primary" @click="handlePasswordSubmit" :loading="passwordSubmitting">
            确认修改
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useMessage } from 'naive-ui';

const message = useMessage();
const formRef = ref(null);
const passwordFormRef = ref(null);

// 用户信息
const userInfo = ref({
  name: 'Admin',
  email: 'admin@example.com',
  phone: '13800138000',
  avatar: ''
});

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2到20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
};

// 修改密码相关
const showPasswordModal = ref(false);
const passwordSubmitting = ref(false);
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const validatePassword = (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入密码'));
  }
  if (value.length < 6) {
    return callback(new Error('密码长度不能小于6位'));
  }
  callback();
};

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    return callback(new Error('两次输入的密码不一致'));
  }
  callback();
};

const passwordRules = {
  currentPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// 头像上传
const handleAvatarUpload = async ({ file }) => {
  try {

    // 这里应该是上传到服务器的代码
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 模拟返回图片URL
    userInfo.value.avatar = `https://picsum.photos/200/200?t=${Date.now()}`;
    message.success('头像上传成功');
  } catch (error) {
    console.error('上传失败:', error);
    message.error('头像上传失败');
  }
};

// 提交表单
const submitting = ref(false);
const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    submitting.value = true;
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    message.success('保存成功');
  } catch (errors) {
    console.error('验证失败:', errors);
  } finally {
    submitting.value = false;
  }
};

// 提交修改密码
const handlePasswordSubmit = async () => {
  try {
    await passwordFormRef.value?.validate();
    passwordSubmitting.value = true;
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    message.success('密码修改成功');
    showPasswordModal.value = false;
    passwordForm.currentPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
  } catch (errors) {
    console.error('验证失败:', errors);
  } finally {
    passwordSubmitting.value = false;
  }
};

onMounted(() => {
  // 模拟获取用户信息
  fetchUserInfo();
});

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 模拟返回数据
    userInfo.value = {
      name: 'Admin',
      email: 'admin@example.com',
      phone: '13800138000',
      avatar: ''
    };
  } catch (error) {
    console.error('获取用户信息失败:', error);
    message.error('获取用户信息失败');
  }
};
</script>

<style scoped>
.profile-container {
  height: 100%;
}

.avatar-upload {
  transition: all 0.3s;
  border: 2px dashed #eee;
}

.avatar-upload:hover {
  transform: scale(1.05);
  border-color: #722ED1;
}

.n-list-item {
  padding: 12px 0;
}

.n-list-item + .n-list-item {
  border-top: 1px solid #f0f0f0;
}
</style>
