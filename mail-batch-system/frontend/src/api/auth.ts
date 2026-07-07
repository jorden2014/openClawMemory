import request from '../utils/request'
import type { Result, LoginRequest, LoginResponse } from '../utils/types'

// 登录
export function login(data: LoginRequest) {
  return request.post<any, Result<LoginResponse>>('/simple-auth/login', data)
}
