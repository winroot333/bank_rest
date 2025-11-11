package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    User save(User user);

    /**
     * Создание пользователя
     *
     * @return созданный пользователь
     */
    User create(User user);

    User updateUserStatus(Long userId, UserStatus status);

    Page<User> getAll(Pageable pageable);

    Page<User> getByStatus(UserStatus status, Pageable pageable);

    User getById(Long userId);

    void delete(Long userId);

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String username);

    /**
     * Получение текущего пользователя из контекста Spring Security
     *
     * @return текущий пользователь
     */
    public User getCurrentUser();

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService();

}
