import request from '../utils/request'
import type { Result, PageResponse, Customer, CustomerRequest } from '../utils/types'

// 获取客户列表（分页+搜索）
export function getCustomers(params: { page: number; size: number; keyword?: string; tag?: string }) {
  return request.get<any, Result<PageResponse<Customer>>>('/customers', { params })
}

// 获取客户详情
export function getCustomer(id: string) {
  return request.get<any, Result<Customer>>(`/customers/${id}`)
}

// 新增客户
export function createCustomer(data: CustomerRequest) {
  return request.post<any, Result<Customer>>('/customers', data)
}

// 编辑客户
export function updateCustomer(id: string, data: CustomerRequest) {
  return request.put<any, Result<Customer>>(`/customers/${id}`, data)
}

// 删除客户
export function deleteCustomer(id: string) {
  return request.delete<any, Result<void>>(`/customers/${id}`)
}

// 批量导入（CSV/Excel）
export function importCustomers(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, Result<{ success: number; failed: number; errors: string[] }>>('/customers/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 导出 CSV
export function exportCustomers(params?: { tag?: string }) {
  return request.get('/customers/export', { params, responseType: 'blob' })
}

// 获取所有标签
export function getAllTags() {
  return request.get<any, Result<string[]>>('/customers/tags')
}
