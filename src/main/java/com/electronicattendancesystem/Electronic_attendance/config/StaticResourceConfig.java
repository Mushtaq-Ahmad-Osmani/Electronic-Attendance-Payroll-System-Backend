package com.electronicattendancesystem.Electronic_attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // محل ذخیره عکس‌های آپلود شده
        String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/Project/profile-picture/");
        registry.addResourceHandler("/notification-images/**")
                .addResourceLocations("file:C:/notification-images/");
    }
}