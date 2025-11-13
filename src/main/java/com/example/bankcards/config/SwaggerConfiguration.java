package com.example.bankcards.config;

import com.example.bankcards.dto.response.ErrorResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
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
                        .description("Приложения для управления пользователями и картами с JWT авторизацией"))
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
     * Метод для добавления ErrorResponse тела ошибок по умолчанию.
     * Чтобы не указывать вручную каждый раз, а так без тела   @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
     */
    @Bean
    public OpenApiCustomizer forceErrorResponseSchema() {
        return openApi -> {
            // Регистрируем схему ErrorResponse
            var resolvedSchema = ModelConverters.getInstance()
                    .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.class));

            if (resolvedSchema == null || resolvedSchema.schema == null) return;

            openApi.getComponents().addSchemas("ErrorResponse", resolvedSchema.schema);

            Schema<?> errorSchemaRef = new Schema<>();
            errorSchemaRef.set$ref("#/components/schemas/ErrorResponse");

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation ->
                            operation.getResponses().forEach((status, response) -> {
                                if (status.startsWith("4") || status.startsWith("5")) {
                                    // Всегда создаем новый контент для ошибок
                                    Content content = new Content();
                                    MediaType mediaType = new MediaType();
                                    mediaType.setSchema(errorSchemaRef);
                                    content.addMediaType("application/json", mediaType);

                                    response.setContent(content);
                                }
                            })
                    )
            );
        };
    }


}

