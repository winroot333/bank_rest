package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Тесты для сервиса аутентификации")
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthenticationServiceImpl authenticationService;

    private User user;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl(userService, userDetailsService, jwtService, passwordEncoder, authenticationManager);

        user = User.builder()
                .id(1L)
                .username("IVAN IVANOV")
                .email("ivan@example.com")
                .password("encodedPassword")
                .build();

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("IVAN IVANOV")
                .password("encodedPassword")
                .authorities("ROLE_USER")
                .build();
    }

    @DisplayName("signUp Должен успешно зарегистрировать пользователя и вернуть токен")
    @Test
    void signUp_ShouldRegisterUserAndReturnToken() {
        String expectedToken = "my.super.jwt.token";
        when(userService.create(user)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(expectedToken);

        String result = authenticationService.signUp(user);

        assertThat(result).isEqualTo(expectedToken);
        verify(userService, times(1)).create(user);
        verify(jwtService, times(1)).generateToken(user);
    }

    @DisplayName("signIn Должен успешно аутентифицировать пользователя и вернуть токен")
    @Test
    void signIn_ShouldAuthenticateUserAndReturnToken() {
        String username = "IVAN IVANOV";
        String password = "password";
        String expectedToken = "my.super.jwt.token";

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);

        String result = authenticationService.signIn(username, password);

        assertThat(result).isEqualTo(expectedToken);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtService, times(1)).generateToken(userDetails);
    }

    @DisplayName("signIn Должен использовать правильные credentials для аутентификации")
    @Test
    void signIn_ShouldUseCorrectCredentialsForAuthentication() {
        String username = "IVAN IVANOV";
        String password = "password";
        String expectedToken = "my.super.jwt.token";

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn(expectedToken);

        authenticationService.signIn(username, password);

        verify(authenticationManager, times(1)).authenticate(
                argThat(token ->
                        token instanceof UsernamePasswordAuthenticationToken &&
                                token.getPrincipal().equals(username) &&
                                token.getCredentials().equals(password)
                )
        );
    }
}