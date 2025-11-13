package com.example.bankcards.controller;

import com.example.bankcards.dto.request.UpdateUserStatusRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Управление пользователями")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Получить всех пользователей (только для ADMIN)")
    @GetMapping
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public PageResponse<UserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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
    @GetMapping("/{userId}")
    @PreAuthorize("@userSecurity.isOwnerOrAdmin(#userId)")
    public UserResponse getUser(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return userMapper.toResponse(user);
    }

    @Operation(summary = "Изменить статус пользователя")
    @PatchMapping("/{userId}/status")
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public UserResponse updateUserStatus(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateUserStatusRequest request) {

        User updatedUser = userService.updateUserStatus(userId, request.getStatus());
        return userMapper.toResponse(updatedUser);
    }

    @Operation(summary = "Удалить пользователя")
    @DeleteMapping("/{userId}")
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить текущего пользователя")
    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getByUsername(username);
        return userMapper.toResponse(user);
    }
}