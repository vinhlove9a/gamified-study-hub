package com.gamifiedstudyhub.backend.common.config;

import com.gamifiedstudyhub.backend.common.constant.AppConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${FRONTEND_ORIGIN:http://localhost:5173}")
    private String frontendOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Cookie-based auth requires credentialed CORS, which forbids "*" — the
        // origin must be the explicit frontend origin.
        registry.addMapping(AppConstants.API_PREFIX + "/**")
                .allowedOrigins(frontendOrigin)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-XSRF-TOKEN")
                .allowCredentials(true);
    }
}
