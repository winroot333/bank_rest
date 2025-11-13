package com.example.bankcards.dto.request;

import com.example.bankcards.entity.enums.UserStatus;
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
public class UpdateUserStatusRequest {
    @NotNull(message = "Статус не может быть пустым")
    private UserStatus status;
}

