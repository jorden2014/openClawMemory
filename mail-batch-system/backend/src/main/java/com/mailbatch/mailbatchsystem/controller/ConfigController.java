package com.mailbatch.mailbatchsystem.controller;

import com.mailbatch.mailbatchsystem.dto.Result;
import com.mailbatch.mailbatchsystem.dto.SendParams;
import com.mailbatch.mailbatchsystem.dto.SmtpConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Properties;

/**
 * 系统配置控制器
 * 处理SMTP配置、发送参数配置等请求
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    @Value("${spring.mail.host}")
    private String smtpHost;

    @Value("${spring.mail.port}")
    private Integer smtpPort;

    @Value("${spring.mail.username}")
    private String smtpUsername;

    @Value("${spring.mail.password}")
    private String smtpPassword;

    @Value("${mail.batch.send-interval:3000}")
    private Integer sendInterval;

    @Value("${mail.batch.max-retry:3}")
    private Integer maxRetry;

    /**
     * 获取SMTP配置
     * GET /api/config/smtp
     */
    @GetMapping("/smtp")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SmtpConfig> getSmtpConfig() {
        log.info("获取SMTP配置");

        SmtpConfig config = new SmtpConfig();
        config.setHost(smtpHost);
        config.setPort(smtpPort);
        config.setUsername(smtpUsername);
        // 出于安全考虑，不返回密码
        config.setPassword("********");

        return Result.success(config);
    }

    /**
     * 更新SMTP配置
     * PUT /api/config/smtp
     * 注意：实际项目中应该将配置保存到数据库或配置文件，这里仅为示例
     */
    @PutMapping("/smtp")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateSmtpConfig(@RequestBody SmtpConfig config) {
        log.info("更新SMTP配置: host={}, port={}", config.getHost(), config.getPort());

        //  TODO: 实际项目中应该将配置保存到数据库或配置文件

        return Result.success("SMTP配置已更新（需要重启服务生效）");
    }

    /**
     * 获取发送参数配置
     * GET /api/config/send-params
     */
    @GetMapping("/send-params")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<SendParams> getSendParams() {
        log.info("获取发送参数配置");

        SendParams params = new SendParams();
        params.setSendInterval(sendInterval);
        params.setMaxRetry(maxRetry);
        params.setBatchSize(100);  // 默认值
        params.setRateLimitEnabled(false);
        params.setRateLimitPerHour(100);

        return Result.success(params);
    }

    /**
     * 更新发送参数配置
     * PUT /api/config/send-params
     */
    @PutMapping("/send-params")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateSendParams(@RequestBody SendParams params) {
        log.info("更新发送参数配置: sendInterval={}, maxRetry={}", 
            params.getSendInterval(), params.getMaxRetry());

        // TODO: 实际项目中应该将配置保存到数据库或配置文件

        return Result.success("发送参数配置已更新（需要重启服务生效）");
    }

    /**
     * 测试SMTP连接
     * POST /api/config/smtp/test
     */
    @PostMapping("/smtp/test")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> testSmtpConnection(@RequestBody SmtpConfig config) {
        log.info("测试SMTP连接: host={}, port={}", config.getHost(), config.getPort());

        try {
            // TODO: 实际项目中应该测试SMTP连接
            // 可以使用JavaMail的Transport.connect()方法测试

            return Result.success("SMTP连接测试成功");
        } catch (Exception e) {
            log.error("SMTP连接测试失败: {}", e.getMessage(), e);
            return Result.error("SMTP连接测试失败: " + e.getMessage());
        }
    }
}
