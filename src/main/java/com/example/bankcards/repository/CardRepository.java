package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    /**
     * Найти карту по номеру (для проверки уникальности)
     * @param cardNumber Номер карты
     */
    Optional<Card> findByCardNumber(String cardNumber);

    /**
     * Найти все карты пользователя с пагинацией
     * @param ownerId Id пользователя
     * @param pageable Пагинация
     */
    Page<Card> findAllByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Найти карты пользователя со статусом
     * @param ownerId Id пользователя
     * @param status Статус
     */
    //
    List<Card> findByOwnerIdAndStatus(Long ownerId, CardStatus status);

    // Найти карты по статусу и дате истечения
    List<Card> findByStatusAndExpirationDateBefore(CardStatus status, LocalDate date);

    // Обновление баланса карты (оптимистичная блокировка)
    @Modifying
    @Query("UPDATE Card c SET c.balance = c.balance + :amount WHERE c.id = :id")
    void updateBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    // Поиск карт по диапазону баланса
    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId AND c.balance BETWEEN :min AND :max")
    List<Card> findByBalanceBetween(
            @Param("ownerId") Long ownerId, @Param("min") BigDecimal min, @Param("max") BigDecimal max);
}
