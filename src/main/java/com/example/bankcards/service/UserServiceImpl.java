package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        return save(user);
    }

    @Override
    public User updateUserStatus(Long userId, UserStatus status) {
        return null;
    }

    @Override
    public Page<User> getAll(Pageable pageable) {
        return null;
    }

    @Override
    public Page<User> getByStatus(UserStatus status, Pageable pageable) {
        return null;
    }

    @Override
    public User getById(Long userId) {
        return null;
    }

    @Override
    public void delete(Long userId) {

    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Override
    public User getCurrentUser() {
        return null;
    }

    @Override
    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

}
