package com.mailbatch.mailbatchsystem.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 启用 CORS，禁用 CSRF（前后端分离）
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            
            // Session 管理：使用 Session（前后端分离，但用 Session）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // 需要时创建 Session
                .maximumSessions(1)  // 同一用户只能有一个 Session
                .expiredUrl("/api/auth/session-expired")
            )
            
            // 认证配置：暂时全部放开（开发环境）
            .authorizeHttpRequests(auth -> auth
                // 放行 OPTIONS 请求（CORS 预检）
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 放行上传接口（开发环境）
                .requestMatchers("/api/upload/**").permitAll()
                // 所有请求都允许访问（开发环境）
                .anyRequest().permitAll()
            )
            
            // 表单登录（可选，我们主要用 JSON 登录）
            .formLogin(form -> form.disable())
            
            // 注销配置
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    response.getWriter().write("{\"code\":200,\"message\":\"注销成功\"}");
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            
            // 异常处理（暂时返回 200，不拦截）
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(200);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":200,\"message\":\"ok\",\"data\":null}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(200);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":200,\"message\":\"ok\",\"data\":null}");
                })
            )
            
            // 记住我（可选）
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400)  // 24小时
                .userDetailsService(userDetailsService)
            );

        return http.build();
    }

    /**
     * CORS 配置
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://120.77.255.98:3000",
            "http://120.77.255.98:3003",
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:3003",
            "https://wy2026.top",
            "https://wy2026.top/mail/"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);  // 允许携带 Cookie
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    /**
     * 密码编码器（临时：明文密码，开发环境）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 开发环境：使用 NoOpPasswordEncoder（明文密码）
        // 生产环境应该改用 BCryptPasswordEncoder
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
