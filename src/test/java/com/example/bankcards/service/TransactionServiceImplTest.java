package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("Тесты для сервиса транзакций")
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardService cardService;

    private TransactionServiceImpl transactionService;

    private User user;
    private Card fromCard;
    private Card toCard;
    private Transaction transaction;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(transactionRepository, cardService);

        user = User.builder()
                .id(1L)
                .username("IVAN IVANOV")
                .email("ivan@example.com")
                .build();

        fromCard = Card.builder()
                .id(1L)
                .cardHolder("IVAN IVANOV")
                .balance(new BigDecimal("1000.00"))
                .status(CardStatus.ACTIVE)
                .owner(user)
                .build();

        toCard = Card.builder()
                .id(2L)
                .cardHolder("IVAN IVANOV")
                .balance(new BigDecimal("500.00"))
                .status(CardStatus.ACTIVE)
                .owner(user)
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .description("Test transfer")
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @DisplayName("transferBetweenOwnCards Должен успешно выполнить перевод между своими картами")
    @Test
    void transferBetweenOwnCards_ShouldTransferSuccessfully() {
        BigDecimal amount = new BigDecimal("100.00");
        String description = "Test transfer";

        when(cardService.getCardByIdAndOwner(1L, 1L)).thenReturn(fromCard);
        when(cardService.getCardByIdAndOwner(2L, 1L)).thenReturn(toCard);
        when(cardService.updateCardBalance(1L, new BigDecimal("900.00"))).thenReturn(fromCard);
        when(cardService.updateCardBalance(2L, new BigDecimal("600.00"))).thenReturn(toCard);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.transferBetweenOwnCards(1L, 1L, 2L, amount, description);

        assertThat(result).isNotNull();
        assertThat(result.getFromCard()).isEqualTo(fromCard);
        assertThat(result.getToCard()).isEqualTo(toCard);
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(result.getDescription()).isEqualTo(description);

        verify(cardService, times(1)).getCardByIdAndOwner(1L, 1L);
        verify(cardService, times(1)).getCardByIdAndOwner(2L, 1L);
        verify(cardService, times(1)).updateCardBalance(1L, new BigDecimal("900.00"));
        verify(cardService, times(1)).updateCardBalance(2L, new BigDecimal("600.00"));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @DisplayName("transferBetweenOwnCards Должен выбросить исключение при переводе между разными пользователями")
    @Test
    void transferBetweenOwnCards_WhenDifferentOwners_ShouldThrowException() {
        User differentUser = User.builder().id(2L).build();
        toCard.setOwner(differentUser);

        when(cardService.getCardByIdAndOwner(1L, 1L)).thenReturn(fromCard);
        when(cardService.getCardByIdAndOwner(2L, 1L)).thenReturn(toCard);

        assertThatThrownBy(() ->
                transactionService.transferBetweenOwnCards(1L, 1L, 2L, new BigDecimal("100.00"), "Test"))
                .isInstanceOf(UnauthorizedTransferException.class);

        verify(cardService, times(1)).getCardByIdAndOwner(1L, 1L);
        verify(cardService, times(1)).getCardByIdAndOwner(2L, 1L);
        verify(cardService, never()).updateCardBalance(anyLong(), any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @DisplayName("transferBetweenOwnCards Должен выбросить исключение при переводе на ту же карту")
    @Test
    void transferBetweenOwnCards_WhenSameCard_ShouldThrowException() {
        when(cardService.getCardByIdAndOwner(1L, 1L)).thenReturn(fromCard);

        assertThatThrownBy(() ->
                transactionService.transferBetweenOwnCards(
                        1L, 1L, 1L, new BigDecimal("100.00"), "Test"))
                .isInstanceOf(UnauthorizedTransferException.class);

        verify(cardService, times(2)).getCardByIdAndOwner(1L, 1L);
        verify(cardService, never()).updateCardBalance(anyLong(), any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @DisplayName("transferBetweenOwnCards Должен выбросить исключение при недостатке средств")
    @Test
    void transferBetweenOwnCards_WhenInsufficientFunds_ShouldThrowException() {
        BigDecimal largeAmount = new BigDecimal("2000.00");

        when(cardService.getCardByIdAndOwner(1L, 1L)).thenReturn(fromCard);
        when(cardService.getCardByIdAndOwner(2L, 1L)).thenReturn(toCard);

        assertThatThrownBy(() ->
                transactionService.transferBetweenOwnCards(
                        1L, 1L, 2L, largeAmount, "Test"))
                .isInstanceOf(InsufficientFundsException.class);

        verify(cardService, times(1)).getCardByIdAndOwner(1L, 1L);
        verify(cardService, times(1)).getCardByIdAndOwner(2L, 1L);
        verify(cardService, never()).updateCardBalance(anyLong(), any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @DisplayName("transferBetweenOwnCards Должен выбросить исключение при истекшей карте")
    @Test
    void transferBetweenOwnCards_WhenCardExpired_ShouldThrowException() {
        fromCard.setStatus(CardStatus.EXPIRED);

        when(cardService.getCardByIdAndOwner(1L, 1L)).thenReturn(fromCard);
        when(cardService.getCardByIdAndOwner(2L, 1L)).thenReturn(toCard);

        assertThatThrownBy(() ->
                transactionService.transferBetweenOwnCards(1L, 1L, 2L, new BigDecimal("100.00"), "Test"))
                .isInstanceOf(CardExpiredException.class);

        verify(cardService, times(1)).getCardByIdAndOwner(1L, 1L);
        verify(cardService, times(1)).getCardByIdAndOwner(2L, 1L);
        verify(cardService, never()).updateCardBalance(anyLong(), any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @DisplayName("transferBetweenOwnCards Должен выбросить исключение при заблокированной карте")
    @Test
    void transferBetweenOwnCards_WhenCardBlocked_ShouldThrowException() {
        fromCard.setStatus(CardStatus.BLOCKED);

        when(cardService.getCardByIdAndOwner(1L, 1L)).thenReturn(fromCard);
        when(cardService.getCardByIdAndOwner(2L, 1L)).thenReturn(toCard);

        assertThatThrownBy(() ->
                transactionService.transferBetweenOwnCards(1L, 1L, 2L, new BigDecimal("100.00"), "Test"))
                .isInstanceOf(CardBlockedException.class);

        verify(cardService, times(1)).getCardByIdAndOwner(1L, 1L);
        verify(cardService, times(1)).getCardByIdAndOwner(2L, 1L);
        verify(cardService, never()).updateCardBalance(anyLong(), any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @DisplayName("transferBetweenOwnCards Должен выбросить исключение при неположительной сумме")
    @Test
    void transferBetweenOwnCards_WhenInvalidAmount_ShouldThrowException() {
        when(cardService.getCardByIdAndOwner(1L, 1L)).thenReturn(fromCard);
        when(cardService.getCardByIdAndOwner(2L, 1L)).thenReturn(toCard);

        assertThatThrownBy(() ->
                transactionService.transferBetweenOwnCards(1L, 1L, 2L, new BigDecimal("0.00"), "Test"))
                .isInstanceOf(InvalidAmountException.class);

        verify(cardService, times(1)).getCardByIdAndOwner(1L, 1L);
        verify(cardService, times(1)).getCardByIdAndOwner(2L, 1L);
        verify(cardService, never()).updateCardBalance(anyLong(), any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @DisplayName("getUserTransactions Должен вернуть страницу транзакций пользователя")
    @Test
    void getUserTransactions_ShouldReturnUserTransactionsPage() {
        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);
        when(transactionRepository.findByUserId(1L, pageable)).thenReturn(transactionPage);

        Page<Transaction> result = transactionService.getUserTransactions(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(transaction);
        verify(transactionRepository, times(1)).findByUserId(1L, pageable);
    }

    @DisplayName("getCardTransactions Должен вернуть страницу транзакций карты")
    @Test
    void getCardTransactions_ShouldReturnCardTransactionsPage() {
        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);
        when(transactionRepository.findByCardId(1L, pageable)).thenReturn(transactionPage);

        Page<Transaction> result = transactionService.getCardTransactions(1L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(transaction);
        verify(transactionRepository, times(1)).findByCardId(1L, pageable);
    }

    @DisplayName("getAllTransactions Должен вернуть страницу всех транзакций")
    @Test
    void getAllTransactions_ShouldReturnAllTransactionsPage() {
        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);
        when(transactionRepository.findAll(pageable)).thenReturn(transactionPage);

        Page<Transaction> result = transactionService.getAllTransactions(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(transaction);
        verify(transactionRepository, times(1)).findAll(pageable);
    }
}