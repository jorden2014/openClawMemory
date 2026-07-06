package com.mailbatch.mailbatchsystem.service;

import com.mailbatch.mailbatchsystem.dto.LoginRequest;
import com.mailbatch.mailbatchsystem.dto.LoginResponse;
import com.mailbatch.mailbatchsystem.entity.User;
import com.mailbatch.mailbatchsystem.exception.BusinessException;
import com.mailbatch.mailbatchsystem.repository.UserRepository;
import com.mailbatch.mailbatchsystem.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户服务类
 * 处理用户登录、注册、JWT Token生成等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应（包含JWT Token）
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录: {}", loginRequest.getUsername());

        // 使用Spring Security进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 设置认证信息到SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT Token
        String jwt = tokenProvider.generateToken(authentication);

        // 获取用户信息
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 构建响应
        return LoginResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .expiresIn(86400000)  // 24小时
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    /**
     * 用户注册（可选功能）
     * @param username 用户名
     * @param password 密码
     * @return 注册后的用户
     */
    @Transactional
    public User register(String username, String password) {
        log.info("用户注册: {}", username);

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }

        // 创建新用户
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(User.Role.USER)
                .build();

        return userRepository.save(user);
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户实体
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 获取当前登录用户
     * @return 当前用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}
