package com.mailbatch.mailbatchsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mail_config")
public class MailConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 配置项 key，如 smtpHost、smtpPort、username、password */
    @Column(unique = true, nullable = false, length = 100)
    private String configKey;

    @Column(length = 500)
    private String configValue;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private Boolean encrypted = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
