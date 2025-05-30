package com.example.cvgenerator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations("file:uploads/photos/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:src/main/resources/images/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/images/");
    }
}