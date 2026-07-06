// 统一响应类型
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

// 分页响应
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

// 登录
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  role: string
}

// 客户
export interface Customer {
  id: string
  name: string
  salutation: string
  email: string
  tags: string[]
  remark: string
  createdAt: string
  updatedAt: string
}

export interface CustomerRequest {
  name: string
  salutation: string
  email: string
  tags?: string[]
  remark?: string
}

// 邮件模板
export interface MailTemplate {
  id: string
  name: string
  subject: string
  body: string
  attachmentPaths: string[]
  createdAt: string
  updatedAt: string
}

export interface TemplateRequest {
  name: string
  subject: string
  body: string
  attachmentPaths?: string[]
}

// 发送邮件请求
export interface SendMailRequest {
  templateId?: string
  customerIds: string[]
  subject?: string
  body?: string
  attachmentPaths?: string[]
}

// 发送进度
export interface SendProgress {
  batchId: string
  total: number
  sent: number
  failed: number
  pending: number
  status: 'PENDING' | 'SENDING' | 'COMPLETED' | 'CANCELLED'
}

// 邮件记录
export interface MailRecord {
  id: string
  batchId: string
  customerId: string
  toEmail: string
  salutation: string
  subject: string
  body: string
  attachmentPaths: string[]
  status: 'PENDING' | 'SENDING' | 'SENT' | 'FAILED'
  retryCount: number
  errorMsg: string
  sentAt: string
  createdAt: string
}

// SMTP 配置
export interface SmtpConfig {
  host: string
  port: number
  username: string
  password: string
  ssl: boolean
  senderName: string
  senderSignature: string
}

// 发送参数
export interface SendParams {
  intervalSeconds: number
  maxRetryCount: number
  maxBatchSize: number
}

// 仪表盘统计
export interface DashboardStats {
  customerCount: number
  todaySentCount: number
  monthSentCount: number
  failedCount: number
  recentRecords: MailRecord[]
  weeklyTrend: { date: string; count: number }[]
}
