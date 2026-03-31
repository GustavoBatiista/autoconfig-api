package com.gustavobatista.autoconfig.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_JWT = "bearer-jwt";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AutoConfig API")
                        .description("API for AutoConfig")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT obtido em POST /auth/login (cole só o token, sem o prefixo Bearer).")));
    }
}