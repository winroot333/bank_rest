package com.example.bankcards.exception;

/**
 * Исключение если пользователь с таким именем уже существует
 */
public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
