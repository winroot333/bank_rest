package com.example.bankcards.repository;

import com.example.bankcards.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с транзакциями
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Найти все транзакции пользователя (как отправителя, так и получателя) с пагинацией
     *
     * @param userId   ID пользователя
     * @param pageable параметры пагинации
     * @return страница с транзакциями пользователя
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.fromCard.owner.id = :userId OR t.toCard.owner.id = :userId")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Найти все транзакции по карте (исходящие и входящие) с пагинацией
     *
     * @param cardId   ID карты
     * @param pageable параметры пагинации
     * @return страница с транзакциями карты
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId")
    Page<Transaction> findByCardId(@Param("cardId") Long cardId, Pageable pageable);

}