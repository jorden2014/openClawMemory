package com.mailbatch.mailbatchsystem.dto;

import lombok.Data;

/**
 * SMTP配置DTO
 */
@Data
public class SmtpConfig {

    /**
     * SMTP服务器地址
     */
    private String host;

    /**
     * SMTP服务器端口
     */
    private Integer port;

    /**
     * 发件人邮箱
     */
    private String username;

    /**
     * 邮箱密码或授权码
     */
    private String password;

    /**
     * 是否启用SSL
     */
    private Boolean sslEnable;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectionTimeout;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer timeout;
}
