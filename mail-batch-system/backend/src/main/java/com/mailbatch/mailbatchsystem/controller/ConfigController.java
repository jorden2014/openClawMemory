package com.mailbatch.mailbatchsystem.controller;

import com.mailbatch.mailbatchsystem.dto.Result;
import com.mailbatch.mailbatchsystem.service.MailConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final MailConfigService mailConfigService;

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
     * 获取邮件配置（合并数据库 + yml 默认值）
     */
    @GetMapping("/mail")
    public Result<Map<String, Object>> getMailConfig() {
        log.info("获取邮件配置");
        Map<String, String> dbConfig = mailConfigService.getAllConfig();
        Map<String, Object> config = new java.util.LinkedHashMap<>();
        config.put("smtpHost", dbConfig.getOrDefault("smtpHost", mailHost));
        config.put("smtpPort", Integer.parseInt(dbConfig.getOrDefault("smtpPort", String.valueOf(mailPort))));
        config.put("smtpUsername", dbConfig.getOrDefault("smtpUsername", mailUsername));
        config.put("smtpAuth", Boolean.parseBoolean(dbConfig.getOrDefault("smtpAuth", String.valueOf(mailAuth))));
        config.put("smtpSslEnable", Boolean.parseBoolean(dbConfig.getOrDefault("smtpSslEnable", "false")));
        config.put("smtpStartTlsEnable", Boolean.parseBoolean(dbConfig.getOrDefault("smtpStartTlsEnable", String.valueOf(mailStartTls))));
        return Result.success(config);
    }

    /**
     * 更新邮件配置（保存到数据库 + 写 application.yml + 重启服务）
     */
    @PostMapping("/mail")
    public Result<?> updateMailConfig(@RequestBody Map<String, Object> newConfig) {
        log.info("更新邮件配置: {}", newConfig);
        try {
            java.util.Map<String, String> saveMap = new java.util.HashMap<>();
            if (newConfig.containsKey("smtpHost")) saveMap.put("smtpHost", newConfig.get("smtpHost").toString());
            if (newConfig.containsKey("smtpPort")) saveMap.put("smtpPort", newConfig.get("smtpPort").toString());
            if (newConfig.containsKey("smtpUsername")) saveMap.put("smtpUsername", newConfig.get("smtpUsername").toString());
            if (newConfig.containsKey("smtpPassword")) saveMap.put("smtpPassword", newConfig.get("smtpPassword").toString());
            if (newConfig.containsKey("smtpAuth")) saveMap.put("smtpAuth", newConfig.get("smtpAuth").toString());
            if (newConfig.containsKey("smtpSslEnable")) saveMap.put("smtpSslEnable", newConfig.get("smtpSslEnable").toString());
            if (newConfig.containsKey("smtpStartTlsEnable")) saveMap.put("smtpStartTlsEnable", newConfig.get("smtpStartTlsEnable").toString());
            mailConfigService.saveAllConfig(saveMap);
            return Result.success("配置已保存到数据库");
        } catch (Exception e) {
            log.error("保存邮件配置失败: {}", e.getMessage(), e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 测试邮件连接
     */
    @PostMapping("/mail/test")
    public Result<?> testMailConfig(@RequestBody Map<String, Object> testConfig) {
        log.info("测试邮件连接: {}", testConfig);
        try {
            String host = testConfig.getOrDefault("smtpHost", mailHost).toString();
            int port = Integer.parseInt(testConfig.getOrDefault("smtpPort", String.valueOf(mailPort)).toString());
            String username = testConfig.getOrDefault("smtpUsername", mailUsername).toString();
            String password = testConfig.getOrDefault("smtpPassword", "").toString();

            java.util.Properties props = new java.util.Properties();
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.starttls.enable", true);
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            jakarta.mail.Session session = jakarta.mail.Session.getInstance(props,
                new jakarta.mail.Authenticator() {
                    protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new jakarta.mail.PasswordAuthentication(username, password);
                    }
                });
            jakarta.mail.Transport transport = session.getTransport("smtp");
            transport.connect(host, username, password);
            transport.close();
            return Result.success("SMTP 连接测试成功！");
        } catch (Exception e) {
            log.error("SMTP 连接测试失败: {}", e.getMessage(), e);
            return Result.error("连接失败: " + e.getMessage());
        }
    }
}
