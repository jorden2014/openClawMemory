package com.mailbatch.mailbatchsystem.repository;

import com.mailbatch.mailbatchsystem.entity.MailConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MailConfigRepository extends JpaRepository<MailConfig, Long> {
    Optional<MailConfig> findByConfigKey(String configKey);
}
