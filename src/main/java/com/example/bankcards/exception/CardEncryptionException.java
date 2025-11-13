package com.example.bankcards.exception;

/**
 * Исключение при ошибках шифрования номера карты
 */
public class CardEncryptionException extends RuntimeException {
    public CardEncryptionException(String message) {
        super(message);
    }
}
