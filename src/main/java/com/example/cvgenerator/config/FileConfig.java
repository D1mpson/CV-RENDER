package com.example.cvgenerator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Видаляємо налаштування для локальних uploads, оскільки використовуємо Cloudinary
        // Залишаємо тільки статичні ресурси
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/images/");
    }
}