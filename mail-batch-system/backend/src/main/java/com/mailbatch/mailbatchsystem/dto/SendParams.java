package com.mailbatch.mailbatchsystem.dto;

import lombok.Data;

/**
 * 邮件发送参数配置DTO
 */
@Data
public class SendParams {

    /**
     * 每封邮件发送间隔（毫秒）
     */
    private Integer sendInterval;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 批量发送数量限制
     */
    private Integer batchSize;

    /**
     * 是否启用发送限速
     */
    private Boolean rateLimitEnabled;

    /**
     * 限速阈值（每小时发送数量）
     */
    private Integer rateLimitPerHour;
}
