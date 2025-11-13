package com.example.bankcards.controller;

import com.example.bankcards.dto.request.UpdateUserStatusRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.security.UserSecurity;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Управление пользователями")
@ApiResponses(@ApiResponse(responseCode = "200", useReturnTypeSchema = true))
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserSecurity userSecurity;

    @Operation(summary = "Получить всех пользователей (только для ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователи успешно получены"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    public PageResponse<UserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) UserStatus status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("username"));
        Page<User> usersPage;

        if (status != null) {
            usersPage = userService.getByStatus(status, pageable);
        } else {
            usersPage = userService.getAll(pageable);
        }

        return userMapper.toPageResponse(usersPage);
    }

    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно получен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return userMapper.toResponse(user);
    }

    @Operation(summary = "Изменить статус пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус пользователя успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping("/{userId}/status")
    public UserResponse updateUserStatus(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateUserStatusRequest request) {

        User updatedUser = userService.updateUserStatus(userId, request.getStatus());
        return userMapper.toResponse(updatedUser);
    }

    @Operation(summary = "Удалить пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Текущий пользователь успешно получен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        User user = userService.getById(userSecurity.getLoggedInUserId());
        return userMapper.toResponse(user);
    }
}