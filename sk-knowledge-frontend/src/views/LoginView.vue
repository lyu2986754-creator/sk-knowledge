<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-left">
        <div class="login-banner">
          <h1 class="banner-title">sk-knowledge</h1>
          <p class="banner-desc">智能问答 · 知识管理 · 数据分析</p>
          <div class="banner-features">
            <div class="feature-item">
              <n-icon size="24" color="#722ED1">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M12 3a9 9 0 0 0-9 9c0 1.5.4 3 1.1 4.3l-1.5 1.5c-.4.4-.4 1 0 1.4.2.2.4.3.7.3h4.7c.6 0 1-.4 1-1v-4.4c0-.6-.4-1-1-1-.3 0-.5.1-.7.3l-1.3 1.3c-.5-.9-.7-1.9-.7-2.7 0-3.9 3.1-7 7-7s7 3.1 7 7-3.1 7-7 7c-1.5 0-3-.5-4.2-1.4-.4-.3-1.1-.2-1.4.2-.3.4-.2 1.1.2 1.4 1.5 1.1 3.2 1.7 5.1 1.7 5 0 9-4 9-9s-4-9-9-9z" />
                </svg>
              </n-icon>
              <span>智能问答</span>
            </div>
            <div class="feature-item">
              <n-icon size="24" color="#13C2C2">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M20 3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2 6h-8v2h8V9zm0 4h-8v2h8v-2zm0-8h-8v2h8V5zM6 7h2v2H6V7zm0 4h2v2H6v-2zm0 4h2v2H6v-2z" />
                </svg>
              </n-icon>
              <span>知识管理</span>
            </div>
            <div class="feature-item">
              <n-icon size="24" color="#fa8c16">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-8 14H7v-2h4v2zm0-4H7v-2h4v2zm0-4H7V7h4v2zm6 8h-4v-2h4v2zm0-4h-4v-2h4v2zm0-4h-4V7h4v2z" />
                </svg>
              </n-icon>
              <span>数据分析</span>
            </div>
          </div>
        </div>
      </div>
      <div class="login-right">
        <n-card class="login-card" :bordered="false">
          <div class="login-header">
            <h2 class="login-title">欢迎登录</h2>
            <p class="login-subtitle">企业知识库</p>
          </div>

          <n-form ref="formRef" :model="formValue" :rules="rules" size="large" @keyup.enter="handleSubmit"
            class="login-form">
            <n-form-item path="username" class="form-item">
              <n-input v-model:value="formValue.username" placeholder="请输入用户名" clearable round>
                <template #prefix>
                  <n-icon :component="PersonCircleOutline" />
                </template>
              </n-input>
            </n-form-item>

            <n-form-item path="password" class="form-item">
              <n-input v-model:value="formValue.password" type="password" show-password-on="click" placeholder="请输入密码"
                clearable round>
                <template #prefix>
                  <n-icon :component="LockClosedOutline" />
                </template>
              </n-input>
            </n-form-item>

            <div class="form-actions">
              <n-checkbox v-model:checked="rememberMe">记住我</n-checkbox>
              <a href="#" class="forgot-password">忘记密码？</a>
            </div>

            <n-button type="primary" block size="large" @click="handleSubmit" :loading="loading" round
              class="login-button">
              登 录
            </n-button>
          </n-form>

          <div class="login-footer">
            <p>欢迎使用sk-knowledge</p>
          </div>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NCard, NIcon, NCheckbox } from 'naive-ui'
import { PersonCircleOutline, LockClosedOutline } from '@vicons/ionicons5'
import { login } from '@/api/auth'

const router = useRouter()
const message = useMessage()
const formRef = ref(null)
const loading = ref(false)

const formValue = ref({
  username: '',
  password: ''
});

const rememberMe = ref(false);

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
};

const handleSubmit = (e) => {
  e?.preventDefault()
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      try {
        loading.value = true
        let formData = new FormData()
        formData.append("username", formValue.value.username)
        formData.append("password", formValue.value.password)

        const data = await login(formData)


        // 保存token到localStorage
        localStorage.setItem('token', data)
        // 跳转到首页
        await router.push('/');
        message.success('登录成功')
      } catch (error) {
        console.log(error);

        message.error(error.response?.data?.message || '登录失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
  background-image: linear-gradient(120deg, #f6f9fc 0%, #f8fafc 100%);
  padding: 20px;
}

.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1200px;
  min-height: 700px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: #fff;
  padding: 80px 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
  border-radius: 50%;
}

.login-banner {
  max-width: 480px;
  z-index: 1;
}

.banner-title {
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  line-height: 1.3;
}

.banner-desc {
  font-size: 1.25rem;
  opacity: 0.9;
  margin-bottom: 3rem;
  line-height: 1.6;
}

.banner-features {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 1.1rem;
}

.feature-item .n-icon {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 80px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border: none;
  box-shadow: none;
  background: transparent;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  font-size: 2rem;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.login-subtitle {
  color: #666;
  font-size: 0.95rem;
  margin: 0;
}

.login-form {
  margin-top: 40px;
}

.form-item {
  margin-bottom: 24px;
}

.form-item :deep(.n-form-item-label) {
  padding-bottom: 8px;
  font-size: 0.9rem;
  color: #444;
}

.form-item :deep(.n-input) {
  border-radius: 8px;
  height: 48px;
  font-size: 0.95rem;
}

.form-item :deep(.n-input__input) {
  padding: 12px 16px;
  line-height: 1.5;
  display: flex;
  align-items: center;
  height: 100%;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 0.9rem;
}

.forgot-password {
  color: #1890ff;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-password:hover {
  color: #40a9ff;
}

.login-button {
  height: 48px;
  font-size: 1rem;
  font-weight: 500;
  letter-spacing: 1px;
  margin-top: 10px;
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  color: #666;
  font-size: 0.9rem;
}

.register-link {
  color: #1890ff;
  text-decoration: none;
  margin-left: 4px;
  font-weight: 500;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .login-wrapper {
    flex-direction: column;
    max-width: 500px;
    min-height: auto;
  }

  .login-left {
    padding: 40px 30px;
    text-align: center;
  }

  .login-banner {
    max-width: 100%;
  }

  .banner-title {
    font-size: 2rem;
  }

  .banner-desc {
    font-size: 1.1rem;
    margin-bottom: 2rem;
  }

  .login-right {
    padding: 40px 30px;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-card {
  animation: fadeIn 0.6s ease-out forwards;
}

.feature-item {
  opacity: 0;
  transform: translateX(-20px);
  animation: slideIn 0.5s ease-out forwards;
}

.feature-item:nth-child(1) {
  animation-delay: 0.2s;
}

.feature-item:nth-child(2) {
  animation-delay: 0.4s;
}

.feature-item:nth-child(3) {
  animation-delay: 0.6s;
}

@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
