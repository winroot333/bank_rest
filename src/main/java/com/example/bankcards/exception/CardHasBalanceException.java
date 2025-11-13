package com.example.bankcards.exception;

/**
 * Исключение если баланс карты не равен 0 для удаления
 */
public class CardHasBalanceException extends RuntimeException {
    public CardHasBalanceException(String message) {
        super(message);
    }
}
