package com.example.bankcards.exception;

/**
 * Исключение при неверной сумме транзакции
 */
public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
    }
}
