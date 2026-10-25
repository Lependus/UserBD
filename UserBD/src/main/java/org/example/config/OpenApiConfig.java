package org.example.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info
                (title = "UserAPI",
                version = "1.0",
                description = "REST API для создания, просмотра, изменения и удаления пользователей"))
public class OpenApiConfig {

}
