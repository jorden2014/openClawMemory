package com.mailbatch.mailbatchsystem.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 发送邮件请求DTO
 */
@Data
public class SendMailRequest {

    /**
     * 模板ID（可选，如果提供则使用模板）
     */
    private Long templateId;

    /**
     * 客户ID列表
     */
    @NotEmpty(message = "客户列表不能为空")
    private List<Long> customerIds;

    /**
     * 邮件主题（如果不使用模板则必填）
     */
    private String subject;

    /**
     * 邮件正文（如果不使用模板则必填，支持HTML）
     */
    private String body;

    /**
     * 附件路径列表（可选）
     */
    private List<String> attachmentPaths;

    /**
     * 批次ID（可选，由后端生成）
     */
    private String batchId;
}
