<template>
  <div style="padding: 20px;">
    <h2>系统设置</h2>
    
    <!-- 发件人配置 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div>发件人配置</div>
      </template>
      
      <el-form :model="form" label-width="120px">
        <el-form-item label="SMTP 主机">
          <el-input v-model="form.smtpHost" placeholder="smtp.qq.com" />
        </el-form-item>
        <el-form-item label="SMTP 端口">
          <el-input v-model="form.smtpPort" placeholder="587" />
        </el-form-item>
        <el-form-item label="发件邮箱">
          <el-input v-model="form.mailUsername" placeholder="492203171@qq.com" />
        </el-form-item>
        <el-form-item label="邮箱授权码">
          <el-input v-model="form.mailPassword" type="password" placeholder="QQ邮箱授权码" />
        </el-form-item>
        <el-form-item label="是否启用 TLS">
          <el-switch v-model="form.mailStartTls" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存配置</el-button>
          <el-button @click="handleTest" :loading="testing">测试连接</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMailConfig, updateMailConfig, testMailConfig } from '../api/config'

const form = ref({
  smtpHost: '',
  smtpPort: '587',
  mailUsername: '',
  mailPassword: '',
  mailStartTls: true
})

const saving = ref(false)
const testing = ref(false)

// 加载配置
const loadConfig = async () => {
  try {
    const res = await getMailConfig()
    if (res.data) {
      form.value = res.data
    }
  } catch (e) {
    ElMessage.error('加载配置失败')
  }
}

// 保存配置
const handleSave = async () => {
  saving.value = true
  try {
    await updateMailConfig(form.value)
    ElMessage.success('配置已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 测试连接
const handleTest = async () => {
  testing.value = true
  try {
    await testMailConfig(form.value)
    ElMessage.success('连接测试成功！')
  } catch (e) {
    ElMessage.error('连接测试失败')
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>
