package com.mailbatch.mailbatchsystem.service;

import com.mailbatch.mailbatchsystem.dto.CustomerRequest;
import com.mailbatch.mailbatchsystem.dto.CustomerResponse;
import com.mailbatch.mailbatchsystem.entity.Customer;
import com.mailbatch.mailbatchsystem.exception.BusinessException;
import com.mailbatch.mailbatchsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户服务类
 * 处理客户信息的CRUD、导入导出等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * 创建客户
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("创建客户: {}", request.getName());

        // 检查邮箱是否已存在
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已存在: " + request.getEmail());
        }

        // 构建客户实体
        Customer customer = Customer.builder()
                .name(request.getName())
                .salutation(request.getSalutation())
                .email(request.getEmail())
                .tags(request.getTags() != null ? String.join(",", request.getTags()) : null)
                .remark(request.getRemark())
                .build();

        customer = customerRepository.save(customer);
        return CustomerResponse.fromEntity(customer);
    }

    /**
     * 更新客户
     */
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.info("更新客户: id={}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在: " + id));

        // 检查邮箱是否被其他客户使用
        Customer existing = customerRepository.findByEmail(request.getEmail());
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("邮箱已被其他客户使用: " + request.getEmail());
        }

        customer.setName(request.getName());
        customer.setSalutation(request.getSalutation());
        customer.setEmail(request.getEmail());
        customer.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);
        customer.setRemark(request.getRemark());

        customer = customerRepository.save(customer);
        return CustomerResponse.fromEntity(customer);
    }

    /**
     * 删除客户
     */
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("删除客户: id={}", id);

        if (!customerRepository.existsById(id)) {
            throw new BusinessException("客户不存在: " + id);
        }

        customerRepository.deleteById(id);
    }

    /**
     * 批量删除客户
     */
    @Transactional
    public void deleteCustomers(List<Long> ids) {
        log.info("批量删除客户: ids={}", ids);
        customerRepository.deleteByIds(ids);
    }

    /**
     * 根据ID查询客户
     */
    public CustomerResponse getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("客户不存在: " + id));
        return CustomerResponse.fromEntity(customer);
    }

    /**
     * 分页查询客户（支持搜索和筛选）
     */
    public PageResponse<CustomerResponse> listCustomers(
            String keyword, String tag, Pageable pageable) {
        
        Specification<Customer> spec = Specification.where(null);
        
        // 关键词搜索（名称、邮箱、称呼）
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.or(
                    criteriaBuilder.like(root.get("name"), likeKeyword),
                    criteriaBuilder.like(root.get("email"), likeKeyword),
                    criteriaBuilder.like(root.get("salutation"), likeKeyword)
                )
            );
        }
        
        // 按标签筛选
        if (tag != null && !tag.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.like(root.get("tags"), "%" + tag.trim() + "%")
            );
        }

        Page<Customer> page = customerRepository.findAll(spec, pageable);
        Page<CustomerResponse> responsePage = page.map(CustomerResponse::fromEntity);
        
        return PageResponse.fromPage(responsePage);
    }

    /**
     * 导出客户到Excel
     */
    public byte[] exportToExcel() throws IOException {
        log.info("导出客户到Excel");
        
        List<Customer> customers = customerRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("客户列表");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "姓名", "称呼", "邮箱", "标签", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                // 设置表头样式
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            for (int i = 0; i < customers.size(); i++) {
                Customer customer = customers.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(customer.getId());
                row.createCell(1).setCellValue(customer.getName());
                row.createCell(2).setCellValue(customer.getSalutation());
                row.createCell(3).setCellValue(customer.getEmail());
                row.createCell(4).setCellValue(customer.getTags());
                row.createCell(5).setCellValue(customer.getRemark());
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * 从Excel导入客户
     */
    @Transactional
    public int importFromExcel(MultipartFile file) throws IOException {
        log.info("从Excel导入客户: {}", file.getOriginalFilename());
        
        int count = 0;
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 跳过表头行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    String name = getCellValue(row.getCell(1));  // 姓名
                    String email = getCellValue(row.getCell(3)); // 邮箱
                    
                    if (name.isEmpty() || email.isEmpty()) continue;
                    
                    // 检查邮箱是否已存在
                    if (customerRepository.existsByEmail(email)) {
                        log.warn("邮箱已存在，跳过: {}", email);
                        continue;
                    }
                    
                    Customer customer = Customer.builder()
                            .name(name)
                            .salutation(getCellValue(row.getCell(2)))
                            .email(email)
                            .tags(getCellValue(row.getCell(4)))
                            .remark(getCellValue(row.getCell(5)))
                            .build();
                    
                    customerRepository.save(customer);
                    count++;
                } catch (Exception e) {
                    log.error("导入第{}行失败: {}", i + 1, e.getMessage());
                }
            }
        }
        
        log.info("成功导入{}条客户数据", count);
        return count;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
