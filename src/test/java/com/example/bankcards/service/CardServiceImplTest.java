package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardHasBalanceException;
import com.example.bankcards.exception.CardHasTransactionsException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.CardNumberUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("Тесты для сервиса управления картами")
@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;
    @Mock
    private CardNumberUtil cardNumberUtil;
    @Mock
    private TransactionRepository transactionRepository;

    private CardServiceImpl cardService;

    private User user;
    private Card card;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        cardService = new CardServiceImpl(cardRepository, userService, cardNumberUtil, transactionRepository);

        user = User.builder()
                .id(1L)
                .username("IVAN IVANOV")
                .email("ivan@example.com")
                .build();

        card = Card.builder()
                .id(1L)
                .encryptedCardNumber("MTIzNDU2NzgxMjM0NTY3OA==")
                .maskedNumber("**** **** **** 5678")
                .cardHolder("IVAN IVANOV")
                .expirationDate(LocalDate.now().plusYears(3))
                .balance(new BigDecimal("0.00"))
                .status(CardStatus.ACTIVE)
                .owner(user)
                .build();

        pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"));
    }

    @DisplayName("createCard Должен успешно создать новую карту")
    @Test
    void createCard_ShouldCreateCardSuccessfully() {
        String cardHolder = "IVAN IVANOV";
        String cardNumber = "1234567812345678";
        String encryptedNumber = "MTIzNDU2NzgxMjM0NTY3OA==";
        String maskedNumber = "**** **** **** 5678";

        when(cardNumberUtil.generateCardNumber()).thenReturn(cardNumber);
        when(cardNumberUtil.encryptCardNumber(cardNumber)).thenReturn(encryptedNumber);
        when(cardNumberUtil.maskCardNumber(cardNumber)).thenReturn(maskedNumber);
        when(userService.getById(1L)).thenReturn(user);
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.createCard(cardHolder, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getCardHolder()).isEqualTo(cardHolder);
        assertThat(result.getEncryptedCardNumber()).isEqualTo(encryptedNumber);
        assertThat(result.getMaskedNumber()).isEqualTo(maskedNumber);
        assertThat(result.getBalance()).isEqualTo(new BigDecimal("0.00"));
        assertThat(result.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(result.getOwner()).isEqualTo(user);

        verify(cardNumberUtil, times(1)).generateCardNumber();
        verify(cardNumberUtil, times(1)).encryptCardNumber(cardNumber);
        verify(cardNumberUtil, times(1)).maskCardNumber(cardNumber);
        verify(userService, times(1)).getById(1L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @DisplayName("blockCard Должен успешно заблокировать карту")
    @Test
    void blockCard_ShouldBlockCardSuccessfully() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardService.blockCard(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(card);
    }

    @DisplayName("blockCard Должен выбросить исключение при отсутствии карты")
    @Test
    void blockCard_WhenCardNotFound_ShouldThrowException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.blockCard(1L))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @DisplayName("updateCardStatusByAdmin Должен успешно обновить статус карты")
    @Test
    void updateCardStatusByAdmin_ShouldUpdateStatusSuccessfully() {
        CardStatus newStatus = CardStatus.EXPIRED;
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardService.updateCardStatusByAdmin(1L, newStatus);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(card);
    }

    @DisplayName("updateCardBalance Должен успешно обновить баланс карты")
    @Test
    void updateCardBalance_ShouldUpdateBalanceSuccessfully() {
        BigDecimal newBalance = new BigDecimal("100.50");
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardService.updateCardBalance(1L, newBalance);

        assertThat(result).isNotNull();
        assertThat(result.getBalance()).isEqualTo(newBalance);
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(card);
    }

    @DisplayName("getCardByIdAndOwner Должен успешно вернуть карту по ID и владельцу")
    @Test
    void getCardByIdAndOwner_ShouldReturnCardSuccessfully() {
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(card));

        Card result = cardService.getCardByIdAndOwner(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOwner()).isEqualTo(user);
        verify(cardRepository, times(1)).findByIdAndOwnerId(1L, 1L);
    }

    @DisplayName("getCardByIdAndOwner Должен выбросить исключение при отсутствии карты")
    @Test
    void getCardByIdAndOwner_WhenCardNotFound_ShouldThrowException() {
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardByIdAndOwner(1L, 1L))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardRepository, times(1)).findByIdAndOwnerId(1L, 1L);
    }

    @DisplayName("getAllUserCards Должен вернуть страницу карт пользователя")
    @Test
    void getAllUserCards_ShouldReturnUserCardsPage() {
        Page<Card> cardPage = new PageImpl<>(List.of(card), pageable, 1);
        when(cardRepository.findByOwnerId(1L, pageable)).thenReturn(cardPage);

        Page<Card> result = cardService.getAllUserCards(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(card);
        verify(cardRepository, times(1)).findByOwnerId(1L, pageable);
    }

    @DisplayName("getAllCards Должен вернуть страницу всех карт")
    @Test
    void getAllCards_ShouldReturnAllCardsPage() {
        Page<Card> cardPage = new PageImpl<>(List.of(card), pageable, 1);
        when(cardRepository.findAll(pageable)).thenReturn(cardPage);

        Page<Card> result = cardService.getAllCards(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(card);
        verify(cardRepository, times(1)).findAll(pageable);
    }

    @DisplayName("getCardsByStatus Должен вернуть страницу карт по статусу")
    @Test
    void getCardsByStatus_ShouldReturnCardsByStatus() {
        CardStatus status = CardStatus.ACTIVE;
        Page<Card> cardPage = new PageImpl<>(List.of(card), pageable, 1);
        when(cardRepository.findAllByStatus(status, pageable)).thenReturn(cardPage);

        Page<Card> result = cardService.getCardsByStatus(status, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(card);
        verify(cardRepository, times(1)).findAllByStatus(status, pageable);
    }

    @DisplayName("getUserCardsByStatus Должен вернуть страницу карт пользователя по статусу")
    @Test
    void getUserCardsByStatus_ShouldReturnUserCardsByStatus() {
        CardStatus status = CardStatus.ACTIVE;
        Page<Card> cardPage = new PageImpl<>(List.of(card), pageable, 1);
        when(cardRepository.findByOwnerIdAndStatus(1L, status, pageable)).thenReturn(cardPage);

        Page<Card> result = cardService.getUserCardsByStatus(1L, status, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(card);
        verify(cardRepository, times(1)).findByOwnerIdAndStatus(1L, status, pageable);
    }

    @DisplayName("deleteCard Должен успешно удалить карту")
    @Test
    void deleteCard_ShouldDeleteCardSuccessfully() {
        card.setBalance(BigDecimal.ZERO);
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(card));
        when(transactionRepository.findByCardId(1L, pageable)).thenReturn(Page.empty());

        cardService.deleteCard(1L, 1L);

        verify(cardRepository, times(1)).delete(card);
        verify(cardRepository, times(1)).findByIdAndOwnerId(1L, 1L);
        verify(transactionRepository, times(1)).findByCardId(1L, pageable);
    }

    @DisplayName("deleteCard Должен выбросить исключение при положительном балансе")
    @Test
    void deleteCard_WhenCardHasPositiveBalance_ShouldThrowException() {
        card.setBalance(new BigDecimal("100.00"));
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.deleteCard(1L, 1L))
                .isInstanceOf(CardHasBalanceException.class);
        verify(cardRepository, times(1)).findByIdAndOwnerId(1L, 1L);
        verify(cardRepository, never()).delete(any(Card.class));
        verify(transactionRepository, never()).findByCardId(anyLong(), any(Pageable.class));
    }

    @DisplayName("deleteCard Должен выбросить исключение при наличии транзакций")
    @Test
    void deleteCard_WhenCardHasTransactions_ShouldThrowException() {
        card.setBalance(BigDecimal.ZERO);
        Page<Transaction> transactionsPage = new PageImpl<>(List.of(new Transaction()), pageable, 1);
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(card));
        when(transactionRepository.findByCardId(1L, pageable)).thenReturn(transactionsPage);

        assertThatThrownBy(() -> cardService.deleteCard(1L, 1L))
                .isInstanceOf(CardHasTransactionsException.class);

        verify(cardRepository, times(1)).findByIdAndOwnerId(1L, 1L);
        verify(transactionRepository, times(1)).findByCardId(1L, pageable);
        verify(cardRepository, never()).delete(any(Card.class));
    }

    @DisplayName("isCardOwnedByUser Должен вернуть true когда карта принадлежит пользователю")
    @Test
    void isCardOwnedByUser_WhenCardOwnedByUser_ShouldReturnTrue() {
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(card));

        boolean result = cardService.isCardOwnedByUser(1L, 1L);

        assertThat(result).isTrue();
        verify(cardRepository, times(1)).findByIdAndOwnerId(1L, 1L);
    }

    @DisplayName("isCardOwnedByUser Должен вернуть false когда карта не принадлежит пользователю")
    @Test
    void isCardOwnedByUser_WhenCardNotOwnedByUser_ShouldReturnFalse() {
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.empty());

        boolean result = cardService.isCardOwnedByUser(1L, 1L);

        assertThat(result).isFalse();
        verify(cardRepository, times(1)).findByIdAndOwnerId(1L, 1L);
    }

    @DisplayName("getCardById Должен успешно вернуть карту по ID")
    @Test
    void getCardById_ShouldReturnCardSuccessfully() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Card result = cardService.getCardById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(cardRepository, times(1)).findById(1L);
    }

    @DisplayName("getCardById Должен выбросить исключение при отсутствии карты")
    @Test
    void getCardById_WhenCardNotFound_ShouldThrowException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(1L))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardRepository, times(1)).findById(1L);
    }
}