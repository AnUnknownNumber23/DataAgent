import { createApp } from 'vue';
import App from '@/App.vue';
import router from '@/router';
import axios from 'axios';

// 引入全局样式
import '@/styles/global.css';
import 'element-plus/dist/index.css';
import ElementPlus from 'element-plus';

// Axios 拦截器 - 自动带 JWT Token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Axios 拦截器 - 401 自动跳登录
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      router.push('/login');
    }
    return Promise.reject(error);
  }
);

// 路由守卫 - 未登录跳登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  if (!to.meta.noAuth && !token) {
    next('/login');
  } else {
    next();
  }
});

// 创建应用实例
const app = createApp(App);
app.use(router);
app.use(ElementPlus);
app.mount('#app');
