package com.mailbatch.mailbatchsystem.dto;

import com.mailbatch.mailbatchsystem.entity.MailTemplate;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件模板响应DTO
 */
@Data
@Builder
public class TemplateResponse {

    private Long id;
    private String name;
    private String subject;
    private String body;
    private String attachmentPaths;  // 逗号分隔的字符串
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为响应DTO
     */
    public static TemplateResponse fromEntity(MailTemplate template) {
        if (template == null) {
            return null;
        }
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .subject(template.getSubject())
                .body(template.getBody())
                .attachmentPaths(template.getAttachmentPaths())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
