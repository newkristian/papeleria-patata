package com.kristianconk.api_papeleria.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Papelería Patata POS API",
        version = "1.0",
        description = "Documentación de la API REST para el sistema Point of Sale (POS) de Papelería Patata."
    ),
    security = @SecurityRequirement(name = "BearerAuth")
)
@SecurityScheme(
    name = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Ingrese el token JWT para autenticar las peticiones en Swagger UI."
)
public class OpenApiConfig {
}
