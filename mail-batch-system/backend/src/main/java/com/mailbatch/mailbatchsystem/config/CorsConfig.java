package com.mailbatch.mailbatchsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS跨域配置
 * 允许前端应用跨域访问后端API
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     * @return CORS过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        // 允许的来源（前端地址）
        corsConfiguration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",     // React开发服务器
            "http://localhost:8080",     // Vue开发服务器
            "http://localhost:5173",     // Vite开发服务器
            "http://127.0.0.1:3000",
            "http://127.0.0.1:8080",
            "http://127.0.0.1:5173"
        ));
        
        // 允许所有来源（生产环境应该指定具体域名）
        // corsConfiguration.addAllowedOriginPattern("*");
        
        // 允许的请求方法
        corsConfiguration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // 允许的请求头
        corsConfiguration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // 暴露的响应头
        corsConfiguration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition"
        ));
        
        // 是否允许发送Cookie
        corsConfiguration.setAllowCredentials(true);
        
        // 预检请求的有效期（秒）
        corsConfiguration.setMaxAge(3600L);

        // 注册CORS配置到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsFilter(source);
    }
}
