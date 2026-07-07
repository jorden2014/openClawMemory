package com.mailbatch.mailbatchsystem.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库初始化组件
 * 在应用启动时执行SQL初始化脚本
 */
@Slf4j
@Component
public class DatabaseInitializer {

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initialize() {
        log.info("开始初始化数据库...");

        try (Connection connection = dataSource.getConnection()) {
            // 检查是否已经有数据
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                log.info("数据库已初始化，跳过初始化步骤");
                return;
            }

            // 跳过 SQL 脚本执行（手动初始化）
            log.info("跳过 schema.sql 执行，使用 Hibernate 自动建表");
            
            // 确保 admin 用户存在
            var checkAdmin = connection.createStatement();
            var adminResult = checkAdmin.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'");
            adminResult.next();
            if (adminResult.getInt(1) == 0) {
                // 创建 admin 用户（密码: admin123）
                var createAdmin = connection.createStatement();
                // BCrypt hash for 'admin123'
                String adminPassword = "$2a$10$DmG.AxJ4sy5BD.b2V6cIIu8fLrk44/hH.lEDye5p9hEOeSjz2B5p2";
                createAdmin.executeUpdate("INSERT INTO users (username, password, role) VALUES ('admin', '" + adminPassword + "', 'ADMIN')");
                log.info("创建 admin 用户完成");
            }

        } catch (SQLException e) {
            log.error("数据库初始化失败: {}", e.getMessage(), e);
        }
    }
}
