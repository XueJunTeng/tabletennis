package com.example.tabletennis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射本地文件路径到 URL
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/uploads/"); // 根据实际存储路径修改
    }

}