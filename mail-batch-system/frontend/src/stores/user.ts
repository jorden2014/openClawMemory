import { defineStore } from 'pinia';
import { reactive, computed } from 'vue';

interface UserState {
  id: number;
  username: string;
  role: string;
  token: string;
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = reactive<UserState>({
    id: 0,
    username: '',
    role: '',
    token: '',
  });

  // 计算属性：用户名（响应式）
  const username = computed(() => user.username);

  // 判断是否是管理员
  function isAdmin(): boolean {
    return user.role === 'ADMIN';
  }

  // 登录成功后设置用户信息
  function setUser(userInfo: { id: number; username: string; role: string; token?: string }) {
    user.id = userInfo.id;
    user.username = userInfo.username;
    user.role = userInfo.role;
    if (userInfo.token) {
      user.token = userInfo.token;
    }
    localStorage.setItem('user', JSON.stringify(user));
  }

  // 从本地存储恢复用户信息
  function restoreUser() {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        const parsed = JSON.parse(savedUser);
        user.id = parsed.id || 0;
        user.username = parsed.username || '';
        user.role = parsed.role || '';
        user.token = parsed.token || '';
      } catch (e) {
        console.error('恢复用户信息失败:', e);
      }
    }
  }

  // 注销
  function logout() {
    user.id = 0;
    user.username = '';
    user.role = '';
    user.token = '';
    localStorage.removeItem('user');
  }

  return {
    user,
    username,
    isAdmin,
    setUser,
    restoreUser,
    logout,
  };
});
