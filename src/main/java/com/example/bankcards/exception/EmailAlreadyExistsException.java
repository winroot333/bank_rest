package com.example.bankcards.exception;

/**
 * Исключение если пользователь с такой почтой уже существует
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
