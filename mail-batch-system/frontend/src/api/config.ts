import request from '../utils/request'
import type { Result, SmtpConfig, SendParams } from '../utils/types'

// 获取 SMTP 配置
export function getSmtpConfig() {
  return request.get<any, Result<SmtpConfig>>('/config/smtp')
}

// 更新 SMTP 配置
export function updateSmtpConfig(data: SmtpConfig) {
  return request.put<any, Result<void>>('/config/smtp', data)
}

// 获取发送参数
export function getSendParams() {
  return request.get<any, Result<SendParams>>('/config/send-params')
}

// 更新发送参数
export function updateSendParams(data: SendParams) {
  return request.put<any, Result<void>>('/config/send-params', data)
}
