import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';
import { useUserStore } from '../stores/user';

const instance = axios.create({
  baseURL: '/api',
  timeout: 120000,
});

instance.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

instance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore();
      userStore.logout();
      router.push('/login');
    }
    return Promise.reject(error);
  }
);

export default instance;
