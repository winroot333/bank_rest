package com.example.bankcards.exception;

/**
 * Если недостаточно средств на перевод
 */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
