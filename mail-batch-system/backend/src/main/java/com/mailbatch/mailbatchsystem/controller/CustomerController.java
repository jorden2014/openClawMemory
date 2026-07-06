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
import java.util.List;

/**
 * 客户管理控制器
 * 处理客户的CRUD、导入导出等请求
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 创建客户
     * POST /api/customers
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<CustomerResponse> createCustomer(@Validated @RequestBody CustomerRequest request) {
        log.info("创建客户请求: {}", request.getName());
        CustomerResponse response = customerService.createCustomer(request);
        return Result.success("创建成功", response);
    }

    /**
     * 更新客户
     * PUT /api/customers/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public Result<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Validated @RequestBody CustomerRequest request) {
        log.info("更新客户请求: id={}", id);
        CustomerResponse response = customerService.updateCustomer(id, request);
        return Result.success("更新成功", response);
    }

    /**
     * 删除客户
     * DELETE /api/customers/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteCustomer(@PathVariable Long id) {
        log.info("删除客户请求: id={}", id);
        customerService.deleteCustomer(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除客户
     * DELETE /api/customers/batch
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> batchDeleteCustomers(@RequestBody List<Long> ids) {
        log.info("批量删除客户请求: ids={}", ids);
        customerService.deleteCustomers(ids);
        return Result.success("批量删除成功");
    }

    /**
     * 查询客户详情
     * GET /api/customers/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
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

    /**
     * 导出客户到Excel
     * GET /api/customers/export
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportCustomers() throws IOException {
        log.info("导出客户到Excel");

        byte[] excelData = customerService.exportToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "customers.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    /**
     * 从Excel导入客户
     * POST /api/customers/import
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> importCustomers(@RequestParam("file") MultipartFile file) {
        log.info("导入客户Excel文件: {}", file.getOriginalFilename());

        try {
            int count = customerService.importFromExcel(file);
            return Result.success("成功导入 " + count + " 条客户数据");
        } catch (IOException e) {
            log.error("导入失败: {}", e.getMessage(), e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }
}
