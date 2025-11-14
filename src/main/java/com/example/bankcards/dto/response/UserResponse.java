package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с информацией о пользователе")
public class UserResponse implements Serializable {

    @Schema(description = "ID пользователя", example = "1")
    private Long id;

    @Schema(description = "Имя пользователя", example = "jon_doe")
    private String username;

    @Schema(description = "Email пользователя", example = "jon.doe@example.com")
    private String email;

    @Schema(description = "Роль пользователя", example = "ROLE_USER")
    private Role role;

    @Schema(description = "Статус пользователя", example = "ACTIVE")
    private UserStatus status;
}

