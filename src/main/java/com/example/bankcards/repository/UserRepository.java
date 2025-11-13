package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Поиск по username
     */
    Optional<User> findByUsername(String username);

    /**
     * Существует ли пользователь с именем
     * @param username Имя пользователя
     */
    boolean existsByUsername(String username);

    /**
     * Существует ли пользователь с почтой
     * @param email Почта для проверки
     */
    boolean existsByEmail(String email);

    /**
     * Получение пользователей по статусу
     * @param status статус
     * @param pageable пагинация
     * @return Список пользователей с нужным статусом
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

}