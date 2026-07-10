<template>
  <div style="display: flex; gap: 16px; height: calc(100vh - 140px)">
    <!-- 左侧：客户选择 -->
    <el-card shadow="never" style="width: 360px; flex-shrink: 0; display: flex; flex-direction: column">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>选择客户</span>
          <el-tag type="info" size="small">已选 {{ selectedCustomers.length }} 人</el-tag>
        </div>
      </template>

      <!-- 标签筛选 -->
      <el-select v-model="filterTag" placeholder="按标签筛选" clearable size="small" style="margin-bottom: 12px" @change="loadCustomers">
        <el-option v-for="tag in allTags" :key="tag" :label="tag" :value="tag" />
      </el-select>

      <!-- 搜索 -->
      <el-input v-model="customerKeyword" placeholder="搜索客户" size="small" clearable style="margin-bottom: 12px" @clear="loadCustomers" @keyup.enter="loadCustomers" />

      <!-- 全选/反选 -->
      <div style="margin-bottom: 12px">
        <el-button size="small" @click="selectAll">全选</el-button>
        <el-button size="small" @click="invertSelection">反选</el-button>
        <el-button size="small" @click="selectedCustomers = []">清空</el-button>
      </div>

      <!-- 客户列表 -->
      <div style="flex: 1; overflow-y: auto">
        <el-checkbox-group v-model="selectedIds">
          <div v-for="c in customers" :key="c.id" style="padding: 6px 0; border-bottom: 1px solid #f0f0f0">
            <el-checkbox :value="c.id">
              <span>{{ c.salutation }}</span>
              <span style="color: #999; margin-left: 8px; font-size: 12px">{{ c.email }}</span>
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
    </el-card>

    <!-- 右侧：邮件编辑 -->
    <el-card shadow="never" style="flex: 1; display: flex; flex-direction: column; overflow: hidden">
      <template #header><span>编辑邮件</span></template>

      <el-form label-width="100px" style="flex: 1; overflow-y: auto; padding-right: 16px">
        <!-- 选择模板 -->
        <el-form-item label="使用模板">
          <el-select v-model="selectedTemplateId" placeholder="选择已有模板（可选）" clearable @change="applyTemplate" style="width: 100%">
            <el-option v-for="t in templateList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>

        <!-- 邮件主题 -->
        <el-form-item label="邮件主题" required>
          <el-input v-model="mailSubject" placeholder="支持 {{称呼}} 变量">
            <template #append>
              <el-button @click="mailSubject += '{{称呼}}'">插入称呼</el-button>
            </template>
          </el-input>
        </el-form-item>

        <!-- 富文本正文 -->
        <el-form-item label="邮件正文" required>
          <RichTextEditor v-model="mailBody" @insert-salutation="mailBody += '{{称呼}}'" style="height: 300px" />
        </el-form-item>

        <!-- 附件 -->
        <el-form-item label="附件">
          <el-upload :auto-upload="false" :on-change="handleAttachment">
            <el-button size="small">选择文件</el-button>
            <template #tip><div style="color: #999; font-size: 12px">单文件 ≤20MB，总计 ≤50MB</div></template>
          </el-upload>
        </el-form-item>
      </el-form>

      <!-- 底部操作 -->
      <div style="display: flex; justify-content: flex-end; gap: 12px; padding-top: 16px; border-top: 1px solid #eee">
        <el-button @click="showPreview = true">预览</el-button>
        <el-button type="primary" :disabled="!canSend" :loading="sending" @click="handleSend">发送</el-button>
      </div>
    </el-card>

    <!-- 预览弹窗 -->
    <el-dialog v-model="showPreview" title="发送预览" width="800px" destroy-on-close top="5vh">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        共 {{ selectedCustomers.length }} 封邮件，确认无误后点击发送
      </el-alert>
      <el-table :data="selectedCustomers" stripe max-height="500">
        <el-table-column type="index" width="50" />
        <el-table-column prop="salutation" label="称呼" width="100" />
        <el-table-column prop="email" label="邮箱" width="220" />
        <el-table-column label="主题预览">
          <template #default="{ row }">
            {{ mailSubject.replace(/\{\{称呼\}\}/g, row.salutation) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showPreview = false">取消</el-button>
        <el-button type="primary" :loading="sending" @click="handleSend">确认发送</el-button>
      </template>
    </el-dialog>

    <!-- 发送进度弹窗 -->
    <el-dialog v-model="showProgress" title="发送进度" width="500px" :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false">
      <el-progress :percentage="progressPercent" :status="progressStatus" />
      <div style="margin-top: 12px; color: #666">
        总计 {{ progress.total }} 封 | 已发送 {{ progress.sent }} 封 | 失败 {{ progress.failed }} 封 | 待发送 {{ progress.pending }} 封
      </div>
      <template #footer>
        <el-button v-if="progress.status === 'COMPLETED' || progress.status === 'CANCELLED'" @click="handleClose">关闭</el-button>
        <el-button v-else type="danger" @click="handleCancel">取消发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCustomers, getAllTags } from '../api/customer'
import { getAllTemplates } from '../api/template'
import { sendMail, getProgress, cancelSend } from '../api/mail'
import type { Customer, MailTemplate, SendProgress } from '../utils/types'
import RichTextEditor from '../components/RichTextEditor.vue'

const customers = ref<Customer[]>([])
const allTags = ref<string[]>([])
const templateList = ref<MailTemplate[]>([])
const selectedIds = ref<string[]>([])
const selectedCustomers = ref<Customer[]>([])
const filterTag = ref('')
const customerKeyword = ref('')
const selectedTemplateId = ref('')
const mailSubject = ref('')
const mailBody = ref('')

const showPreview = ref(false)
const showProgress = ref(false)
const sending = ref(false)
const progress = ref<SendProgress>({ batchId: '', total: 0, sent: 0, failed: 0, pending: 0, status: 'PENDING' })
let eventSource: EventSource | null = null

const canSend = computed(() => selectedIds.value.length > 0 && mailSubject.value && mailBody.value)
const progressPercent = computed(() => {
  if (!progress.value.total) return 0
  return Math.round(((progress.value.sent + progress.value.failed) / progress.value.total) * 100)
})
const progressStatus = computed(() => {
  if (progress.value.status === 'COMPLETED') return 'success'
  if (progress.value.status === 'CANCELLED') return 'warning'
  return undefined
})

watch(selectedIds, (ids) => {
  selectedCustomers.value = customers.value.filter((c) => ids.includes(c.id))
})

async function loadCustomers() {
  const res = await getCustomers({ page: 0, size: 500, keyword: customerKeyword.value || undefined, tag: filterTag.value || undefined })
  customers.value = res.data.content
}

async function loadTags() {
  const res = await getAllTags()
  allTags.value = res.data
}

async function loadTemplates() {
  const res = await getAllTemplates()
  templateList.value = res.data
}

function applyTemplate(id: string) {
  const tpl = templateList.value.find((t) => t.id === id)
  if (tpl) {
    mailSubject.value = tpl.subject
    mailBody.value = tpl.body
  }
}

function selectAll() {
  selectedIds.value = customers.value.map((c) => c.id)
}

function invertSelection() {
  const allIds = new Set(customers.value.map((c) => c.id))
  const selected = new Set(selectedIds.value)
  selectedIds.value = customers.value.filter((c) => !selected.has(c.id) && allIds.has(c.id)).map((c) => c.id)
}

function handleAttachment() {
  // 附件上传逻辑，实际需要后端文件上传接口支持
}

async function handleSend() {
  if (!canSend.value) return

  await ElMessageBox.confirm(
    `确认向 ${selectedIds.value.length} 位客户发送邮件？`,
    '发送确认',
    { type: 'warning' }
  )

  sending.value = true
  try {
    const response = await sendMail({
      templateId: selectedTemplateId.value || undefined,
      customerIds: selectedIds.value,
      subject: mailSubject.value,
      body: mailBody.value,
    })
    console.log('发送响应:', response)
    const batchId = response.data
    if (batchId && typeof batchId === 'string') {
      ElMessage.success('发送任务已提交')
      showPreview.value = false
      showProgress.value = true
      progress.value.batchId = batchId
      progress.value.total = selectedIds.value.length
      progress.value.pending = selectedIds.value.length
      progress.value.sent = 0
      progress.value.failed = 0
      progress.value.status = 'SENDING'
      connectProgressSSE(batchId)
    } else {
      ElMessage.error('提交失败，响应格式错误')
    }
  } catch (error) {
    console.error('发送失败:', error)
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

function connectProgressSSE(batchId: string) {
  if (eventSource) {
    eventSource.close()
  }

  eventSource = new EventSource(`/api/mail/progress/${batchId}`)

  eventSource.addEventListener('progress', (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.code === 200 && data.data) {
        const p = data.data
        progress.value = {
          batchId: p.batchId || batchId,
          total: p.total || progress.value.total,
          sent: p.sent || 0,
          failed: p.failed || 0,
          pending: p.pending !== undefined ? p.pending : (progress.value.total - p.sent - p.failed),
          status: p.status || 'SENDING'
        }
        console.log('进度更新:', progress.value)
      }
    } catch (e) {
      console.error('解析进度数据失败:', e)
    }
  })

  eventSource.addEventListener('complete', (event) => {
    try {
      const data = JSON.parse(event.data)
      ElMessage.success(data.message || '发送完成')
    } catch (e) {
      ElMessage.success('发送完成')
    }
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
  })

  eventSource.addEventListener('error', (event) => {
    console.error('SSE连接错误:', event)
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    ElMessage.error('进度获取失败')
  })

  eventSource.addEventListener('close', () => {
    console.log('SSE连接关闭')
    eventSource = null
  })
}

async function handleCancel() {
  if (!progress.value.batchId) return
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  await cancelSend(progress.value.batchId)
  progress.value.status = 'CANCELLED'
  ElMessage.info('正在取消发送...')
}

function handleClose() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  showProgress.value = false
  progress.value = { batchId: '', total: 0, sent: 0, failed: 0, pending: 0, status: 'PENDING' }
}

onMounted(() => {
  loadCustomers()
  loadTags()
  loadTemplates()
})
</script>
