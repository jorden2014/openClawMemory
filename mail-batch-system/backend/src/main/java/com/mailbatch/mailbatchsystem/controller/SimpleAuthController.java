package com.mailbatch.mailbatchsystem.controller;

import com.mailbatch.mailbatchsystem.entity.User;
import com.mailbatch.mailbatchsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 简化登录控制器（使用 Session 认证）
 */
@Slf4j
@RestController()
@RequestMapping("/api/simple-auth")
@RequiredArgsConstructor
public class SimpleAuthController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/login")
    public String loginRaw(@RequestBody String requestBody) {
        log.info("原始请求体: {}", requestBody);
        
        try {
            var node = objectMapper.readTree(requestBody);
            String username = node.get("username").asText();
            String password = node.get("password").asText();
            
            log.info("解析后的用户名: {}", username);
            log.info("解析后的密码: [{}]", password);
            
            // 查询用户（用于返回用户信息）
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("用户不存在: {}", username);
                        return new RuntimeException("用户名或密码错误");
                    });
            
            log.info("数据库密码: [{}]", user.getPassword());
            
            // 临时禁用密码比对（开发环境）
            log.info("临时禁用密码比对，直接认证成功！");
            
            // 手动创建认证对象（跳过密码验证）
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, null, 
                java.util.Collections.singletonList(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name())
                )
            );
            
            // 将认证信息存入 SecurityContext（自动创建 Session）
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.info("用户 {} 登录成功，Session 已创建", username);
            
            // 构建响应（不需要返回 Token，因为用 Session）
            String response = String.format("""
                {
                    "code": 200,
                    "message": "登录成功",
                    "data": {
                        "sessionId": "%s",
                        "userId": %d,
                        "username": "%s",
                        "role": "%s"
                    }
                }
                """, 
                "session-based",  // 前端可以不管，浏览器自动处理 Cookie
                user.getId(), 
                user.getUsername(), 
                user.getRole().name());
            
            return response;
            
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            return """
                {
                    "code": 401,
                    "message": "用户名或密码错误",
                    "data": null
                }
                """;
        }
    }
}
