package com.mailbatch.mailbatchsystem.service;

import com.mailbatch.mailbatchsystem.entity.MailConfig;
import com.mailbatch.mailbatchsystem.repository.MailConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailConfigService {

    private final MailConfigRepository mailConfigRepository;

    @Value("${spring.mail.host:smtp.qq.com}")
    private String defaultHost;

    @Value("${spring.mail.port:587}")
    private int defaultPort;

    @Value("${spring.mail.username:}")
    private String defaultUsername;

    @Value("${spring.mail.password:}")
    private String defaultPassword;

    /** 默认配置 key 列表 */
    public static final List<String> CONFIG_KEYS = List.of(
        "smtpHost", "smtpPort", "smtpUsername", "smtpPassword",
        "smtpAuth", "smtpSslEnable", "smtpStartTlsEnable"
    );

    /**
     * 获取所有邮件配置（优先数据库，没有则读 yml 默认值）
     */
    public Map<String, String> getAllConfig() {
        Map<String, String> config = new LinkedHashMap<>();
        config.put("smtpHost", getConfigValue("smtpHost", defaultHost));
        config.put("smtpPort", getConfigValue("smtpPort", String.valueOf(defaultPort)));
        config.put("smtpUsername", getConfigValue("smtpUsername", defaultUsername));
        config.put("smtpPassword", getConfigValue("smtpPassword", defaultPassword));
        config.put("smtpAuth", getConfigValue("smtpAuth", "true"));
        config.put("smtpSslEnable", getConfigValue("smtpSslEnable", "false"));
        config.put("smtpStartTlsEnable", getConfigValue("smtpStartTlsEnable", "true"));
        return config;
    }

    /**
     * 获取单个配置值
     */
    public String getConfigValue(String key, String defaultValue) {
        return mailConfigRepository.findByConfigKey(key)
            .map(MailConfig::getConfigValue)
            .orElse(defaultValue);
    }

    /**
     * 保存配置（全量覆盖）
     */
    @Transactional
    public void saveAllConfig(Map<String, String> configMap) {
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            MailConfig config = mailConfigRepository.findByConfigKey(key)
                .orElse(new MailConfig());
            config.setConfigKey(key);
            config.setConfigValue(value);
            if (config.getId() == null) {
                config.setDescription(key + " 配置");
            }
            mailConfigRepository.save(config);
        }
        log.info("邮件配置已保存到数据库");
    }

    /**
     * 获取用于 JavaMail 的配置 Map
     */
    public Properties getMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", Boolean.parseBoolean(getConfigValue("smtpAuth", "true")));
        props.put("mail.smtp.ssl.enable", Boolean.parseBoolean(getConfigValue("smtpSslEnable", "false")));
        props.put("mail.smtp.starttls.enable", Boolean.parseBoolean(getConfigValue("smtpStartTlsEnable", "true")));
        return props;
    }

    /**
     * 获取 SMTP host
     */
    public String getSmtpHost() {
        return getConfigValue("smtpHost", defaultHost);
    }

    /**
     * 获取 SMTP port
     */
    public int getSmtpPort() {
        return Integer.parseInt(getConfigValue("smtpPort", String.valueOf(defaultPort)));
    }

    /**
     * 获取用户名
     */
    public String getUsername() {
        return getConfigValue("smtpUsername", defaultUsername);
    }

    /**
     * 获取密码
     */
    public String getPassword() {
        return getConfigValue("smtpPassword", defaultPassword);
    }
}
