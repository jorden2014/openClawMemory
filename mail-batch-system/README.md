# 📧 邮件批量发送系统 (Mail Batch System)

一个功能完善的邮件批量发送管理系统，基于 Spring Boot 和 Vue 3 构建，支持客户管理、模板管理、邮件批量发送和发送记录追踪。

## ✨ 功能特性

### 🔐 用户认证
- JWT Token 认证机制
- 安全的登录/登出功能
- 密码加密存储（BCrypt）

### 👥 客户管理
- 客户信息增删改查
- 客户分组管理
- 批量导入/导出客户
- 客户标签和备注

### 📝 邮件模板
- 可视化邮件模板编辑器
- 支持富文本编辑（图片、表格、样式）
- 模板变量替换（如：`${name}`, `${email}`）
- 模板分类和标签

### 📨 邮件发送
- 支持 SMTP 配置（QQ、163、Gmail 等）
- 批量发送邮件
- 定时发送功能
- 邮件队列和异步发送
- 发送进度实时追踪

### 📊 发送记录
- 详细的发送记录查询
- 发送状态追踪（成功/失败/待发送）
- 失败原因记录
- 发送统计报表

### 🎨 现代化界面
- Vue 3 + TypeScript
- Element Plus UI 组件
- 响应式设计
- 暗色/亮色主题切换

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.x
- **ORM**: MyBatis Plus
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **认证**: JWT (JSON Web Token)
- **异步处理**: Spring Async
- **构建工具**: Maven

### 前端
- **框架**: Vue 3
- **语言**: TypeScript
- **UI 库**: Element Plus
- **路由**: Vue Router
- **状态管理**: Pinia
- **HTTP 客户端**: Axios
- **构建工具**: Vite

## 📋 环境要求

- **JDK**: 21+ （推荐 OpenJDK 21）
- **Node.js**: 18+ （推荐 Node 20 LTS）
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Maven**: 3.8+

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/jorden2014/mail-batch-system.git
cd mail-batch-system
```

### 2. 后端配置

#### 2.1 创建数据库

```sql
CREATE DATABASE mail_batch_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2.2 配置数据库连接

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mail_batch_system?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    database: 0
```

#### 2.3 启动后端服务

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

### 3. 前端配置

#### 3.1 安装依赖

```bash
cd frontend
npm install
# 或使用 pnpm
pnpm install
```

#### 3.2 启动开发服务器

```bash
npm run dev
```

前端将在 `http://localhost:5173` 启动。

### 4. 访问系统

打开浏览器访问：`http://localhost:5173`

默认管理员账号：
- 用户名：`admin`
- 密码：`admin123`

## 📦 生产部署

### 后端部署

```bash
cd backend
mvn clean package -DskipTests
java -jar target/mail-batch-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 前端部署

```bash
cd frontend
npm run build
# 构建产物在 dist/ 目录
# 将 dist/ 目录部署到 Nginx/Apache 等 Web 服务器
```

### Docker 部署（推荐）

```bash
# TODO: 添加 Dockerfile 和 docker-compose.yml
```

## 📁 项目结构

```
mail-batch-system/
├── backend/                      # 后端服务
│   ├── src/main/java/com/mailbatch/mailbatchsystem/
│   │   ├── config/             # 配置类
│   │   ├── controller/         # 控制器
│   │   ├── service/            # 业务逻辑
│   │   ├── repository/         # 数据访问层
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── security/           # 安全配置
│   │   └── exception/         # 异常处理
│   ├── src/main/resources/
│   │   ├── application.yml     # 配置文件
│   │   └── schema.sql          # 数据库初始化脚本
│   └── pom.xml
│
├── frontend/                     # 前端项目
│   ├── src/
│   │   ├── views/             # 页面组件
│   │   ├── components/        # 通用组件
│   │   ├── api/               # API 接口
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # 状态管理
│   │   └── utils/             # 工具函数
│   ├── package.json
│   └── vite.config.ts
│
└── README.md
```

## 🔧 配置说明

### SMTP 配置示例

在系统 "设置" 页面配置 SMTP：

| 邮箱类型 | SMTP 服务器 | 端口 | 加密方式 |
|---------|------------|------|---------|
| QQ 邮箱  | smtp.qq.com | 465 | SSL |
| 163 邮箱 | smtp.163.com | 465 | SSL |
| Gmail   | smtp.gmail.com | 587 | TLS |

**注意**：使用 QQ/163 邮箱时，需要使用**授权码**而不是登录密码。

### 邮件模板变量

在邮件模板中可以使用以下变量：

- `${name}` - 客户姓名
- `${email}` - 客户邮箱
- `${company}` - 公司名称
- `${phone}` - 联系电话
- 更多自定义变量...

## 📊 功能截图

> TODO: 添加系统截图

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 开源协议

本项目采用 MIT 协议开源。

## 📞 联系方式

- 作者：jorden2014
- GitHub：[https://github.com/jorden2014/mail-batch-system](https://github.com/jorden2014/mail-batch-system)
- 邮箱：your-email@example.com

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue 3](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis Plus](https://baomidou.com/)
- [Redis](https://redis.io/)

---

⭐ 如果这个项目对你有帮助，请给它一个 Star！
