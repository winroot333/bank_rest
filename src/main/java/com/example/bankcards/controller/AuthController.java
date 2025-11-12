package com.example.bankcards.controller;

import com.example.bankcards.dto.response.JwtAuthenticationResponse;
import com.example.bankcards.dto.request.SignInRequest;
import com.example.bankcards.dto.request.SignUpRequest;
import com.example.bankcards.service.AuthenticationServiceImpl;
import com.example.bankcards.service.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {
    private final AuthenticationServiceImpl authenticationService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        var user = userMapper.toEntityWithEncodedPassword(request, passwordEncoder);
        String token = authenticationService.signUp(user);
        return new JwtAuthenticationResponse(token);
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        String token = authenticationService.signIn(request.getUsername(), request.getPassword());
        return new JwtAuthenticationResponse(token);
    }
}