#!/bin/bash

# 邮件批量发送系统启动脚本

echo "=========================================="
echo "   邮件批量发送系统 - 启动脚本"
echo "=========================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装JDK 17+"
    exit 1
fi

echo "Java版本:"
java -version

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "警告: 未找到Maven，将尝试使用已编译的jar包"
    USE_MAVEN=false
else
    echo "Maven版本:"
    mvn -version
    USE_MAVEN=true
fi

# 检查MySQL服务
echo "检查MySQL服务..."
if command -v mysql &> /dev/null; then
    if mysqladmin ping -h localhost --silent 2>/dev/null; then
        echo "✓ MySQL服务已启动"
    else
        echo "⚠ MySQL服务未启动，请先启动MySQL"
        echo "  可以使用: sudo systemctl start mysql"
    fi
else
    echo "⚠ 未找到MySQL客户端，请手动确认MySQL服务已启动"
fi

# 检查Redis服务
echo "检查Redis服务..."
if command -v redis-cli &> /dev/null; then
    if redis-cli ping 2>/dev/null | grep -q PONG; then
        echo "✓ Redis服务已启动"
    else
        echo "⚠ Redis服务未启动，请先启动Redis"
        echo "  可以使用: sudo systemctl start redis"
    fi
else
    echo "⚠ 未找到Redis客户端，请手动确认Redis服务已启动"
fi

# 进入项目目录
cd "$(dirname "$0")"

# 配置检查
echo ""
echo "检查配置文件..."
if [ -f "src/main/resources/application.yml" ]; then
    echo "✓ 配置文件存在"
    echo "  请确认以下配置已正确设置:"
    echo "  - MySQL数据库连接"
    echo "  - Redis连接"
    echo "  - SMTP邮件服务器"
else
    echo "✗ 配置文件不存在！"
    exit 1
fi

# 编译项目
if [ "$USE_MAVEN" = true ]; then
    echo ""
    echo "开始编译项目..."
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        echo "✓ 项目编译成功"
    else
        echo "✗ 项目编译失败"
        exit 1
    fi
fi

# 启动应用
echo ""
echo "启动应用..."
echo "=========================================="

if [ -f "target/mail-batch-system-1.0.0.jar" ]; then
    java -jar target/mail-batch-system-1.0.0.jar
elif [ -f "target/mail-batch-system-*.jar" ]; then
    java -jar target/mail-batch-system-*.jar
else
    echo "未找到可执行的jar包，尝试使用Maven启动..."
    mvn spring-boot:run
fi

echo ""
echo "应用已停止"
echo "=========================================="
