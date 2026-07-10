package com.mailbatch.mailbatchsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * 邮件批量发送系统启动类
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAsync
public class MailBatchSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailBatchSystemApplication.class, args);
        System.out.println("🚀 邮件批量发送系统启动成功！");
    }
}
