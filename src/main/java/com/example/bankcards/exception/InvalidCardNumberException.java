package com.example.bankcards.exception;

/**
 * Исключение при неверном номере карты
 */
public class InvalidCardNumberException extends RuntimeException {
    public InvalidCardNumberException(String message) {
        super(message);
    }
}
