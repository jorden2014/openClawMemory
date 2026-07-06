package com.mailbatch.mailbatchsystem.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 邮件模板请求DTO
 */
@Data
public class TemplateRequest {

    @NotBlank(message = "模板名称不能为空")
    private String name;

    @NotBlank(message = "邮件主题不能为空")
    private String subject;

    @NotBlank(message = "邮件正文不能为空")
    private String body;

    /**
     * 附件路径列表
     */
    private List<String> attachmentPaths;
}
