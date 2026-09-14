package com.basiltech.sipafin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200", "http://172.16.7.90", "http://172.16.7.90:9090", "http://172.16.7.90:8090", "http://172.16.7.180", "http://172.16.7.180:9080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD", "TRACE")
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true) // Allow credentials
                .maxAge(3600); // Cache preflight requests for 1 hour
    }
}