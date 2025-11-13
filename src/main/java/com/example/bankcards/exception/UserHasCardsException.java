package com.example.bankcards.exception;

/**
 * Если у пользователя есть карты при удалении
 */
public class UserHasCardsException extends RuntimeException {
    public UserHasCardsException(String message) {
        super(message);
    }
}
