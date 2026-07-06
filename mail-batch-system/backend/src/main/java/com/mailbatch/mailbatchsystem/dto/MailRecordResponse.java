package com.mailbatch.mailbatchsystem.dto;

import com.mailbatch.mailbatchsystem.entity.MailRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件发送记录响应DTO
 */
@Data
@Builder
public class MailRecordResponse {

    private Long id;
    private String batchId;
    private Long customerId;
    private String toEmail;
    private String salutation;
    private String subject;
    private String body;
    private String attachmentPaths;
    private String status;  // 状态字符串
    private Integer retryCount;
    private String errorMsg;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    /**
     * 从实体转换为响应DTO
     */
    public static MailRecordResponse fromEntity(MailRecord record) {
        if (record == null) {
            return null;
        }
        return MailRecordResponse.builder()
                .id(record.getId())
                .batchId(record.getBatchId())
                .customerId(record.getCustomerId())
                .toEmail(record.getToEmail())
                .salutation(record.getSalutation())
                .subject(record.getSubject())
                .body(record.getBody())
                .attachmentPaths(record.getAttachmentPaths())
                .status(record.getStatus() != null ? record.getStatus().name() : null)
                .retryCount(record.getRetryCount())
                .errorMsg(record.getErrorMsg())
                .sentAt(record.getSentAt())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
