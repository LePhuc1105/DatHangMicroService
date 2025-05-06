package com.example.userservice.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Commenting out CORS configuration as it's handled by API Gateway
// @Configuration
public class CorsConfig {
  // @Override
  //   public void addCorsMappings(CorsRegistry registry) {
  //       registry.addMapping("/**")
  //               .allowedOrigins("http://localhost:8080", "http://localhost", "http://127.0.0.1", "http://localhost:3000")
  //               .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
  //               .allowedHeaders("*")
  //               .allowCredentials(true);
  //   }
}