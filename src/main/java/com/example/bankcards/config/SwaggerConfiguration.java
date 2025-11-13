package com.example.bankcards.config;

import com.example.bankcards.dto.response.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация swagger ui, spring docs
 */
@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Rest")
                        .version("1.0")
                        .description("API Documentation with JWT authentication"))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)));
    }

    /**
     * Добавляем автоматически тело в документацию ко всем ошибкам
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            //Получаем схему ошибки
            var sharedErrorSchema = ModelConverters.getInstance()
                    .read(ErrorResponse.class).get(ErrorResponse.class.getSimpleName());
            if (sharedErrorSchema == null) {
                throw new IllegalStateException(
                        "Не удалось сгенерировать ответ для ошибок 4xx и 5xx, поскольку отсутствует схема ошибки");
            }

            //Добавляем тело ответа ко всем ответам с кодами 4xx и 5xx
            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation ->
                    operation.getResponses().forEach((status, response) -> {
                        if (status.startsWith("4") || status.startsWith("5")) {
                            response.getContent().forEach((code, mediaType) -> mediaType.setSchema(sharedErrorSchema));
                        }
                    })));
        };
    }
}

