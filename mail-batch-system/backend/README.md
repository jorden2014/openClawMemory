# 邮件批量发送系统

## 项目简介
这是一个基于Spring Boot的邮件批量发送系统，支持：
- 客户管理（CRUD、导入导出）
- 邮件模板管理
- 批量发送邮件
- 发送进度跟踪
- 失败重试机制

## 技术栈
- **后端框架**: Spring Boot 3.2.1
- **Java版本**: 17
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **邮件**: Spring Mail
- **Excel处理**: Apache POI
- **工具库**: Hutool, Lombok

## 项目结构
```
mail-batch-system/backend/
├── src/main/java/com/mailbatch/mailbatchsystem/
│   ├── controller/      # 控制器层
│   ├── service/         # 业务逻辑层
│   ├── entity/          # 实体类
│   ├── repository/      # 数据访问层
│   ├── dto/             # 数据传输对象
│   ├── config/          # 配置类
│   ├── security/        # 安全相关
│   └── exception/       # 异常处理
├── src/main/resources/
│   ├── application.yml  # 应用配置
│   └── schema.sql       # 数据库初始化脚本
└── pom.xml              # Maven依赖
```

## 快速开始

### 1. 环境准备
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 2. 数据库初始化
```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本（或让应用自动执行）
source backend/src/main/resources/schema.sql
```

### 3. 配置修改
编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mail_batch_db
    username: root
    password: your_password
  
  mail:
    host: smtp.exmail.qq.com
    port: 465
    username: your_email@domain.com
    password: your_password
```

### 4. 编译运行
```bash
# 进入项目目录
cd mail-batch-system/backend

# 编译
mvn clean package

# 运行
java -jar target/mail-batch-system-1.0.0.jar

# 或者直接使用Maven运行
mvn spring-boot:run
```

### 5. 访问应用
- 应用启动后访问: http://localhost:8080
- API文档: 待集成Swagger

## API接口说明

### 认证相关
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息

### 客户管理
- `GET /api/customers` - 分页查询客户列表
- `POST /api/customers` - 创建客户
- `PUT /api/customers/{id}` - 更新客户
- `DELETE /api/customers/{id}` - 删除客户
- `GET /api/customers/export` - 导出客户到Excel
- `POST /api/customers/import` - 从Excel导入客户

### 邮件模板
- `GET /api/templates` - 分页查询模板
- `POST /api/templates` - 创建模板
- `PUT /api/templates/{id}` - 更新模板
- `DELETE /api/templates/{id}` - 删除模板

### 邮件发送
- `POST /api/mail/send` - 提交发送任务
- `GET /api/mail/progress/{batchId}` - SSE推送发送进度
- `POST /api/mail/cancel/{batchId}` - 取消发送任务

### 发送记录
- `GET /api/records` - 分页查询发送记录
- `POST /api/records/{id}/resend` - 重发邮件
- `DELETE /api/records/{id}` - 删除记录

## 默认账号
- 用户名: `admin`
- 密码: `admin123`

## 配置说明

### SMTP配置
在 `application.yml` 中配置邮件服务器：
```yaml
spring:
  mail:
    host: smtp.exmail.qq.com  # SMTP服务器
    port: 465                  # 端口
    username: your_email@domain.com
    password: your_password
    properties:
      mail:
        smtp:
          ssl:
            enable: true      # 启用SSL
```

### 发送参数配置
```yaml
mail:
  batch:
    send-interval: 3000  # 每封邮件间隔（毫秒）
    max-retry: 3         # 最大重试次数
```

## 注意事项
1. 邮件模板支持占位符 `{{称呼}}`，发送时会自动替换为客户的称呼
2. 发送任务会放入Redis队列异步处理
3. 发送进度可以通过SSE实时获取
4. 失败的邮件会自动重试（最多3次）
5. 请确保Redis服务已启动

## 开发计划
- [ ] 集成Swagger API文档
- [ ] 添加邮件发送统计报表
- [ ] 支持更多邮件服务商
- [ ] 添加定时发送功能
- [ ] 优化前端界面

## 许可证
MIT License

## 联系方式
如有问题，请联系开发团队。
