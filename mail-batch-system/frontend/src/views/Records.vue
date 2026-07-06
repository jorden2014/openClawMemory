<template>
  <div>
    <!-- 筛选栏 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-row :gutter="16" align="middle">
        <el-col :span="5">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-col>
        <el-col :span="4">
          <el-input v-model="filterCustomerName" placeholder="客户姓名" clearable />
        </el-col>
        <el-col :span="4">
          <el-input v-model="filterBatchId" placeholder="批次ID" clearable />
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterStatus" placeholder="状态" clearable>
            <el-option label="待发送" value="PENDING" />
            <el-option label="发送中" value="SENDING" />
            <el-option label="已发送" value="SENT" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-col>
        <el-col :span="7" style="text-align: right">
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 记录表格 -->
    <el-card shadow="never">
      <el-table :data="records" stripe v-loading="loading" @row-click="showDetail">
        <el-table-column prop="batchId" label="批次ID" width="150" />
        <el-table-column prop="salutation" label="称呼" width="100" />
        <el-table-column prop="toEmail" label="收件邮箱" width="220" />
        <el-table-column prop="subject" label="主题" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="90" />
        <el-table-column prop="sentAt" label="发送时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'FAILED'" type="primary" text size="small" @click.stop="handleResend(row.id)">重发</el-button>
            <el-button type="primary" text size="small" @click.stop="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadData"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="邮件详情" width="700px" destroy-on-close>
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="批次ID">{{ detailData.batchId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detailData.status)" size="small">{{ statusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收件人">{{ detailData.toEmail }}</el-descriptions-item>
          <el-descriptions-item label="称呼">{{ detailData.salutation }}</el-descriptions-item>
          <el-descriptions-item label="重试次数">{{ detailData.retryCount }}</el-descriptions-item>
          <el-descriptions-item label="发送时间">{{ detailData.sentAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="失败原因" :span="2" v-if="detailData.errorMsg">{{ detailData.errorMsg }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 16px">
          <div style="margin-bottom: 8px; font-weight: bold">邮件主题：{{ detailData.subject }}</div>
          <el-divider />
          <div v-html="detailData.body" style="padding: 16px; border: 1px solid #eee; border-radius: 4px; min-height: 100px" />
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecords, resendRecord } from '../api/record'
import type { MailRecord } from '../utils/types'

const loading = ref(false)
const records = ref<MailRecord[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dateRange = ref<string[]>([])
const filterCustomerName = ref('')
const filterBatchId = ref('')
const filterStatus = ref('')

const detailVisible = ref(false)
const detailData = ref<MailRecord | null>(null)

function statusType(status: string) {
  const map: Record<string, string> = { SENT: 'success', FAILED: 'danger', SENDING: 'warning', PENDING: 'info' }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = { SENT: '已发送', FAILED: '失败', SENDING: '发送中', PENDING: '待发送' }
  return map[status] || status
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRecords({
      page: page.value - 1,
      size: size.value,
      customerName: filterCustomerName.value || undefined,
      batchId: filterBatchId.value || undefined,
      status: filterStatus.value || undefined,
      startDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined,
    })
    records.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  dateRange.value = []
  filterCustomerName.value = ''
  filterBatchId.value = ''
  filterStatus.value = ''
  page.value = 1
  loadData()
}

function showDetail(row: MailRecord) {
  detailData.value = row
  detailVisible.value = true
}

async function handleResend(id: string) {
  await resendRecord(id)
  ElMessage.success('已重新发送')
  loadData()
}

onMounted(loadData)
</script>
