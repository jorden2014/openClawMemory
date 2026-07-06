<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <div style="color: #999; font-size: 14px">{{ card.title }}</div>
              <div style="font-size: 28px; font-weight: bold; margin-top: 8px">{{ card.value }}</div>
            </div>
            <el-icon :size="48" :color="card.color"><component :is="card.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发送趋势 -->
    <el-card shadow="hover" style="margin-bottom: 24px">
      <template #header><span>近7天发送趋势</span></template>
      <v-chart :option="chartOption" style="height: 300px" autoresize />
    </el-card>

    <!-- 最近记录 -->
    <el-card shadow="hover">
      <template #header><span>最近发送记录</span></template>
      <el-table :data="recentRecords" stripe>
        <el-table-column prop="toEmail" label="收件人" width="200" />
        <el-table-column prop="salutation" label="称呼" width="100" />
        <el-table-column prop="subject" label="主题" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { User, Promotion, Calendar, Warning } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { getDashboardStats } from '../api/record'
import type { DashboardStats, MailRecord } from '../utils/types'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const stats = ref<DashboardStats | null>(null)
const recentRecords = ref<MailRecord[]>([])

const statCards = computed(() => [
  { title: '客户总数', value: stats.value?.customerCount ?? 0, icon: User, color: '#409EFF' },
  { title: '今日发送', value: stats.value?.todaySentCount ?? 0, icon: Promotion, color: '#67C23A' },
  { title: '本月发送', value: stats.value?.monthSentCount ?? 0, icon: Calendar, color: '#E6A23C' },
  { title: '失败待重发', value: stats.value?.failedCount ?? 0, icon: Warning, color: '#F56C6C' },
])

const chartOption = computed(() => {
  const trend = stats.value?.weeklyTrend || []
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: trend.map((t) => t.date), boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ name: '发送数', type: 'line', data: trend.map((t) => t.count), smooth: true, areaStyle: { opacity: 0.3 }, color: '#409EFF' }],
  }
})

function statusType(status: string) {
  const map: Record<string, string> = { SENT: 'success', FAILED: 'danger', SENDING: 'warning', PENDING: 'info' }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = { SENT: '已发送', FAILED: '失败', SENDING: '发送中', PENDING: '待发送' }
  return map[status] || status
}

async function loadData() {
  try {
    const res = await getDashboardStats()
    stats.value = res.data
    recentRecords.value = res.data.recentRecords
  } catch {}
}

onMounted(loadData)
</script>
