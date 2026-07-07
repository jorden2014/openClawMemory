import request from '../utils/request'
import type { Result } from '../utils/types'

export function getMailConfig() {
  return request.get<any, Result<Map<string, any>>>('/config/mail')
}

export function updateMailConfig(data: any) {
  return request.post<any, Result<null>>('/config/mail', data)
}

export function testMailConfig(data: any) {
  return request.post<any, Result<null>>('/config/mail/test', data)
}
