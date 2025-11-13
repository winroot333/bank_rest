package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Объект ответа при ошибке
 */
@Schema(description = "Ответ с информацией об ошибке")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    @Schema(description = "Сообщение об ошибке", example = "Карта не найдена")
    String error;
}



