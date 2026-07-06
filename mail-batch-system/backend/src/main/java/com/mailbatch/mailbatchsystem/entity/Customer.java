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
 * 客户实体类
 * 存储收件人信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 称呼（用于邮件模板中的{{称呼}}占位符替换）
     */
    @Column(length = 50)
    private String salutation;

    @Column(unique = true, nullable = false, length = 200)
    private String email;

    /**
     * 标签，多个标签用逗号分隔
     * 例如：VIP,潜在客户,已成交
     */
    @Column(length = 500)
    private String tags;

    @Column(length = 1000)
    private String remark;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 获取标签数组
     */
    public String[] getTagArray() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }

    /**
     * 设置标签（从数组转换为逗号分隔字符串）
     */
    public void setTagArray(String[] tagArray) {
        if (tagArray == null || tagArray.length == 0) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagArray);
        }
    }
}
