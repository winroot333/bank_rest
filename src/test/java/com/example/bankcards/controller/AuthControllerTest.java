package com.example.bankcards.controller;

import com.example.bankcards.dto.request.SignInRequest;
import com.example.bankcards.dto.request.SignUpRequest;
import com.example.bankcards.dto.response.JwtAuthenticationResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AuthenticationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Тесты для контроллера аутентификации /api/auth")
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @MockitoBean
    private AuthenticationServiceImpl authenticationService;
    @MockitoBean
    private UserMapper userMapper;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private SignUpRequest signUpRequest;
    private SignInRequest signInRequest;
    private User user;
    private JwtAuthenticationResponse jwtResponse;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest("user123", "user@example.com", "password123");
        signInRequest = new SignInRequest("user123", "password123");
        user = User.builder()
                .id(1L)
                .username("user123")
                .email("user@example.com")
                .password("encodedPassword")
                .build();
        jwtResponse = new JwtAuthenticationResponse("jwt-token-123");
    }

    @DisplayName("POST /api/auth/sign-up Должен успешно зарегистрировать пользователя")
    @Test
    void signUp_ShouldRegisterUserSuccessfully() throws Exception {
        when(userMapper.toEntityWithEncodedPassword(any(SignUpRequest.class), any(PasswordEncoder.class)))
                .thenReturn(user);
        when(authenticationService.signUp(user)).thenReturn("jwt-token-123");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));

        verify(authenticationService, times(1)).signUp(user);
        verify(userMapper, times(1)).toEntityWithEncodedPassword(signUpRequest, passwordEncoder);
    }

    @DisplayName("POST /api/auth/sign-up Должен вернуть 400 при невалидных данных")
    @Test
    void signUp_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        SignUpRequest invalidRequest = new SignUpRequest("", "invalid-email", "");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("POST /api/auth/sign-up Должен вернуть 404 когда пользователь не найден")
    @Test
    void signUp_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        when(userMapper.toEntityWithEncodedPassword(any(SignUpRequest.class), any(PasswordEncoder.class)))
                .thenReturn(user);
        when(authenticationService.signUp(user)).thenThrow(new UserNotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @DisplayName("POST /api/auth/sign-in Должен успешно аутентифицировать пользователя")
    @Test
    void signIn_ShouldAuthenticateUserSuccessfully() throws Exception {
        when(authenticationService.signIn("user123", "password123")).thenReturn("jwt-token-123");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));

        verify(authenticationService, times(1)).signIn("user123", "password123");
    }

    @DisplayName("POST /api/auth/sign-in Должен вернуть 400 при невалидных данных")
    @Test
    void signIn_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        SignInRequest invalidRequest = new SignInRequest("", "");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("POST /api/auth/sign-in Должен вернуть 401 при неверных учетных данных")
    @Test
    void signIn_WithWrongCredentials_ShouldReturnUnauthorized() throws Exception {
        when(authenticationService.signIn("user123", "wrongPassword"))
                .thenThrow(new BadCredentialsException("Неверные учетные данные"));

        SignInRequest wrongCredentialsRequest = new SignInRequest("user123", "wrongPassword");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongCredentialsRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Неверные учетные данные"));
    }

    @DisplayName("POST /api/auth/sign-in Должен вернуть 404 когда пользователь не найден")
    @Test
    void signIn_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        when(authenticationService.signIn("unknown", "password"))
                .thenThrow(new UserNotFoundException("Пользователь не найден"));

        SignInRequest unknownUserRequest = new SignInRequest("unknown", "password");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownUserRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @DisplayName("POST /api/auth/sign-up Должен вернуть 500 при внутренней ошибке сервера")
    @Test
    void signUp_WhenInternalError_ShouldReturnInternalServerError() throws Exception {
        when(userMapper.toEntityWithEncodedPassword(any(SignUpRequest.class), any(PasswordEncoder.class)))
                .thenReturn(user);
        when(authenticationService.signUp(user)).thenThrow(new RuntimeException("Unknown error"));

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isInternalServerError());
    }
}