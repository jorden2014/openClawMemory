package com.mailbatch.mailbatchsystem.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 客户请求DTO
 */
@Data
public class CustomerRequest {

    @NotBlank(message = "客户名称不能为空")
    private String name;

    /**
     * 称呼（用于邮件模板中的{{称呼}}占位符替换）
     */
    private String salutation;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 备注
     */
    private String remark;
}
