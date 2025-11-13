package com.example.bankcards.exception;

/**
 * Если у пользователя нет прав на смену статуса
 */
public class UnauthorizedStatusChangeException extends RuntimeException {
    public UnauthorizedStatusChangeException(String message) {
        super(message);
    }
}
