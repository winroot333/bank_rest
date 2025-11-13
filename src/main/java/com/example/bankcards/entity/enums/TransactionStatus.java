package com.example.bankcards.entity.enums;

/**
 * Список статусов транзакций
 * Но не используются, только ставится COMPLETED
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}