package com.example.bankcards.exception;

/**
 * Если у пользователя нет прав на смену статуса
 */
public class CardBlockedException extends RuntimeException {
    public CardBlockedException(String message) {
        super(message);
    }
}
