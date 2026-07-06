# 邮件批量发送系统 - 项目结构

```
mail-batch-system/backend/
├── pom.xml                          # Maven依赖配置
├── README.md                        # 项目说明文档
├── start.sh                         # 启动脚本
└── src/
    ├── main/
    │   ├── java/com/mailbatch/mailbatchsystem/
    │   │   ├── MailBatchSystemApplication.java    # 启动类
    │   │   ├── controller/               # 控制器层
    │   │   │   ├── AuthController.java           # 认证接口
    │   │   │   ├── CustomerController.java       # 客户管理接口
    │   │   │   ├── TemplateController.java       # 模板管理接口
    │   │   │   ├── MailController.java           # 邮件发送接口
    │   │   │   ├── MailRecordController.java     # 发送记录接口
    │   │   │   └── ConfigController.java         # 系统配置接口
    │   │   ├── service/                  # 业务逻辑层
    │   │   │   ├── UserService.java             # 用户服务
    │   │   │   ├── CustomerService.java         # 客户服务
    │   │   │   ├── TemplateService.java          # 模板服务
    │   │   │   ├── MailSendService.java          # 邮件发送服务（核心）
    │   │   │   └── MailRecordService.java        # 发送记录服务
    │   │   ├── entity/                   # 实体类
    │   │   │   ├── User.java                    # 用户实体
    │   │   │   ├── Customer.java                # 客户实体
    │   │   │   ├── MailTemplate.java            # 邮件模板实体
    │   │   │   └── MailRecord.java              # 邮件记录实体
    │   │   ├── repository/                # 数据访问层
    │   │   │   ├── UserRepository.java          # 用户数据访问
    │   │   │   ├── CustomerRepository.java      # 客户数据访问
    │   │   │   ├── MailTemplateRepository.java   # 模板数据访问
    │   │   │   └── MailRecordRepository.java     # 记录数据访问
    │   │   ├── dto/                      # 数据传输对象
    │   │   │   ├── LoginRequest.java            # 登录请求
    │   │   │   ├── LoginResponse.java           # 登录响应
    │   │   │   ├── CustomerRequest.java          # 客户请求
    │   │   │   ├── CustomerResponse.java         # 客户响应
    │   │   │   ├── TemplateRequest.java          # 模板请求
    │   │   │   ├── TemplateResponse.java         # 模板响应
    │   │   │   ├── SendMailRequest.java          # 发送邮件请求
    │   │   │   ├── MailRecordResponse.java       # 邮件记录响应
    │   │   │   ├── PageResponse.java             # 分页响应
    │   │   │   ├── Result.java                   # 统一返回格式
    │   │   │   ├── SmtpConfig.java               # SMTP配置
    │   │   │   └── SendParams.java               # 发送参数配置
    │   │   ├── config/                    # 配置类
    │   │   │   ├── SecurityConfig.java           # Spring Security配置
    │   │   │   ├── RedisConfig.java              # Redis配置
    │   │   │   ├── CorsConfig.java               # CORS跨域配置
    │   │   │   ├── AsyncConfig.java              # 异步执行器配置
    │   │   │   └── DatabaseInitializer.java      # 数据库初始化
    │   │   ├── security/                  # 安全相关
    │   │   │   ├── JwtTokenProvider.java         # JWT工具类
    │   │   │   ├── JwtAuthenticationFilter.java  # JWT过滤器
    │   │   │   └── SecurityConfig.java           # 安全配置
    │   │   └── exception/                # 异常处理
    │   │       ├── GlobalExceptionHandler.java   # 全局异常处理器
    │   │       └── BusinessException.java        # 业务异常类
    │   └── resources/
    │       ├── application.yml            # 应用配置文件
    │       └── schema.sql                # 数据库初始化脚本
    └── test/                            # 测试代码
        └── java/
```

## 核心功能模块

### 1. 认证模块 (Auth)
- 用户登录 (`/api/auth/login`)
- JWT Token生成和验证
- 当前用户信息获取

### 2. 客户管理模块 (Customer)
- CRUD操作
- Excel导入导出
- 按标签/关键词搜索

### 3. 邮件模板模块 (Template)
- 模板CRUD
- HTML内容支持
- 占位符（{{称呼}}）

### 4. 邮件发送模块 (Mail) - 核心
- 提交发送任务到Redis队列
- 异步消费队列逐封发送
- 发送频率控制（可配置间隔）
- 失败自动重试（最多3次）
- SSE实时推送进度
- 取消发送任务

### 5. 发送记录模块 (Record)
- 分页查询发送记录
- 按状态/批次/客户筛选
- 失败邮件重发

### 6. 系统配置模块 (Config)
- SMTP配置
- 发送参数配置
- 测试连接

## 技术亮点

1. **JWT认证**: 无状态认证，支持Token过期
2. **Redis队列**: 异步处理发送任务，削峰填谷
3. **SSE推送**: 实时推送发送进度到前端
4. **失败重试**: 自动重试机制，提高发送成功率
5. **频率控制**: 可配置发送间隔，避免被邮件服务商限制
6. **Excel导入导出**: 方便批量管理客户
7. **统一异常处理**: 全局异常捕获，统一返回格式
8. **统一返回格式**: Result<T> 包装所有API响应

## 数据库设计

### 用户表 (users)
- id, username, password, role, created_at, updated_at

### 客户表 (customers)
- id, name, salutation, email, tags, remark, created_at, updated_at

### 邮件模板表 (mail_templates)
- id, name, subject, body, attachment_paths, created_at, updated_at

### 邮件发送记录表 (mail_records)
- id, batch_id, customer_id, to_email, salutation, subject, body, 
  attachment_paths, status, retry_count, error_msg, sent_at, created_at

## API请求示例

### 登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 创建客户
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"name":"张三","salutation":"张先生","email":"zhangsan@example.com","tags":["VIP"]}'
```

### 发送邮件
```bash
curl -X POST http://localhost:8080/api/mail/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"templateId":1,"customerIds":[1,2,3]}'
```

## 部署建议

1. **生产环境**: 
   - 修改JWT密钥（至少256位）
   - 使用环境变量或配置中心管理敏感信息
   - 配置MySQL主从复制
   - 配置Redis哨兵或集群
   - 使用Nginx反向代理

2. **性能优化**:
   - 调整连接池大小
   - 增加邮件发送线程池
   - 考虑使用消息队列（如RabbitMQ）替代Redis List

3. **监控告警**:
   - 接入Prometheus + Grafana
   - 配置邮件发送失败告警
   - 监控队列积压情况

## 扩展方向

- [ ] 添加邮件发送统计报表
- [ ] 支持定时发送
- [ ] 支持邮件打开/点击追踪
- [ ] 集成更多邮件服务商
- [ ] 添加邮件模板可视化编辑器
- [ ] 支持A/B测试
- [ ] 添加黑名单管理
