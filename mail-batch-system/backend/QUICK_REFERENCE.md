# 快速参考卡片 (Quick Reference)

## 🚀 快速启动

```bash
# 进入项目目录
cd /home/admin/.openclaw/workspace/mail-batch-system/backend

# 方式1: 使用启动脚本
./start.sh

# 方式2: Maven编译后运行
mvn clean package -DskipTests
java -jar target/mail-batch-system-1.0.0.jar
```

## 🔑 默认登录信息
```
用户名: admin
密码: admin123
```

## 📡 API快速测试

### 1. 登录获取Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. 使用Token访问API
```bash
# 将上一步获取的token替换YOUR_JWT_TOKEN
export TOKEN="YOUR_JWT_TOKEN"

# 获取客户列表
curl http://localhost:8080/api/customers?page=0&size=10 \
  -H "Authorization: Bearer $TOKEN"
```

## 📂 关键文件路径

| 文件 | 路径 | 说明 |
|------|------|------|
| 主配置 | `src/main/resources/application.yml` | 应用配置 |
| 数据库脚本 | `src/main/resources/schema.sql` | 初始化SQL |
| 启动类 | `src/main/java/.../MailBatchSystemApplication.java` | 程序入口 |
| 主POM | `pom.xml` | Maven依赖 |

## ⚙️ 配置检查清单

在启动前检查 `application.yml`:

- [ ] MySQL数据库连接 (spring.datasource.*)
- [ ] Redis连接 (spring.data.redis.*)
- [ ] SMTP邮件服务器 (spring.mail.*)
- [ ] JWT密钥 (jwt.secret) - 生产环境必须修改
- [ ] 发送间隔和重试次数 (mail.batch.*)

## 🔧 常用命令

### 编译
```bash
mvn clean package -DskipTests
```

### 运行测试
```bash
mvn test
```

### 清理
```bash
mvn clean
```

### 查看依赖树
```bash
mvn dependency:tree
```

## 🐛 常见问题

### 1. 启动失败 - 数据库连接失败
**解决**: 检查MySQL是否启动，用户名密码是否正确

### 2. 启动失败 - Redis连接失败
**解决**: 检查Redis是否启动
```bash
redis-server --version
redis-cli ping  # 应该返回PONG
```

### 3. 邮件发送失败
**解决**: 检查SMTP配置，确认邮箱密码/授权码正确

### 4. JWT Token过期
**解决**: 重新登录获取新Token

## 📊 监控端点

应用启动后访问:
- 应用: http://localhost:8080
- 健康检查: http://localhost:8080/actuator/health (需添加actuator依赖)
- API文档: 待集成Swagger

## 📦 部署检查清单

生产环境部署前:
- [ ] 修改JWT密钥为强密钥
- [ ] 使用环境变量或配置中心管理敏感信息
- [ ] 配置MySQL主从/集群
- [ ] 配置Redis哨兵/集群
- [ ] 配置日志轮转
- [ ] 配置HTTPS
- [ ] 设置合适的JVM参数
- [ ] 配置监控和告警

## 🔗 相关文档

- [README.md](README.md) - 项目说明
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - 详细结构
- [REPORT.md](REPORT.md) - 生成报告

## 📞 技术支持

遇到问题？
1. 查看日志文件
2. 检查配置
3. 阅读文档
4. 搜索类似问题

---

快速参考版本: 1.0
最后更新: 2026-07-06
