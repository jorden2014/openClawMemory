#!/bin/bash

# ============================================
# 邮件批量发送系统 - 前端部署脚本
# ============================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量（根据需要修改）
PROJECT_DIR="/home/admin/.openclaw/workspace/mail-batch-system"
FRONTEND_DIR="${PROJECT_DIR}/frontend"
DEPLOY_DIR="/usr/share/nginx/html/mail-batch-system"  # 部署目录（Nginx 静态目录）
BACKUP_DIR="${PROJECT_DIR}/backups"
BUILD_DIR="${FRONTEND_DIR}/dist"

# 打印带颜色的消息
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "命令 '$1' 未找到，请先安装"
        exit 1
    fi
}

# 检查依赖
check_dependencies() {
    log_info "检查依赖..."
    check_command node
    check_command npm
    check_command git
}

# 拉取最新代码
pull_latest_code() {
    log_info "拉取最新代码..."
    cd "${PROJECT_DIR}"
    
    # 保存未提交的改动（如果有）
    if [[ -n $(git status -s) ]]; then
        log_warn "发现未提交的改动，已暂存"
        git stash push -m "Auto stash before deploy $(date '+%Y-%m-%d %H:%M:%S')"
    fi
    
    # 拉取最新代码
    git pull origin master
    log_info "代码拉取完成"
}

# 安装依赖
install_dependencies() {
    log_info "安装前端依赖..."
    cd "${FRONTEND_DIR}"
    
    # 清理旧的 node_modules（可选，遇到问题时取消注释）
    # rm -rf node_modules package-lock.json
    
    npm install --production=false
    log_info "依赖安装完成"
}

# 编译前端
build_frontend() {
    log_info "开始编译前端..."
    cd "${FRONTEND_DIR}"
    
    # 设置 Node.js 内存限制（根据实际情况调整）
    export NODE_OPTIONS="--max-old-space-size=2048"
    
    # 编译
    npm run build
    
    # 检查编译结果
    if [[ ! -d "${BUILD_DIR}" ]]; then
        log_error "编译失败：dist 目录不存在"
        exit 1
    fi
    
    log_info "编译完成，输出目录：${BUILD_DIR}"
}

# 备份当前部署
backup_current_deploy() {
    if [[ -d "${DEPLOY_DIR}" ]]; then
        log_info "备份当前部署..."
        mkdir -p "${BACKUP_DIR}"
        BACKUP_FILE="${BACKUP_DIR}/backup_$(date '+%Y%m%d_%H%M%S').tar.gz"
        tar -czf "${BACKUP_FILE}" -C "$(dirname "${DEPLOY_DIR}")" "$(basename "${DEPLOY_DIR}")"
        log_info "备份已保存：${BACKUP_FILE}"
    fi
}

# 部署到目标目录
deploy_to_target() {
    log_info "部署到目标目录：${DEPLOY_DIR}"
    
    # 创建目标目录
    sudo mkdir -p "${DEPLOY_DIR}"
    
    # 删除旧文件
    sudo rm -rf "${DEPLOY_DIR}/*"
    
    # 复制新文件
    sudo cp -r "${BUILD_DIR}"/* "${DEPLOY_DIR}/"
    
    # 设置权限
    sudo chown -R www-data:www-data "${DEPLOY_DIR}" 2>/dev/null || true
    sudo chmod -R 755 "${DEPLOY_DIR}"
    
    log_info "部署完成"
}

# 测试部署
test_deploy() {
    log_info "测试部署..."
    
    # 检查关键文件
    if [[ ! -f "${DEPLOY_DIR}/index.html" ]]; then
        log_error "部署失败：index.html 不存在"
        exit 1
    fi
    
    log_info "部署测试通过"
}

# 显示部署信息
show_deploy_info() {
    log_info "=========================================="
    log_info "部署信息"
    log_info "=========================================="
    log_info "项目目录：${PROJECT_DIR}"
    log_info "前端目录：${FRONTEND_DIR}"
    log_info "部署目录：${DEPLOY_DIR}"
    log_info "编译输出：${BUILD_DIR}"
    log_info "=========================================="
    
    # 显示前端访问地址（根据实际情况修改）
    echo ""
    log_info "前端访问地址："
    log_info "  http://your-server-ip/"
    log_info "  (请确保 Nginx 已配置静态目录：${DEPLOY_DIR})"
    echo ""
}

# 主函数
main() {
    log_info "=========================================="
    log_info "开始部署邮件批量发送系统前端"
    log_info "=========================================="
    echo ""
    
    # 检查依赖
    check_dependencies
    echo ""
    
    # 拉取最新代码
    pull_latest_code
    echo ""
    
    # 安装依赖
    install_dependencies
    echo ""
    
    # 编译前端
    build_frontend
    echo ""
    
    # 备份当前部署
    backup_current_deploy
    echo ""
    
    # 部署到目标目录
    deploy_to_target
    echo ""
    
    # 测试部署
    test_deploy
    echo ""
    
    # 显示部署信息
    show_deploy_info
    
    log_info "=========================================="
    log_info "部署完成！"
    log_info "=========================================="
}

# 如果是直接运行脚本（不是被 source），则执行主函数
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main
fi
