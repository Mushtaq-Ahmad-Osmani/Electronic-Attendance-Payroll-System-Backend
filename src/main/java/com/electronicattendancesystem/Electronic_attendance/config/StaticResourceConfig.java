package com.electronicattendancesystem.Electronic_attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get("C:/Project/profile-picture").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
        String notificationImagePath = Paths.get("C:/notification-images").toAbsolutePath().toString();
        registry.addResourceHandler("/notification-images/**")
                .addResourceLocations("file:" + notificationImagePath + "/")
                .setCachePeriod(0); // dis activiting the cache for testing
    }
}