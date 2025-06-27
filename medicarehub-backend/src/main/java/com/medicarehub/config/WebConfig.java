package com.medicarehub.config; // <<<< DOUBLE CHECK THIS PACKAGE NAME!

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; // If your IDE adds this, fine. If not, it's okay to remove.
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // <<<< ESSENTIAL: Spring needs to know this is a configuration class
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) { // @NonNull is optional
        System.out.println("!!!!!!!!!!! Configuring CORS in WebConfig !!!!!!!!!!"); // DEBUG LINE
        registry.addMapping("/api/**") // Apply to all paths under /api
                .allowedOrigins("http://localhost:5173") // <<<< YOUR FRONTEND ORIGIN (Port 5176 from error)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // OPTIONS IS CRITICAL
                .allowedHeaders("*") // Allows all standard and custom headers
                .allowCredentials(true) // Allows cookies/authorization headers
                .maxAge(3600); // Cache preflight response for 1 hour
    }
}