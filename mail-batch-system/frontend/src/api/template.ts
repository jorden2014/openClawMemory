import request from '../utils/request'
import type { Result, PageResponse, MailTemplate, TemplateRequest } from '../utils/types'

// 获取模板列表
export function getTemplates(params?: { page: number; size: number; keyword?: string }) {
  return request.get<any, Result<PageResponse<MailTemplate>>>('/templates', { params })
}

// 获取所有模板（下拉选择用）
export function getAllTemplates() {
  return request.get<any, Result<MailTemplate[]>>('/templates/all')
}

// 获取模板详情
export function getTemplate(id: string) {
  return request.get<any, Result<MailTemplate>>(`/templates/${id}`)
}

// 新增模板
export function createTemplate(data: TemplateRequest) {
  return request.post<any, Result<MailTemplate>>('/templates', data)
}

// 编辑模板
export function updateTemplate(id: string, data: TemplateRequest) {
  return request.put<any, Result<MailTemplate>>(`/templates/${id}`, data)
}

// 删除模板
export function deleteTemplate(id: string) {
  return request.delete<any, Result<void>>(`/templates/${id}`)
}
