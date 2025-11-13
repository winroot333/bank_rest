package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Объект ответа при ошибке
 */
@Schema(description = "Ответ с информацией об ошибке")
public record ErrorResponse(
        @Schema(description = "Сообщение об ошибке", example = "Карта не найдена")
        String error
) {
}

