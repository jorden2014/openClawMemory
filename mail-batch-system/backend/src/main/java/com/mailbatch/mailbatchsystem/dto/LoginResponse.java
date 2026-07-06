package com.mailbatch.mailbatchsystem.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
@Builder
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token类型
     */
    private String tokenType;

    /**
     * 过期时间（秒）
     */
    private long expiresIn;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色
     */
    private String role;
}
