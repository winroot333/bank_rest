package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Класс для упрощения проверок прав пользователя
 */
@Component("userSecurity")
public class UserSecurity {

    /**
     * Является ли залогиненный пользователь админом или создателем ресурса
     * @param ownerUserId - id владельца ресурса для проверки
     * @return boolean есть или нет права на редактирование
     */
    public boolean isOwnerOrAdmin(Long ownerUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (hasAdminRole()) {
            return true;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User currentUser) {
            return currentUser.getId().equals(ownerUserId);
        }
        return false;
    }
    

    /**
     * Является ли текущий пользователь админом
     * @return boolean админ или нет
     */
    public boolean hasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

}
