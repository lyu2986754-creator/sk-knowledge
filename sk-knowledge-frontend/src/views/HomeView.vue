<template>
  <div class="home-container">
    <n-layout has-sider>
      <!-- 侧边栏 -->
      <n-layout-sider bordered collapse-mode="width" :collapsed-width="64" :width="240" :native-scrollbar="false"
        show-trigger="arrow-circle" class="sider">
        <div class="logo">
          <n-icon size="28" color="#722ED1">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z" />
              <path
                d="M12 6c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm0 10c-2.21 0-4-1.79-4-4s1.79-4 4-4 4 1.79 4 4-1.79 4-4 4z" />
            </svg>
          </n-icon>
          <span v-show="!collapsed">SK-Knowledge</span>
        </div>
        <n-menu v-model:value="activeKey" :collapsed="collapsed" :collapsed-width="64" :collapsed-icon-size="22"
          :options="menuOptions" @update:value="handleMenuSelect" />
      </n-layout-sider>

      <n-layout>
        <!-- 顶部导航 -->
        <n-layout-header bordered>
          <div class="header-content">
            <div class="header-left">
              <n-breadcrumb>
                <n-breadcrumb-item v-for="item in breadcrumbs" :key="item.key">
                  {{ item.label }}
                </n-breadcrumb-item>
              </n-breadcrumb>
            </div>
            <div class="header-right">
              <n-dropdown :options="userOptions" @select="handleUserSelect">
                <n-button text>
                  <div class="user-info">
                    <n-avatar round size="small">{{ userInfo.name?.charAt(0) || 'U' }}</n-avatar>
                    <span class="username">{{ userInfo.name || 'User' }}</span>
                  </div>
                </n-button>
              </n-dropdown>
            </div>
          </div>
        </n-layout-header>

        <!-- 内容区域 -->
        <n-layout-content content-style="padding: 20px;" >
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </n-layout-content>
      </n-layout>
    </n-layout>
  </div>
</template>

<script setup>
import { ref, computed, h } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  NLayout,
  NLayoutSider,
  NLayoutHeader,
  NLayoutContent,
  NMenu,
  NIcon,
  NBreadcrumb,
  NBreadcrumbItem,
  NAvatar,
  NButton,
  NDropdown,
  useMessage
} from 'naive-ui';
import {
  ChatbubblesOutline,
  DocumentTextOutline,
  FolderOpenOutline,
  ChatboxEllipsesOutline,
  LogOutOutline,
  PersonCircleOutline,
  SettingsOutline,
  LockClosedOutline
} from '@vicons/ionicons5';
import { logout } from '@/api/auth';

const router = useRouter();
const route = useRoute();
const message = useMessage();

// 菜单状态
const collapsed = ref(false);
const activeKey = ref(route.name);

// 用户信息
const userInfo = ref({
  name: 'Admin',
  avatar: ''
});

// 菜单选项
const menuOptions = [
  {
    label: '对话',
    key: 'chat',
    icon: renderIcon(ChatbubblesOutline)
  },
  {
    label: '对话模型',
    key: 'chatModel', 
    icon: renderIcon(LockClosedOutline) 
  },
  {
    label: '知识库管理',
    key: 'knowledge',
    icon: renderIcon(DocumentTextOutline)
  },
  {
    label: '文件管理',
    key: 'files',
    icon: renderIcon(FolderOpenOutline)
  }
];

// 用户下拉菜单
const userOptions = [
  {
    label: '个人中心',
    key: 'profile',
    icon: renderIcon(PersonCircleOutline)
  },
  {
    label: '设置',
    key: 'settings',
    icon: renderIcon(SettingsOutline)
  },
  {
    type: 'divider',
    key: 'divider'
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: renderIcon(LogOutOutline)
  }
];

// 面包屑
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title);
  return matched.map(item => ({
    key: item.name,
    label: item.meta.title,
    path: item.path
  }));
});

// 渲染图标
function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) });
}

// 菜单选择
function handleMenuSelect(key) {
  router.push({ name: key });
}

// 用户操作
function handleUserSelect(key) {
  if (key === 'logout') {
    handleLogout();
  } else if (key === 'profile') {
    router.push({ name: 'profile' });
  } else if (key === 'settings') {
    router.push({ name: 'settings' });
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await logout();
    localStorage.removeItem('token');
    await router.push('/login');
    message.success('已退出登录');
  } catch (error) {
    message.error('退出登录失败');
  }
};
</script>

<style scoped>
.home-container {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.sider {
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-shadow: 2px 0 8px 0 rgba(29, 35, 41, 0.05);
  z-index: 2;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64px;
  font-size: 18px;
  font-weight: bold;
  color: #333;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 0 16px;
}

.logo .n-icon {
  margin-right: 8px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #fff;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.username {
  margin-left: 8px;
  font-size: 14px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .sider {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 1000;
  }

  .header-content {
    padding: 0 16px;
  }

  .username {
    display: none;
  }
}
</style>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
}

.n-layout-header {
  background: #2080f0;
  color: white;
}

h1 {
  margin: 0;
  color: white;
}
</style>
