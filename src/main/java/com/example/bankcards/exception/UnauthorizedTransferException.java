package com.example.bankcards.exception;

/**
 * Если у пользователя нет прав на перевод
 */
public class UnauthorizedTransferException extends RuntimeException {
    public UnauthorizedTransferException(String message) {
        super(message);
    }
}
