package com.fml.fluxa.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fluxaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FLUXA API")
                        .version("1.0.0")
                        .description("""
                                API REST de FLUXA — Gestión Financiera Personal.

                                **Moneda:** COP (Peso Colombiano)
                                **Zona horaria:** America/Bogota (UTC-5)
                                **Formato fechas:** ISO 8601 en API | DD/MM/YYYY en UI
                                **Autenticación:** JWT Bearer — obtener token en `POST /api/v1/auth/login`
                                """)
                        .contact(new Contact()
                                .name("Equipo FLUXA")
                                .email("soporte@fluxa.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido en POST /api/v1/auth/login. Incluir como: Bearer <token>")));
    }
}
