package com.mailbatch.mailbatchsystem.repository;

import com.mailbatch.mailbatchsystem.entity.MailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 邮件模板数据访问接口
 */
@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {

    /**
     * 根据名称查询模板
     * @param name 模板名称
     * @return 模板列表
     */
    List<MailTemplate> findByNameContainingIgnoreCase(String name);

    /**
     * 检查模板名称是否已存在
     * @param name 模板名称
     * @return 是否存在
     */
    boolean existsByName(String name);
}
