package com.example.bankcards.controller;

import com.example.bankcards.dto.request.UpdateUserStatusRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.UserSecurity;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Тесты для контроллера управления пользователями /api/users")
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    PageResponse<UserResponse> pageResponse;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserMapper userMapper;
    @MockitoBean
    private UserSecurity userSecurity;
    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private UserResponse userResponse;
    private Page<User> userPage;
    private UpdateUserStatusRequest updateStatusRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("ivanov")
                .email("ivan@example.com")
                .status(UserStatus.ACTIVE)
                .build();

        userResponse = new UserResponse(
                1L,
                "ivanov",
                "ivan@example.com",
                Role.ROLE_USER,
                UserStatus.ACTIVE
        );

        userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        pageResponse = new PageResponse<>(
                List.of(userResponse), 0, 1, 1, 10);
        updateStatusRequest = new UpdateUserStatusRequest(UserStatus.BLOCKED);
    }

    @DisplayName("GET /api/users Должен успешно получить всех пользователей")
    @Test
    void getAllUsers_ShouldReturnAllUsersSuccessfully() throws Exception {
        when(userService.getAll(PageRequest.of(0, 10, Sort.by("username"))))
                .thenReturn(userPage);
        when(userMapper.toPageResponse(userPage)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].username").value("ivanov"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(userService, times(1)).getAll(PageRequest.of(0, 10, Sort.by("username")));
    }

    @DisplayName("GET /api/users Должен успешно получить пользователей по статусу")
    @Test
    void getAllUsers_WithStatusFilter_ShouldReturnFilteredUsers() throws Exception {

        when(userService.getByStatus(UserStatus.ACTIVE, PageRequest.of(0, 10, Sort.by("username"))))
                .thenReturn(userPage);
        when(userMapper.toPageResponse(userPage)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));

        verify(userService, times(1)).getByStatus(UserStatus.ACTIVE, PageRequest.of(0, 10, Sort.by("username")));
    }

    @DisplayName("GET /api/users Должен вернуть 400 при невалидных параметрах пагинации")
    @Test
    void getAllUsers_WithInvalidPagination_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("GET /api/users/{userId} Должен успешно получить пользователя по ID")
    @Test
    void getUser_ShouldReturnUserSuccessfully() throws Exception {
        when(userService.getById(1L)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("ivanov"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(userService, times(1)).getById(1L);
    }

    @DisplayName("GET /api/users/{userId} Должен вернуть 404 когда пользователь не найден")
    @Test
    void getUser_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        when(userService.getById(1L))
                .thenThrow(new UserNotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @DisplayName("PATCH /api/users/{userId}/status Должен успешно изменить статус пользователя")
    @Test
    void updateUserStatus_ShouldUpdateStatusSuccessfully() throws Exception {
        User blockedUser = User.builder()
                .id(1L)
                .username("ivanov")
                .email("ivan@example.com")
                .status(UserStatus.BLOCKED)
                .build();

        UserResponse blockedResponse = new UserResponse(
                1L,
                "ivanov",
                "ivan@example.com",
                Role.ROLE_USER,
                UserStatus.BLOCKED
        );

        when(userService.updateUserStatus(1L, UserStatus.BLOCKED)).thenReturn(blockedUser);
        when(userMapper.toResponse(blockedUser)).thenReturn(blockedResponse);

        mockMvc.perform(patch("/api/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(userService, times(1)).updateUserStatus(1L, UserStatus.BLOCKED);
    }

    @DisplayName("PATCH /api/users/{userId}/status Должен вернуть 400 при невалидных данных")
    @Test
    void updateUserStatus_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Тест с пустым телом запроса
        mockMvc.perform(patch("/api/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("PATCH /api/users/{userId}/status Должен вернуть 404 когда пользователь не найден")
    @Test
    void updateUserStatus_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        when(userService.updateUserStatus(1L, UserStatus.BLOCKED))
                .thenThrow(new UserNotFoundException("Пользователь не найден"));

        mockMvc.perform(patch("/api/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @DisplayName("DELETE /api/users/{userId} Должен успешно удалить пользователя")
    @Test
    void deleteUser_ShouldDeleteUserSuccessfully() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(1L);
    }

    @DisplayName("DELETE /api/users/{userId} Должен вернуть 404 когда пользователь не найден")
    @Test
    void deleteUser_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new UserNotFoundException("Пользователь не найден"))
                .when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @DisplayName("GET /api/users/me Должен успешно получить текущего пользователя")
    @Test
    void getCurrentUser_ShouldReturnCurrentUserSuccessfully() throws Exception {
        when(userSecurity.getLoggedInUserId()).thenReturn(1L);
        when(userService.getById(1L)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("ivanov"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));

        verify(userSecurity, times(1)).getLoggedInUserId();
        verify(userService, times(1)).getById(1L);
    }

    @DisplayName("GET /api/users/me Должен вернуть 404 когда текущий пользователь не найден")
    @Test
    void getCurrentUser_WhenCurrentUserNotFound_ShouldReturnNotFound() throws Exception {
        when(userSecurity.getLoggedInUserId()).thenReturn(1L);
        when(userService.getById(1L))
                .thenThrow(new UserNotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @DisplayName("GET /api/users Должен успешно получить пользователей с разными статусами")
    @Test
    void getAllUsers_WithDifferentStatuses_ShouldReturnCorrectUsers() throws Exception {
        User inactiveUser = User.builder()
                .id(2L)
                .username("petrov")
                .email("petr@example.com")
                .status(UserStatus.BLOCKED)
                .build();

        UserResponse inactiveResponse = new UserResponse(
                2L,
                "petrov",
                "petr@example.com",
                Role.ROLE_USER,
                UserStatus.BLOCKED
        );

        Page<User> inactiveUserPage = new PageImpl<>(List.of(inactiveUser));
        PageResponse<UserResponse> pageResponse = new PageResponse<>(
                List.of(inactiveResponse), 0, 1, 1, 10);

        when(userService.getByStatus(UserStatus.BLOCKED, PageRequest.of(0, 10, Sort.by("username"))))
                .thenReturn(inactiveUserPage);
        when(userMapper.toPageResponse(inactiveUserPage)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("BLOCKED"))
                .andExpect(jsonPath("$.content[0].username").value("petrov"));
    }
}