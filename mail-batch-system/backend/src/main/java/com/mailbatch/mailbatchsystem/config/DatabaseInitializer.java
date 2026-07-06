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

            // 执行初始化SQL脚本
            Resource resource = new ClassPathResource("schema.sql");
            if (resource.exists()) {
                log.info("执行数据库初始化脚本: schema.sql");
                ScriptUtils.executeSqlScript(connection, resource);
                log.info("数据库初始化完成");
            } else {
                log.warn("未找到数据库初始化脚本: schema.sql");
            }

        } catch (SQLException e) {
            log.error("数据库初始化失败: {}", e.getMessage(), e);
        }
    }
}
