package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardHasBalanceException;
import com.example.bankcards.exception.CardHasTransactionsException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.CardNumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Имплементация сервиса для работы с картами
 */
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private static final int CARD_EXPIRY_YEARS = 3;
    private final CardRepository cardRepository;
    private final UserService userService;
    private final CardNumberUtil cardNumberUtil;
    private final TransactionRepository transactionRepository;

    @Override
    @PreAuthorize("@userSecurity.isOwnerOrAdmin(#userId)")
    public Card createCard(String cardHolder, Long userId) {
        String cardNumber = cardNumberUtil.generateCardNumber();
        Card card = Card.builder()
                .cardHolder(cardHolder)
                .encryptedCardNumber(cardNumberUtil.encryptCardNumber(cardNumber))
                .maskedNumber(cardNumberUtil.maskCardNumber(cardNumber))
                .balance(BigDecimal.ZERO)
                .owner(userService.getById(userId))
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(CARD_EXPIRY_YEARS))
                .build();

        return cardRepository.save(card);
    }

    @Override
    @PreAuthorize("@userSecurity.isCardOwnerOrAdmin(#cardId)")
    public Card updateCardStatus(Long cardId, CardStatus status) {
        Card card = getCardById(cardId);

        if (status == CardStatus.BLOCKED) {
            card.setStatus(status);
            return cardRepository.save(card);
        } else {
            return updateCardStatusByAdmin(cardId, status);
        }
    }

    @Override
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public Card updateCardStatusByAdmin(Long cardId, CardStatus status) {
        Card card = getCardById(cardId);
        card.setStatus(status);
        return cardRepository.save(card);
    }

    @Override
    @PreAuthorize("@userSecurity.isCardOwnerOrAdmin(#cardId)")
    public Card updateCardBalance(Long cardId, BigDecimal balance) {
        Card card = getCardById(cardId);
        card.setBalance(balance);
        return cardRepository.save(card);
    }

    @Override
    @PreAuthorize("@userSecurity.isOwnerOrAdmin(#userId)")
    public Card getCardByIdAndOwner(Long cardId, Long userId) {
        return cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена или нет доступа"));
    }

    @Override
    @PreAuthorize("@userSecurity.isOwnerOrAdmin(#userId)")
    public Page<Card> getAllUserCards(Long userId, Pageable pageable) {
        return cardRepository.findByOwnerId(userId, pageable);
    }

    @Override
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    @Override
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public Page<Card> getCardsByStatus(CardStatus status, Pageable pageable) {
        return cardRepository.findAllByStatus(status, pageable);
    }


    @Override
    @PreAuthorize("@userSecurity.isOwnerOrAdmin(#userId)")
    public Page<Card> getUserCardsByStatus(Long userId, CardStatus status, Pageable pageable) {
        return cardRepository.findByOwnerIdAndStatus(userId, status, pageable);
    }

    @Override
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public void deleteCard(Long cardId, Long userId) {
        Card card = getCardByIdAndOwner(cardId, userId);

        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new CardHasBalanceException("Нельзя удалить карту с положительным балансом");
        }

        Pageable pageable = PageRequest.of(0, 1, Sort.by("id").descending());
        var transactions = transactionRepository.findByCardId(cardId, pageable);
        if (!transactions.isEmpty()) {
            throw new CardHasTransactionsException("Нельзя удалить карту с существующими транзакциями");
        }

        cardRepository.delete(card);
    }

    @Override
    @PreAuthorize("@userSecurity.isOwnerOrAdmin(#userId)")
    public boolean isCardOwnedByUser(Long cardId, Long userId) {
        return cardRepository.findByIdAndOwnerId(cardId, userId).isPresent();
    }

    @Override
    @PreAuthorize("@userSecurity.isCardOwnerOrAdmin(#cardId)")
    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена с ID: " + cardId));
    }


}
