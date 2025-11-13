package com.example.bankcards.exception;

/**
 * Исключение если карта не найден
 */
public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String message) {
        super(message);
    }
}
