package com.example.bankcards.service;

import com.example.bankcards.entity.User;

/**
 * Сервис для регистрации и аутентификации пользователей
 */
public interface AuthenticationService {
    /**
     * Аутентификация пользователя
     *
     * @param username Имя пользователя
     * @param password Пароль
     * @return токен
     */
    String signIn(String username, String password);

    /**
     * Регистрация пользователя
     *
     * @param user созданный из ДТО пользователь
     * @return токен
     */
    String signUp(User user);
}
