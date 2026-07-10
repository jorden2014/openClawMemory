<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" style="transition: width 0.3s; background-color: #304156">
      <div style="height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: bold">
        <span v-if="!isCollapse">📧 邮件系统</span>
        <span v-else>📧</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/contacts">
          <el-icon><User /></el-icon>
          <template #title>通讯录</template>
        </el-menu-item>
        <el-menu-item index="/templates">
          <el-icon><Document /></el-icon>
          <template #title>模板管理</template>
        </el-menu-item>
        <el-menu-item index="/compose">
          <el-icon><Promotion /></el-icon>
          <template #title>新建邮件</template>
        </el-menu-item>
        <el-menu-item index="/records">
          <el-icon><List /></el-icon>
          <template #title>发送记录</template>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e6e6e6">
        <div style="display: flex; align-items: center">
          <el-icon style="font-size: 20px; cursor: pointer" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span style="margin-left: 16px; font-size: 16px">{{ currentTitle }}</span>
        </div>
        <div style="display: flex; align-items: center; gap: 16px">
          <span style="color: #666">{{ userStore.username }}</span>
          <el-button type="danger" text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main style="background-color: #f5f5f5; overflow-y: auto">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { User, Document, Promotion, List, Setting, Fold, Expand } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta.title as string) || '')

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>
