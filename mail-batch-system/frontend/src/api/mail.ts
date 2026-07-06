import request from '../utils/request'
import type { Result, SendMailRequest, SendProgress } from '../utils/types'

// 提交发送任务
export function sendMail(data: SendMailRequest) {
  return request.post<any, Result<{ batchId: string }>>('/mail/send', data)
}

// 获取发送进度
export function getProgress(batchId: string) {
  return request.get<any, Result<SendProgress>>(`/mail/progress/${batchId}`)
}

// 取消发送
export function cancelSend(batchId: string) {
  return request.post<any, Result<void>>(`/mail/cancel/${batchId}`)
}
