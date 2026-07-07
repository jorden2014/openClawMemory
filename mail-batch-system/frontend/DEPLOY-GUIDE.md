# 前端部署指南

## 📦 部署脚本

### 1. `deploy.sh` - 自动化部署脚本

**功能**：
- ✅ 拉取最新代码
- ✅ 安装依赖
- ✅ 编译前端
- ✅ 备份当前部署
- ✅ 部署到目标目录
- ✅ 测试部署结果

**使用方法**：

```bash
# 进入前端目录
cd /home/admin/.openclaw/workspace/mail-batch-system/frontend

# 修改配置（可选）
vim deploy.sh  # 修改 DEPLOY_DIR 等变量

# 执行部署
./deploy.sh
```

**注意事项**：
- 需要 `sudo` 权限（部署到 `/var/www/` 目录）
- 确保 `DEPLOY_DIR` 与 Nginx 配置中的 `root` 路径一致
- 首次部署前请修改 `deploy.sh` 中的配置变量

---

## 🌐 Nginx 配置

### 2. `nginx-config-example.conf` - Nginx 配置示例

**功能**：
- ✅ 前端静态文件服务
- ✅ Vue Router history 模式支持
- ✅ API 代理到后端
- ✅ 静态资源缓存
- ✅ 基本安全配置

**使用方法**：

```bash
# 1. 复制配置文件
sudo cp nginx-config-example.conf /etc/nginx/sites-available/mail-batch-system

# 2. 编辑配置文件，修改 server_name 等
sudo vim /etc/nginx/sites-available/mail-batch-system

# 3. 创建软链接
sudo ln -s /etc/nginx/sites-available/mail-batch-system /etc/nginx/sites-enabled/

# 4. 测试配置
sudo nginx -t

# 5. 重载 Nginx
sudo systemctl reload nginx
```

**注意事项**：
- 修改 `server_name` 为你的域名或 IP
- 确保 `root` 路径与 `deploy.sh` 中的 `DEPLOY_DIR` 一致
- 如果后端在不同服务器，修改 `proxy_pass` 地址

---

## 🚀 快速部署流程

### 方式一：使用部署脚本（推荐）

```bash
# 1. 修改配置
cd /home/admin/.openclaw/workspace/mail-batch-system/frontend
vim deploy.sh  # 修改 DEPLOY_DIR 等

# 2. 执行部署
./deploy.sh

# 3. 配置 Nginx（首次）
sudo cp nginx-config-example.conf /etc/nginx/sites-available/mail-batch-system
sudo vim /etc/nginx/sites-available/mail-batch-system
sudo ln -s /etc/nginx/sites-available/mail-batch-system /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# 4. 访问前端
# 浏览器打开：http://your-server-ip/
```

### 方式二：手动部署

```bash
# 1. 拉取代码
cd /home/admin/.openclaw/workspace/mail-batch-system
git pull origin master

# 2. 安装依赖
cd frontend
npm install

# 3. 编译
npm run build

# 4. 部署
sudo rm -rf /var/www/mail-batch-system/*
sudo cp -r dist/* /var/www/mail-batch-system/

# 5. 访问前端
```

---

## ⚙️ 配置说明

### `deploy.sh` 主要配置变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `PROJECT_DIR` | `/home/admin/.openclaw/workspace/mail-batch-system` | 项目根目录 |
| `FRONTEND_DIR` | `${PROJECT_DIR}/frontend` | 前端目录 |
| `DEPLOY_DIR` | `/var/www/mail-batch-system` | 部署目录（Nginx 静态目录） |
| `BACKUP_DIR` | `${PROJECT_DIR}/backups` | 备份目录 |
| `NODE_OPTIONS` | `--max-old-space-size=2048` | Node.js 内存限制 |

### Nginx 配置主要修改点

1. `server_name`：修改为你的域名或 IP
2. `root`：与 `deploy.sh` 中的 `DEPLOY_DIR` 一致
3. `proxy_pass`：后端 API 地址（如果后端在同一台服务器可不修改）

---

## 🐛 常见问题

### 1. 部署失败：权限不足

```bash
# 确保有 sudo 权限
sudo usermod -aG sudo your-username
```

### 2. 前端刷新 404

```bash
# 确保 Nginx 配置了 Vue Router history 模式支持
location / {
    try_files $uri $uri/ /index.html;
}
```

### 3. API 请求失败

```bash
# 检查 Nginx 代理配置
location /api/ {
    proxy_pass http://localhost:8080;  # 确保后端地址正确
}
```

### 4. 编译内存不足

```bash
# 修改 deploy.sh 中的 NODE_OPTIONS
export NODE_OPTIONS="--max-old-space-size=1024"  # 减少内存限制
```

---

## 📝 更新日志

- **2026-07-07**：新增 Quill 编辑器（CDN 版本），支持字体切换和插入图片
- **2026-07-07**：创建部署脚本和 Nginx 配置示例

---

## 🔗 相关链接

- 项目地址：https://github.com/jorden2014/mail-batch-system
- Quill 文档：https://quilljs.com/docs/
- Nginx 文档：https://nginx.org/en/docs/

---

**有问题随时找我！** 🦐
