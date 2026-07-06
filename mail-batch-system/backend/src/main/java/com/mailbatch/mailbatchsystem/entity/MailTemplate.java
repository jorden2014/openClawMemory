package com.mailbatch.mailbatchsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 邮件模板实体类
 * 用于保存邮件模板，支持占位符替换
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mail_templates")
@EntityListeners(AuditingEntityListener.class)
public class MailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 邮件主题，支持占位符
     * 例如：您好，{{称呼}}，欢迎使用我们的服务
     */
    @Column(nullable = false, length = 500)
    private String subject;

    /**
     * 邮件正文（HTML格式），支持占位符
     * 例如：<p>尊敬的{{称呼}}：</p><p>您好！</p>
     */
    @Column(columnDefinition = "TEXT")
    private String body;

    /**
     * 附件路径，多个附件用逗号分隔
     * 例如：/path/to/file1.pdf,/path/to/file2.docx
     */
    @Column(columnDefinition = "TEXT")
    private String attachmentPaths;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 获取附件路径数组
     */
    public String[] getAttachmentPathArray() {
        if (attachmentPaths == null || attachmentPaths.trim().isEmpty()) {
            return new String[0];
        }
        return attachmentPaths.split(",");
    }

    /**
     * 设置附件路径（从数组转换为逗号分隔字符串）
     */
    public void setAttachmentPathArray(String[] paths) {
        if (paths == null || paths.length == 0) {
            this.attachmentPaths = null;
        } else {
            this.attachmentPaths = String.join(",", paths);
        }
    }
}
