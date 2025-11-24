package com.example.transformer_manager_backkend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.directory}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Serve uploads from the primary backend folder, and also fall back to
    // the repository-level ../uploads directory when running locally.
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations(
            "file:" + uploadDir + "/",
            "file:../uploads/")
        .setCachePeriod(3600);
    }
}