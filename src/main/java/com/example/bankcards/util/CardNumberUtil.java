package com.example.bankcards.util;

import com.example.bankcards.exception.CardEncryptionException;
import com.example.bankcards.exception.InvalidCardNumberException;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Random;


/**
 * Утилитный класс для работы с номером карты
 */
@Service
public class CardNumberUtil {
    private static final String MASKED_NUMBER_PREFIX = "**** **** **** ";
    private final Random RANDOM = new Random();

    /**
     * Маскирует номер карты, оставляя видимыми только первые 6 и последние 4 цифры
     *
     * @param cardNumber Номер карты для маскирования
     * @return Замаскированный номер карты
     */
    public String maskCardNumber(String cardNumber) {
        if (!validateCardNumber(cardNumber)) {
            throw new InvalidCardNumberException("Неверный формат карты");
        }
        String lastFour = cardNumber.substring(12);

        return MASKED_NUMBER_PREFIX + lastFour;
    }

    /**
     * Генерирует случайный номер карты
     *
     * @return Сгенерированный номер карты
     */
    public String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            cardNumber.append(RANDOM.nextInt(10));
        }
        return cardNumber.toString();
    }

    /**
     * Простое шифрование номера карты
     *
     * @param cardNumber номер карты
     * @return зашифрованный номер карты
     */
    public String encryptCardNumber(String cardNumber) {
        try {
            return Base64.getEncoder().encodeToString(cardNumber.getBytes());
        } catch (Exception e) {
            throw new CardEncryptionException("Ошибка при шифровании номера карты");
        }
    }

    /**
     * Простая расшифровка номера карты
     *
     * @param encryptedCardNumber зашиврованный номер карты
     * @return расшифрованный номер карты
     */
    public String decryptCardNumber(String encryptedCardNumber) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedCardNumber);
            return new String(decodedBytes);
        } catch (Exception e) {
            throw new CardEncryptionException("Ошибка при дешифровании номера карты");
        }
    }

    /**
     * Валидация номера карты
     *
     * @param cardNumber номер карты
     * @return boolean верный или нет формат номера
     */
    public boolean validateCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.length() == 16 && cardNumber.matches("\\d+");
    }

}
