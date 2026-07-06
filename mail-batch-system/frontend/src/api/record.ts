import request from '../utils/request'
import type { Result, PageResponse, MailRecord, DashboardStats } from '../utils/types'

// 获取发送记录列表
export function getRecords(params: {
  page: number
  size: number
  batchId?: string
  customerName?: string
  status?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<any, Result<PageResponse<MailRecord>>>('/records', { params })
}

// 获取记录详情
export function getRecord(id: string) {
  return request.get<any, Result<MailRecord>>(`/records/${id}`)
}

// 重发失败邮件
export function resendRecord(id: string) {
  return request.post<any, Result<void>>(`/records/${id}/resend`)
}

// 仪表盘统计
export function getDashboardStats() {
  return request.get<any, Result<DashboardStats>>('/records/dashboard')
}
