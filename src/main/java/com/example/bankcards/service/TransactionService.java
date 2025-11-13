package com.example.bankcards.service;

import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления финансовыми транзакциями
 */
public interface TransactionService {

    /**
     * Перевод средств между картами одного пользователя
     *
     * @param userId      Id пользователя, выполняющего перевод
     * @param fromCardId    Id карты с которой совершаем перевод
     * @param toCardId      Id карты на которую совершаем перевод
     * @param amount        Сумма
     * @param description   Описание тразнакции
     * @return созданная транзакция
     * @throws UserNotFoundException         если пользователь не найден
     * @throws CardNotFoundException         если карта не найдена
     * @throws UnauthorizedTransferException если карты не принадлежат пользователю
     * @throws CardBlockedException          если карта заблокирована
     * @throws CardExpiredException          если срок действия карты истек
     * @throws InsufficientFundsException    если недостаточно средств
     */
    Transaction transferBetweenOwnCards(
            Long userId,Long fromCardId, Long toCardId, BigDecimal amount, String description
    ) throws UserNotFoundException;

    /**
     * Получение истории транзакций пользователя с пагинацией
     *
     * @param userId   ID пользователя
     * @param pageable параметры пагинации
     * @return страница с транзакциями пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    Page<Transaction> getUserTransactions(Long userId, Pageable pageable);

    /**
     * Получение истории транзакций по карте с пагинацией
     *
     * @param cardId   ID карты
     * @param pageable параметры пагинации
     * @return страница с транзакциями карты
     */
    Page<Transaction> getCardTransactions(Long cardId, Pageable pageable);

    /**
     * Получение списка всех транзакция для админа
     * @param pageable - параметры пагинации
     * @return страница с транзакциями
     */
    Page<Transaction> getAllTransactions(Pageable pageable);
}
