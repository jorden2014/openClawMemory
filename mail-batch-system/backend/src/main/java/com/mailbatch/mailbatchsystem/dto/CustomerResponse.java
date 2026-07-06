package com.mailbatch.mailbatchsystem.dto;

import com.mailbatch.mailbatchsystem.entity.Customer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 客户响应DTO
 */
@Data
@Builder
public class CustomerResponse {

    private Long id;
    private String name;
    private String salutation;
    private String email;
    private String tags;  // 逗号分隔的字符串
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为响应DTO
     */
    public static CustomerResponse fromEntity(Customer customer) {
        if (customer == null) {
            return null;
        }
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .salutation(customer.getSalutation())
                .email(customer.getEmail())
                .tags(customer.getTags())
                .remark(customer.getRemark())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    /**
     * 获取标签数组
     */
    public String[] getTagArray() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }
}
