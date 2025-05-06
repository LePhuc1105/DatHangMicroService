package com.example.cartservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Commenting out CORS configuration as it's handled by API Gateway
// @Configuration
public class CorsConfig {
   // @Override
   //  public void addCorsMappings(CorsRegistry registry) {
   //      registry.addMapping("/**")
   //              .allowedOrigins("*")
   //              .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
   //              .allowedHeaders("*");
   //  }
}
