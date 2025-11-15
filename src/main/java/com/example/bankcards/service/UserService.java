package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * Сохранение пользователя
     *
     * @param user сохранение нового, или обновление существующего пользователя
     * @return сохраненный пользователь
     */
    User save(User user);

    /**
     * Создание пользователя с проверками уникальности почты и username
     *
     * @param user Объект пользователя из DTO
     * @return созданный пользователь
     * @throws UsernameAlreadyExistsException если пользователь с таким username уже существует
     * @throws EmailAlreadyExistsException    если пользователь с таким email уже существует
     */
    User create(User user);

    /**
     * Обновление статуса пользователя
     *
     * @param userId ID пользователя
     * @param status новый статус пользователя
     * @return обновленный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    User updateUserStatus(Long userId, UserStatus status);

    /**
     * Получение всех пользователей с пагинацией
     *
     * @param pageable параметры пагинации
     * @return страница с пользователями
     */
    Page<User> getAll(Pageable pageable);

    /**
     * Получение пользователей по статусу с пагинацией
     *
     * @param status   статус пользователя для фильтрации
     * @param pageable параметры пагинации
     * @return страница с пользователями отфильтрованная по статусу
     */
    Page<User> getByStatus(UserStatus status, Pageable pageable);

    /**
     * Получение пользователя по ID
     *
     * @param userId ID пользователя
     * @return найденный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    User getById(Long userId);

    /**
     * Удаление пользователя по ID
     *
     * @param userId ID пользователя для удаления
     * @throws UserNotFoundException если пользователь не найден
     */
    void delete(Long userId);
}
