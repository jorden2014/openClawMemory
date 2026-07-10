-- 邮件批量发送系统数据库初始化脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS mail_batch CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mail_batch;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER' COMMENT '角色',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 客户表
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '客户名称',
    salutation VARCHAR(50) COMMENT '称呼（用于邮件模板占位符替换）',
    email VARCHAR(200) NOT NULL UNIQUE COMMENT '邮箱地址',
    tags VARCHAR(500) COMMENT '标签（逗号分隔）',
    remark VARCHAR(1000) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_email (email),
    INDEX idx_name (name),
    INDEX idx_tags (tags(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 邮件模板表
CREATE TABLE IF NOT EXISTS mail_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '模板名称',
    subject VARCHAR(500) NOT NULL COMMENT '邮件主题（支持占位符）',
    body TEXT NOT NULL COMMENT '邮件正文（HTML格式，支持占位符）',
    attachment_paths TEXT COMMENT '附件路径（逗号分隔）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件模板表';

-- 邮件发送记录表
CREATE TABLE IF NOT EXISTS mail_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id VARCHAR(50) NOT NULL COMMENT '批次ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    to_email VARCHAR(200) NOT NULL COMMENT '收件人邮箱',
    salutation VARCHAR(50) COMMENT '称呼（实际发送时使用的称呼）',
    subject VARCHAR(500) NOT NULL COMMENT '邮件主题',
    body TEXT NOT NULL COMMENT '邮件正文（替换占位符后的实际内容）',
    attachment_paths TEXT COMMENT '附件路径（实际发送时的附件）',
    status ENUM('PENDING', 'SENDING', 'SENT', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '发送状态',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    error_msg TEXT COMMENT '错误信息',
    sent_at DATETIME COMMENT '实际发送时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_batch_id (batch_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_sent_at (sent_at),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件发送记录表';

-- 插入初始管理员用户（密码：admin123）
INSERT INTO users (username, password, role) VALUES 
('admin', '$2b$10$g2rBK2onifVsN9JQTyQRxOJG2uyZdHMWdbxFTSp3U.hg5IWhTyK7S', 'ADMIN')
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试客户数据
INSERT INTO customers (name, salutation, email, tags, remark) VALUES 
('张三', '张先生', 'zhangsan@example.com', 'VIP,老客户', '重要客户'),
('李四', '李女士', 'lisi@example.com', '潜在客户', '需要跟进'),
('王五', '王总', 'wangwu@example.com', 'VIP,已成交', '大客户')
ON DUPLICATE KEY UPDATE email=email;

-- 插入测试邮件模板
INSERT INTO mail_templates (name, subject, body, attachment_paths) VALUES 
('欢迎邮件', '欢迎您，{{称呼}}！', '<p>尊敬的{{称呼}}：</p><p>感谢您注册我们的服务！</p><p>祝您使用愉快！</p>', NULL),
('促销通知', '限时优惠活动，{{称呼}}不容错过！', '<p>尊敬的{{称呼}}：</p><p>我们为您准备了限时优惠活动，欢迎参与！</p>', NULL)
ON DUPLICATE KEY UPDATE name=name;

-- 显示创建结果
SHOW TABLES;

-- 查询初始数据
SELECT 'Users:' AS table_name;
SELECT id, username, role, created_at FROM users;

SELECT 'Customers:' AS table_name;
SELECT id, name, email, tags FROM customers;

SELECT 'Templates:' AS table_name;
SELECT id, name, subject FROM mail_templates;
