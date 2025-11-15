package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


/**
 * Утилитный класс для упрощения проверок прав доступа к объектам
 */
@RequiredArgsConstructor
@Component("userSecurity")
public class UserSecurity {
    private final CardService cardService;

    /**
     * Совпадает ли id залогиненного пользователя с id создателя объекта. Или роль админа
     *
     * @param ownerUserId id пользователя для проверки
     * @return boolean результат проверки прав
     */
    public boolean isOwnerOrAdmin(Long ownerUserId) {
        return hasAdminRole() || isOwner(ownerUserId);
    }

    /**
     * Являектя ли залогиненный пользователь владельцем карты или админом
     *
     * @param cardId id карты
     * @return boolean результат проверки
     */
    public boolean isCardOwnerOrAdmin(Long cardId) {
        return hasAdminRole() || isCardOwner(cardId);
    }

    /**
     * Есть ли у залогиненного пользователя права админа
     *
     * @return boolean результат прверки
     */
    public boolean hasAdminRole() {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Получение id залогиненного пользователя
     *
     * @return id залогиненного пользователя
     */
    public Long getLoggedInUserId() {
        Object principal = getAuthentication().getPrincipal();
        if (principal instanceof User currentUser) {
            return currentUser.getId();
        }
        throw new UserNotFoundException("User not found");
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isOwner(Long ownerUserId) {
        return getLoggedInUserId().equals(ownerUserId);
    }

    private boolean isCardOwner(Long cardId) {
        return cardService.isCardOwnedByUser(cardId, getLoggedInUserId());
    }
}
