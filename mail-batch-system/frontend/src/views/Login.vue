<template>
  <div style="max-width: 400px; margin: 100px auto; padding: 20px; border: 1px solid #dcdfe6; border-radius: 8px;">
    <h2 style="text-align: center; margin-bottom: 30px;">Ellen专属邮件发送系统</h2>
    <div style="margin-bottom: 20px;">
      <label style="display: block; margin-bottom: 8px;">用户名</label>
      <input 
        v-model="form.username" 
        placeholder="请输入用户名" 
        style="width: 100%; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px;"
      />
    </div>
    <div style="margin-bottom: 20px;">
      <label style="display: block; margin-bottom: 8px;">密码</label>
      <input 
        type="password" 
        v-model="form.password" 
        placeholder="请输入密码" 
        style="width: 100%; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px;"
        @keyup.enter="handleLogin"
      />
    </div>
    <el-button 
      type="primary" 
      :loading="loading" 
      style="width: 100%" 
      @click="handleLogin"
    >
      登 录
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { login } from '../api/auth';
import { useUserStore } from '../stores/user';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);

const form = reactive({
  username: '',
  password: '',
});

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }

  loading.value = true;
  try {
    // 调用登录接口（Session 认证）
    const res = await login({
      username: form.username,
      password: form.password,
    });

    // 登录成功，存储用户信息（不需要存 Token）
    userStore.setUser({
      id: res.data.userId,
      username: res.data.username,
      role: res.data.role,
    });

    // Session 认证：浏览器会自动保存 JSESSIONID Cookie
    ElMessage.success('登录成功');
    
    // 跳转到首页
    router.push('/');
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败');
  } finally {
    loading.value = false;
  }
}
</script>
