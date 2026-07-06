package com.mailbatch.mailbatchsystem.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务执行器配置
 * 用于邮件发送等异步任务
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 邮件发送异步执行器
     * 专用线程池，避免影响主业务
     */
    @Bean(name = "mailSendExecutor")
    public Executor mailSendExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);          // 核心线程数
        executor.setMaxPoolSize(5);            // 最大线程数
        executor.setQueueCapacity(100);        // 队列容量
        executor.setThreadNamePrefix("MailSend-");  // 线程名前缀
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("邮件发送任务被拒绝，可能队列已满");
        });
        executor.initialize();
        return executor;
    }

    /**
     * 通用异步执行器
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("AsyncTask-");
        executor.initialize();
        return executor;
    }
}
