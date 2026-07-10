package com.mailbatch.mailbatchsystem.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置
 * 配置静态资源映射
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将上传目录映射为静态资源
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        String uploadAbsolutePath = uploadDir.toString() + "/";

        log.info("映射静态资源: {} -> {}", urlPrefix + "/**", uploadAbsolutePath);

        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + uploadAbsolutePath);
    }
}
