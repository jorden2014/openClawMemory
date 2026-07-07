package com.mailbatch.mailbatchsystem.service;

import com.mailbatch.mailbatchsystem.dto.PageResponse;

import com.mailbatch.mailbatchsystem.dto.TemplateRequest;
import com.mailbatch.mailbatchsystem.dto.TemplateResponse;
import com.mailbatch.mailbatchsystem.entity.MailTemplate;
import com.mailbatch.mailbatchsystem.exception.BusinessException;
import com.mailbatch.mailbatchsystem.repository.MailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 邮件模板服务类
 * 处理邮件模板的CRUD业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final MailTemplateRepository templateRepository;

    /**
     * 创建邮件模板
     */
    @Transactional
    public TemplateResponse createTemplate(TemplateRequest request) {
        log.info("创建邮件模板: {}", request.getName());

        // 检查模板名称是否已存在
        if (templateRepository.existsByName(request.getName())) {
            throw new BusinessException("模板名称已存在: " + request.getName());
        }

        // 构建模板实体
        MailTemplate template = MailTemplate.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .body(request.getBody())
                .attachmentPaths(request.getAttachmentPaths() != null ? 
                    String.join(",", request.getAttachmentPaths()) : null)
                .build();

        template = templateRepository.save(template);
        return TemplateResponse.fromEntity(template);
    }

    /**
     * 更新邮件模板
     */
    @Transactional
    public TemplateResponse updateTemplate(Long id, TemplateRequest request) {
        log.info("更新邮件模板: id={}", id);

        MailTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在: " + id));

        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setAttachmentPaths(request.getAttachmentPaths() != null ? 
            String.join(",", request.getAttachmentPaths()) : null);

        template = templateRepository.save(template);
        return TemplateResponse.fromEntity(template);
    }

    /**
     * 删除邮件模板
     */
    @Transactional
    public void deleteTemplate(Long id) {
        log.info("删除邮件模板: id={}", id);

        if (!templateRepository.existsById(id)) {
            throw new BusinessException("模板不存在: " + id);
        }

        templateRepository.deleteById(id);
    }

    /**
     * 根据ID查询模板
     */
    public TemplateResponse getTemplate(Long id) {
        MailTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在: " + id));
        return TemplateResponse.fromEntity(template);
    }

    /**
     * 分页查询模板列表
     */
    public PageResponse<TemplateResponse> listTemplates(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 按名称模糊搜索
            List<MailTemplate> templates = templateRepository.findByNameContainingIgnoreCase(keyword);
            List<TemplateResponse> responses = templates.stream()
                    .map(TemplateResponse::fromEntity)
                    .collect(Collectors.toList());
            
            // 手动分页（简单处理）
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), responses.size());
            List<TemplateResponse> pageContent = start < responses.size() ? 
                responses.subList(start, end) : List.of();
            
            // 构建PageResponse
            return new PageResponse<>(
                pageContent,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                responses.size(),
                (int) Math.ceil((double) responses.size() / pageable.getPageSize()),
                pageable.getPageNumber() == 0,
                end >= responses.size()
            );
        } else {
            Page<MailTemplate> page = templateRepository.findAll(pageable);
            Page<TemplateResponse> responsePage = page.map(TemplateResponse::fromEntity);
            return PageResponse.fromPage(responsePage);
        }
    }

    /**
     * 查询所有模板（用于下拉选择）
     */
    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(TemplateResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
