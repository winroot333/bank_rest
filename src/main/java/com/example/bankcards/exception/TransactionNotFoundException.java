package com.example.bankcards.exception;

/**
 * если транзакция не найдена
 */
public class TransactionNotFoundException extends RuntimeException{
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
