<template>
  <div>
    <el-tabs v-model="activeTab">
      <!-- SMTP 配置 -->
      <el-tab-pane label="邮箱配置" name="smtp">
        <el-card shadow="never" style="max-width: 600px">
          <el-form ref="smtpFormRef" :model="smtpForm" :rules="smtpRules" label-width="120px">
            <el-form-item label="SMTP 主机" prop="host">
              <el-input v-model="smtpForm.host" placeholder="smtp.exmail.qq.com" />
            </el-form-item>
            <el-form-item label="端口" prop="port">
              <el-input-number v-model="smtpForm.port" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="账号" prop="username">
              <el-input v-model="smtpForm.username" placeholder="企业邮箱地址" />
            </el-form-item>
            <el-form-item label="授权码" prop="password">
              <el-input v-model="smtpForm.password" type="password" show-password placeholder="SMTP授权码（非邮箱密码）" />
            </el-form-item>
            <el-form-item label="启用SSL">
              <el-switch v-model="smtpForm.ssl" />
            </el-form-item>
            <el-form-item label="发件人名称" prop="senderName">
              <el-input v-model="smtpForm.senderName" placeholder="如：XX公司" />
            </el-form-item>
            <el-form-item label="邮件签名">
              <el-input v-model="smtpForm.senderSignature" type="textarea" :rows="3" placeholder="可选，邮件末尾签名" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveSmtp">保存配置</el-button>
              <el-button @click="testSmtp" :loading="testing">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 发送参数 -->
      <el-tab-pane label="发送参数" name="params">
        <el-card shadow="never" style="max-width: 600px">
          <el-form ref="paramsFormRef" :model="paramsForm" :rules="paramsRules" label-width="140px">
            <el-form-item label="发送间隔（秒）" prop="intervalSeconds">
              <el-input-number v-model="paramsForm.intervalSeconds" :min="1" :max="60" />
              <span style="margin-left: 8px; color: #999">每封邮件间隔时间</span>
            </el-form-item>
            <el-form-item label="最大重试次数" prop="maxRetryCount">
              <el-input-number v-model="paramsForm.maxRetryCount" :min="0" :max="10" />
            </el-form-item>
            <el-form-item label="单次最大发送数" prop="maxBatchSize">
              <el-input-number v-model="paramsForm.maxBatchSize" :min="1" :max="1000" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveParams">保存参数</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getSmtpConfig, updateSmtpConfig, getSendParams, updateSendParams } from '../api/config'
import type { SmtpConfig, SendParams } from '../utils/types'

const activeTab = ref('smtp')
const saving = ref(false)
const testing = ref(false)

const smtpFormRef = ref<FormInstance>()
const paramsFormRef = ref<FormInstance>()

const smtpForm = reactive<SmtpConfig>({
  host: 'smtp.exmail.qq.com',
  port: 465,
  username: '',
  password: '',
  ssl: true,
  senderName: '',
  senderSignature: '',
})

const paramsForm = reactive<SendParams>({
  intervalSeconds: 3,
  maxRetryCount: 3,
  maxBatchSize: 500,
})

const smtpRules = {
  host: [{ required: true, message: '请输入SMTP主机', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  username: [{ required: true, message: '请输入邮箱账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入授权码', trigger: 'blur' }],
}

const paramsRules = {
  intervalSeconds: [{ required: true, message: '请输入发送间隔', trigger: 'blur' }],
  maxRetryCount: [{ required: true, message: '请输入重试次数', trigger: 'blur' }],
  maxBatchSize: [{ required: true, message: '请输入最大发送数', trigger: 'blur' }],
}

async function loadSmtp() {
  try {
    const res = await getSmtpConfig()
    Object.assign(smtpForm, res.data)
  } catch {}
}

async function loadParams() {
  try {
    const res = await getSendParams()
    Object.assign(paramsForm, res.data)
  } catch {}
}

async function saveSmtp() {
  await smtpFormRef.value?.validate()
  saving.value = true
  try {
    await updateSmtpConfig(smtpForm)
    ElMessage.success('SMTP配置已保存')
  } finally {
    saving.value = false
  }
}

async function testSmtp() {
  testing.value = true
  try {
    await updateSmtpConfig(smtpForm)
    ElMessage.success('连接测试成功')
  } catch {
    ElMessage.error('连接测试失败')
  } finally {
    testing.value = false
  }
}

async function saveParams() {
  await paramsFormRef.value?.validate()
  saving.value = true
  try {
    await updateSendParams(paramsForm)
    ElMessage.success('发送参数已保存')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSmtp()
  loadParams()
})
</script>
