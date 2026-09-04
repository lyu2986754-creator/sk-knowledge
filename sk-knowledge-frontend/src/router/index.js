import { createRouter, createWebHistory } from 'vue-router';
import { h } from 'vue';
import { NEmpty } from 'naive-ui';
import LoginView from '@/views/LoginView.vue';
import HomeView from '@/views/HomeView.vue';

// 懒加载组件
const ChatView = () => import('@/views/ChatView.vue');
const ChatModelView = () => import('@/views/ChatModelView.vue');
const KnowledgeView = () => import('@/views/KnowledgeView.vue');
const FilesView = () => import('@/views/FilesView.vue');
const ProfileView = () => import('@/views/ProfileView.vue');
const SettingsView = () => import('@/views/SettingsView.vue');

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    name: 'Home',
    component: HomeView,
    redirect: '/chat',
    meta: { requiresAuth: true, title: '首页' },
    children: [
      { path: 'chat', name: 'chat', component: ChatView, meta: { title: '对话', icon: 'chat' } },
      { path: 'chatModel', name: 'chatModel', component: ChatModelView, meta: { title: '模型管理', icon: 'models' } },
      {
        path: 'knowledge',
        name: 'knowledge',
        component: KnowledgeView,
        meta: { title: '知识库管理', icon: 'knowledge' }
      },
      {
        path: 'files/:knowledgeId?',
        name: 'files',
        component: FilesView,
        meta: { title: '文件管理', icon: 'files' },
        props: true
      },
      {
        path: 'profile',
        name: 'profile',
        component: ProfileView,
        meta: { title: '个人中心', requiresAuth: true, hideInMenu: true }
      },
      {
        path: 'settings',
        name: 'settings',
        component: SettingsView,
        meta: { title: '系统设置', requiresAuth: true, hideInMenu: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: {
      render: () => h(NEmpty, { description: '页面不存在' })
    },
    meta: { requiresAuth: false, title: '404' }
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL || '/'),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  }
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - SK-Knowledge` : 'SK-Knowledge';

  if (to.meta.requiresAuth && !token) {
    if (window.$message) {
      window.$message.warning('请先登录');
    }
    next('/login');
  } else if (to.path === '/login' && token) {
    next('/');
  } else {
    next();
  }
});

export default router;
