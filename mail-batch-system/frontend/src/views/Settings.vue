<template>
  <div style="padding: 20px;">
    <h2>系统设置</h2>
    
    <!-- 常见邮箱配置说明 -->
    <el-card style="margin-top: 20px;" type="info">
      <template #header>
        <div>📋 常见邮箱 SMTP 配置参考</div>
      </template>
      <el-table :data="mailPresets" border size="small" style="width: 100%">
        <el-table-column prop="name" label="邮箱类型" width="140" />
        <el-table-column prop="host" label="SMTP 主机" />
        <el-table-column prop="port" label="端口" width="100" />
        <el-table-column prop="ssl" label="SSL" width="70" />
        <el-table-column prop="starttls" label="STARTTLS" width="90" />
        <el-table-column prop="note" label="说明" />
      </el-table>
      <el-alert type="warning" :closable="false" style="margin-top: 12px;">
        <template #title>⚠️ 注意事项</template>
        <ul style="margin: 0; padding-left: 20px;">
          <li><b>QQ 邮箱 / 腾讯企业邮箱：</b>密码栏填的是「授权码」，不是登录密码</li>
          <li>授权码获取：登录邮箱 → 设置 → 账户 → 开启 SMTP/POP3/IMAP → 生成授权码</li>
          <li><b>发件人邮箱</b>必须与认证邮箱一致，否则会报 <code>501 Mail from address must be same as authorization user</code></li>
          <li>企业邮箱若开启安全登录，需使用「客户端专用密码」</li>
        </ul>
      </el-alert>
    </el-card>

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
          <el-select v-model="form.smtpPort" style="width: 100%">
            <el-option label="587 (STARTTLS)" :value="587" />
            <el-option label="465 (SSL)" :value="465" />
          </el-select>
        </el-form-item>
        <el-form-item label="发件邮箱">
          <el-input v-model="form.smtpUsername" placeholder="support@yourcompany.com" />
        </el-form-item>
        <el-form-item label="邮箱授权码">
          <el-input v-model="form.smtpPassword" type="password" placeholder="邮箱密码或授权码" show-password />
        </el-form-item>
        <el-form-item label="SSL 启用">
          <el-switch v-model="form.smtpSslEnable" />
        </el-form-item>
        <el-form-item label="STARTTLS 启用">
          <el-switch v-model="form.smtpStartTlsEnable" />
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

const mailPresets = ref([
  { name: '📧 QQ 邮箱', host: 'smtp.qq.com', port: '587 / 465', ssl: '465 时开', starttls: '587 时开', note: '授权码填密码栏' },
  { name: '🏢 腾讯企业邮箱', host: 'smtp.exmail.qq.com', port: '587 / 465', ssl: '465 时开', starttls: '587 时开', note: '用客户端专用密码' },
  { name: '📨 网易 163', host: 'smtp.163.com', port: '465', ssl: '✅', starttls: '❌', note: '授权码填密码栏' },
  { name: '📮 Gmail', host: 'smtp.gmail.com', port: '587', ssl: '❌', starttls: '✅', note: '需开启不安全应用访问' },
])

const form = ref({
  smtpHost: '',
  smtpPort: 587,
  smtpUsername: '',
  smtpPassword: '',
  smtpSslEnable: false,
  smtpStartTlsEnable: true
})

const saving = ref(false)
const testing = ref(false)

// 加载配置
const loadConfig = async () => {
  try {
    const res = await getMailConfig()
    if (res.data) {
      const d = res.data
      form.value = {
        smtpHost: d.smtpHost || '',
        smtpPort: d.smtpPort || 587,
        smtpUsername: d.smtpUsername || '',
        smtpPassword: '',  // 密码不回填
        smtpSslEnable: d.smtpSslEnable ?? false,
        smtpStartTlsEnable: d.smtpStartTlsEnable ?? true
      }
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
