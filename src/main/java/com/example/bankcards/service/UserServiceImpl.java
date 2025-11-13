package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UsernameAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }

        return save(user);
    }

    @Override
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public User updateUserStatus(Long userId, UserStatus status) {
        User user = getById(userId);
        user.setStatus(status);
        return save(user);
    }

    @Override
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public Page<User> getByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable);
    }

    @Override
    @PreAuthorize("@userSecurity.isOwnerOrAdmin(#userId)")
    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь не найден с ID: " + userId));
    }

    @Override
    @PreAuthorize("@userSecurity.isOwnerOrAdmin(#userId)")
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Пользователь не найден с ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
}
