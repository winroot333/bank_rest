package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardHasBalanceException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CardService {

    /**
     * Создание новой карты для пользователя
     *
     * @param cardHolder   Имя на карте
     * @param userId ID пользователя-владельца
     * @return созданная карта
     * @throws UserNotFoundException если пользователь не найден
     */
    Card createCard(String cardHolder, Long userId);

    /**
     * Обновление статуса карты. Пользователь может поставить только Blocked
     *
     * @param cardId ID карты
     * @param status новый статус карты
     * @return обновленная карта
     * @throws CardNotFoundException если карта не найдена
     */
    Card updateCardStatus(Long cardId, CardStatus status);

    /**
     * Обновление статуса карты для админа. Может поставить любой статус
     *
     * @param cardId ID карты
     * @param status новый статус карты
     * @return обновленная карта
     * @throws CardNotFoundException если карта не найдена
     */
    Card updateCardStatusByAdmin(Long cardId, CardStatus status);

    /**
     * Обновление баланса карты
     *
     * @param cardId ID карты
     * @param balance новый статус карты
     * @return обновленная карта
     * @throws CardNotFoundException если карта не найдена
     */
    Card updateCardBalance(Long cardId, BigDecimal balance);

    /**
     * Получение карты по ID с проверкой владельца
     *
     * @param cardId ID карты
     * @param userId ID пользователя-владельца
     * @return найденная карта
     * @throws CardNotFoundException если карта не найдена или не принадлежит пользователю
     */
    Card getCardByIdAndOwner(Long cardId, Long userId);

    /**
     * Получение карты по ID с проверкой владельца
     *
     * @param cardId ID карты
     * @return найденная карта
     * @throws CardNotFoundException если карта не найдена или не принадлежит пользователю
     */
    Card getCardById(Long cardId);

    /**
     * Получение всех карт пользователя с пагинацией
     *
     * @param userId ID пользователя
     * @param pageable параметры пагинации
     * @return страница с картами пользователя
     */
    Page<Card> getAllUserCards(Long userId, Pageable pageable);

    /**
     * Получение всех карт в системе (только для ADMIN)
     *
     * @param pageable параметры пагинации
     * @return страница со всеми картами
     */
    Page<Card> getAllCards(Pageable pageable);

    /**
     * Получение карт по статусу с пагинацией (только для ADMIN)
     *
     * @param status статус карты для фильтрации
     * @param pageable параметры пагинации
     * @return страница с картами отфильтрованная по статусу
     */
    Page<Card> getCardsByStatus(CardStatus status, Pageable pageable);

    /**
     * Получение карт пользователя по статусу с пагинацией
     *
     * @param userId ID пользователя
     * @param status статус карты для фильтрации
     * @param pageable параметры пагинации
     * @return страница с картами пользователя отфильтрованная по статусу
     */
    Page<Card> getUserCardsByStatus(Long userId, CardStatus status, Pageable pageable);

    /**
     * Удаление карты
     *
     * @param cardId ID карты для удаления
     * @param userId ID пользователя, выполняющего операцию
     * @throws CardNotFoundException если карта не найдена
     * @throws CardHasBalanceException если на карте есть средства
     */
    void deleteCard(Long cardId, Long userId);

    /**
     * Проверяет является ли пользователь владельцем карты
     * @param cardId ID карты
     * @param userId ID пользователя для проверки
     */
    boolean isCardOwnedByUser(Long cardId, Long userId);

}
