package com.mailbatch.mailbatchsystem.controller;

import com.mailbatch.mailbatchsystem.dto.CustomerRequest;
import com.mailbatch.mailbatchsystem.dto.CustomerResponse;
import com.mailbatch.mailbatchsystem.dto.PageResponse;
import com.mailbatch.mailbatchsystem.dto.Result;
import com.mailbatch.mailbatchsystem.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public Result<CustomerResponse> createCustomer(@Validated @RequestBody CustomerRequest request) {
        log.info("创建客户请求: {}", request.getName());
        CustomerResponse response = customerService.createCustomer(request);
        return Result.success("创建成功", response);
    }

    @PutMapping("/{id}")
    public Result<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Validated @RequestBody CustomerRequest request) {
        log.info("更新客户请求: id={}", id);
        CustomerResponse response = customerService.updateCustomer(id, request);
        return Result.success("更新成功", response);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteCustomer(@PathVariable Long id) {
        log.info("删除客户请求: id={}", id);
        customerService.deleteCustomer(id);
        return Result.success("删除成功");
    }

    @DeleteMapping("/batch")
    public Result<?> batchDeleteCustomers(@RequestBody List<Long> ids) {
        log.info("批量删除客户请求: ids={}", ids);
        customerService.deleteCustomers(ids);
        return Result.success("批量删除成功");
    }

    /**
     * 获取所有标签
     * GET /api/customers/tags
     * 注意：此接口必须放在 /{id} 之前，否则 "tags" 会被当成 id 参数
     */
    @GetMapping("/tags")
    public Result<List<String>> getAllTags() {
        log.info("获取所有标签");
        try {
            List<String> tags = customerService.getAllTags();
            return Result.success(tags);
        } catch (Exception e) {
            log.warn("获取标签失败: {}", e.getMessage());
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 导出客户到Excel
     * GET /api/customers/export
     * 注意：此接口必须放在 /{id} 之前
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCustomers() throws IOException {
        log.info("导出客户到Excel");
        byte[] excelData = customerService.exportToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "customers.xlsx");
        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    /**
     * 从Excel导入客户
     * POST /api/customers/import
     */
    @PostMapping("/import")
    public Result<?> importCustomers(@RequestParam("file") MultipartFile file) {
        log.info("导入客户Excel文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return Result.error("请选择要导入的文件");
        }

        try {
            // 返回包含成功/失败条数的对象
            int count = customerService.importFromExcel(file);
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("success", count);
            data.put("failed", 0); // TODO: 实现失败计数
            data.put("errors", new java.util.ArrayList<String>());
            return Result.success("导入完成", data);
        } catch (IOException e) {
            log.error("导入失败: {}", e.getMessage(), e);
            return Result.error("导入失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("导入失败: {}", e.getMessage(), e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 查询客户详情
     * GET /api/customers/{id}
     * 注意：此接口必须放在 /tags、/export 等具体路径之后
     */
    @GetMapping("/{id}")
    public Result<CustomerResponse> getCustomer(@PathVariable Long id) {
        log.info("查询客户详情: id={}", id);
        CustomerResponse response = customerService.getCustomer(id);
        return Result.success(response);
    }

    /**
     * 分页查询客户列表
     * GET /api/customers?keyword=xxx&tag=xxx&page=0&size=10
     */
    @GetMapping
    public Result<PageResponse<CustomerResponse>> listCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询客户: keyword={}, tag={}, page={}, size={}", keyword, tag, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<CustomerResponse> response = customerService.listCustomers(keyword, tag, pageable);
        return Result.success(response);
    }
}
