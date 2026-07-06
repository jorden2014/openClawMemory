package com.mailbatch.mailbatchsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 邮件发送记录实体类
 * 记录每封邮件的发送状态和历史
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mail_records", indexes = {
    @Index(name = "idx_batch_id", columnList = "batchId"),
    @Index(name = "idx_customer_id", columnList = "customerId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_sent_at", columnList = "sentAt")
})
@EntityListeners(AuditingEntityListener.class)
public class MailRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 批次ID，用于标识同一批发送任务
     */
    @Column(nullable = false, length = 50)
    private String batchId;

    /**
     * 客户ID（外键）
     */
    @Column(nullable = false)
    private Long customerId;

    /**
     * 收件人邮箱
     */
    @Column(nullable = false, length = 200)
    private String toEmail;

    /**
     * 称呼（发送时实际使用的称呼）
     */
    @Column(length = 50)
    private String salutation;

    /**
     * 邮件主题
     */
    @Column(nullable = false, length = 500)
    private String subject;

    /**
     * 邮件正文（替换占位符后的实际内容）
     */
    @Column(columnDefinition = "TEXT")
    private String body;

    /**
     * 附件路径（实际发送时的附件）
     */
    @Column(columnDefinition = "TEXT")
    private String attachmentPaths;

    /**
     * 发送状态枚举
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    /**
     * 重试次数
     */
    @Column(nullable = false)
    private Integer retryCount;

    /**
     * 错误信息（发送失败时记录）
     */
    @Column(columnDefinition = "TEXT")
    private String errorMsg;

    /**
     * 实际发送时间
     */
    private LocalDateTime sentAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 邮件发送状态枚举
     */
    public enum Status {
        PENDING,    // 待发送
        SENDING,    // 发送中
        SENT,       // 已发送
        FAILED      // 发送失败
    }

    /**
     * 初始化默认值
     */
    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = Status.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}
