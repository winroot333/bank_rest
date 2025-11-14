package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.UserHasCardsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Тесты для сервиса пользователей")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;

    private UserServiceImpl userService;

    private User user;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, cardRepository);

        user = User.builder()
                .id(1L)
                .username("IVAN IVANOV")
                .email("ivan@example.com")
                .status(UserStatus.ACTIVE)
                .build();

        pageable = PageRequest.of(0, 10);
        pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"));
    }

    @DisplayName("save Должен успешно сохранить пользователя")
    @Test
    void save_ShouldSaveUserSuccessfully() {
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.save(user);

        assertThat(result).isNotNull().isEqualTo(user);
        verify(userRepository, times(1)).save(user);
    }

    @DisplayName("create Должен успешно создать пользователя")
    @Test
    void create_ShouldCreateUserSuccessfully() {
        when(userRepository.existsByUsername("IVAN IVANOV")).thenReturn(false);
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.create(user);

        assertThat(result).isNotNull().isEqualTo(user);
        verify(userRepository, times(1)).existsByUsername("IVAN IVANOV");
        verify(userRepository, times(1)).existsByEmail("ivan@example.com");
        verify(userRepository, times(1)).save(user);
    }

    @DisplayName("create Должен выбросить исключение при существующем username")
    @Test
    void create_WhenUsernameExists_ShouldThrowException() {
        when(userRepository.existsByUsername("IVAN IVANOV")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, times(1)).existsByUsername("IVAN IVANOV");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @DisplayName("create Должен выбросить исключение при существующем email")
    @Test
    void create_WhenEmailExists_ShouldThrowException() {
        when(userRepository.existsByUsername("IVAN IVANOV")).thenReturn(false);
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(user))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, times(1)).existsByUsername("IVAN IVANOV");
        verify(userRepository, times(1)).existsByEmail("ivan@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @DisplayName("updateUserStatus Должен успешно обновить статус пользователя")
    @Test
    void updateUserStatus_ShouldUpdateStatusSuccessfully() {
        UserStatus newStatus = UserStatus.BLOCKED;
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUserStatus(1L, newStatus);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @DisplayName("updateUserStatus Должен выбросить исключение при отсутствии пользователя")
    @Test
    void updateUserStatus_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserStatus(1L, UserStatus.BLOCKED))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @DisplayName("getAll Должен вернуть страницу всех пользователей")
    @Test
    void getAll_ShouldReturnAllUsersPage() {
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<User> result = userService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(user);
        verify(userRepository, times(1)).findAll(pageable);
    }

    @DisplayName("getByStatus Должен вернуть страницу пользователей по статусу")
    @Test
    void getByStatus_ShouldReturnUsersByStatus() {
        UserStatus status = UserStatus.ACTIVE;
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findByStatus(status, pageable)).thenReturn(userPage);

        Page<User> result = userService.getByStatus(status, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(user);
        verify(userRepository, times(1)).findByStatus(status, pageable);
    }

    @DisplayName("getById Должен успешно вернуть пользователя по ID")
    @Test
    void getById_ShouldReturnUserSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository, times(1)).findById(1L);
    }

    @DisplayName("getById Должен выбросить исключение при отсутствии пользователя")
    @Test
    void getById_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(1L);
    }

    @DisplayName("delete Должен успешно удалить пользователя")
    @Test
    void delete_ShouldDeleteUserSuccessfully() {

        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.findByOwnerId(1L, pageable)).thenReturn(Page.empty());

        userService.delete(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(cardRepository, times(1)).findByOwnerId(1L, pageable);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @DisplayName("delete Должен выбросить исключение при отсутствии пользователя")
    @Test
    void delete_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).existsById(1L);
        verify(cardRepository, never()).findByOwnerId(anyLong(), any(Pageable.class));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @DisplayName("delete Должен выбросить исключение при наличии карт у пользователя")
    @Test
    void delete_WhenUserHasCards_ShouldThrowException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.findByOwnerId(1L, pageable)).thenReturn(new PageImpl<>(List.of(new Card())));

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(UserHasCardsException.class);

        verify(userRepository, times(1)).existsById(1L);
        verify(cardRepository, times(1)).findByOwnerId(1L, pageable);
        verify(userRepository, never()).deleteById(anyLong());
    }
}