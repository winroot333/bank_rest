package com.example.bankcards.exception;

/**
 * Если срок действия карты истек
 */
public class CardExpiredException extends RuntimeException {
    public CardExpiredException(String message) {
        super(message);
    }
}
