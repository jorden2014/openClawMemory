package com.mailbatch.mailbatchsystem.controller;

import com.mailbatch.mailbatchsystem.dto.*;
import com.mailbatch.mailbatchsystem.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件模板控制器
 * 处理邮件模板的CRUD请求
 */
@Slf4j
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    /**
     * 创建邮件模板
     * POST /api/templates
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<TemplateResponse> createTemplate(@Validated @RequestBody TemplateRequest request) {
        log.info("创建邮件模板: {}", request.getName());
        TemplateResponse response = templateService.createTemplate(request);
        return Result.success("创建成功", response);
    }

    /**
     * 更新邮件模板
     * PUT /api/templates/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<TemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Validated @RequestBody TemplateRequest request) {
        log.info("更新邮件模板: id={}", id);
        TemplateResponse response = templateService.updateTemplate(id, request);
        return Result.success("更新成功", response);
    }

    /**
     * 删除邮件模板
     * DELETE /api/templates/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteTemplate(@PathVariable Long id) {
        log.info("删除邮件模板: id={}", id);
        templateService.deleteTemplate(id);
        return Result.success("删除成功");
    }

    /**
     * 查询模板详情
     * GET /api/templates/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<TemplateResponse> getTemplate(@PathVariable Long id) {
        log.info("查询模板详情: id={}", id);
        TemplateResponse response = templateService.getTemplate(id);
        return Result.success(response);
    }

    /**
     * 分页查询模板列表
     * GET /api/templates?keyword=xxx&page=0&size=10
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<PageResponse<TemplateResponse>> listTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("分页查询模板: keyword={}, page={}, size={}", keyword, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<TemplateResponse> response = templateService.listTemplates(keyword, pageable);

        return Result.success(response);
    }

    /**
     * 获取所有模板（用于下拉选择）
     * GET /api/templates/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<?> getAllTemplates() {
        return Result.success(templateService.getAllTemplates());
    }
}
