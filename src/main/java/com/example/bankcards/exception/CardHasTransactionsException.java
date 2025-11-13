package com.example.bankcards.exception;

/**
 * Исключение если с данной картой есть транзакции, для удаления
 */
public class CardHasTransactionsException extends RuntimeException {
    public CardHasTransactionsException(String message) {
        super(message);
    }
}
