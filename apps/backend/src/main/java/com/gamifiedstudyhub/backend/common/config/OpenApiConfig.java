package com.gamifiedstudyhub.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gamifiedStudyHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gamified Study Hub API")
                        .version("v0.1.0")
                        .description("API documentation for Gamified Study Hub backend"))
                .servers(List.of(new Server().url("/")));
    }
}
