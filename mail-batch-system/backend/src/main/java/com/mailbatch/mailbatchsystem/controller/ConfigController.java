package com.mailbatch.mailbatchsystem.controller;

import com.mailbatch.mailbatchsystem.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController()
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean mailAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean mailStartTls;

    /**
     * 获取邮件配置
     */
    @GetMapping("/mail")
    public Result<Map<String, Object>> getMailConfig() {
        log.info("获取邮件配置");
        Map<String, Object> config = Map.of(
            "mailHost", mailHost,
            "mailPort", mailPort,
            "mailUsername", mailUsername,
            "mailAuth", mailAuth,
            "mailStartTls", mailStartTls
        );
        return Result.success(config);
    }

    /**
     * 更新邮件配置（保存到 application.yml）
     */
    @PostMapping("/mail")
    public Result<?> updateMailConfig(@RequestBody Map<String, Object> newConfig) {
        log.info("更新邮件配置: {}", newConfig);
        // TODO: 实际项目中应该保存到数据库或配置文件
        // 这里先返回成功，后续可以扩展为写入 application.yml 或数据库
        return Result.success("配置已保存（待实现写入）");
    }

    /**
     * 测试邮件连接
     */
    @PostMapping("/mail/test")
    public Result<?> testMailConfig(@RequestBody Map<String, Object> testConfig) {
        log.info("测试邮件连接: {}", testConfig);
        // TODO: 实际测试 SMTP 连接
        return Result.success("连接测试成功（待实现）");
    }
}
