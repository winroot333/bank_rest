package com.example.bankcards.dto.request;

import com.example.bankcards.entity.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto для обновления статуса пользователя
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на обновление статуса пользователя")
public class UpdateUserStatusRequest {

    @Schema(description = "Новый статус пользователя", example = "ACTIVE")
    @NotNull(message = "Статус не может быть пустым")
    private UserStatus status;
}

