package com.mailbatch.mailbatchsystem.controller;

import com.mailbatch.mailbatchsystem.dto.LoginRequest;
import com.mailbatch.mailbatchsystem.dto.LoginResponse;
import com.mailbatch.mailbatchsystem.dto.Result;
import com.mailbatch.mailbatchsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户登录、注册等认证相关请求
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户登录
     * POST /api/auth/login
     * 
     * @param loginRequest 登录请求（包含用户名和密码）
     * @return 登录响应（包含JWT Token）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest loginRequest) {
        log.info("收到登录请求: {}", loginRequest.getUsername());

        try {
            LoginResponse response = userService.login(loginRequest);
            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(401, "用户名或密码错误");
        }
    }

    /**
     * 用户注册（可选功能）
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<?> register(@RequestParam String username, 
                             @RequestParam String password) {
        log.info("收到注册请求: {}", username);

        try {
            userService.register(username, password);
            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public Result<?> getCurrentUser() {
        try {
            return Result.success("获取成功", userService.getCurrentUser());
        } catch (Exception e) {
            return Result.unauthorized(e.getMessage());
        }
    }
}
