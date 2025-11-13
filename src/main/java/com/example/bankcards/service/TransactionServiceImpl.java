package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidAmountException;
import com.example.bankcards.exception.UnauthorizedTransferException;
import com.example.bankcards.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Имплементация сервиса по работе с транзакциями
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CardService cardService;

    @Override
    @PreAuthorize("@userSecurity.isCardOwnerOrAdmin(#fromCardId)")
    public Transaction transferBetweenOwnCards(Long userId, Long fromCardId, Long toCardId,
                                               BigDecimal amount, String description) {

        Card fromCard = cardService.getCardByIdAndOwner(fromCardId, userId);
        Card toCard = cardService.getCardByIdAndOwner(toCardId, userId);

        if (!fromCard.getOwner().getId().equals(toCard.getOwner().getId())) {
            throw new UnauthorizedTransferException("Перевод возможен только между своими картами");
        }

        if (fromCard.getId().equals(toCard.getId())) {
            throw new UnauthorizedTransferException("Нельзя перевести на одну и туже карту");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на карте отправителя");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Сумма перевода должна быть положительной");
        }

        cardService.updateCardBalance(fromCardId, fromCard.getBalance().subtract(amount));
        cardService.updateCardBalance(toCardId, toCard.getBalance().add(amount));

        Transaction transaction = Transaction.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(amount)
                .transactionDate(LocalDateTime.now())
                .status(TransactionStatus.COMPLETED)
                .description(description)
                .build();

        var savedTransaction = transactionRepository.save(transaction);
        savedTransaction.getId();
        return savedTransaction;

    }

    @Override
    @PreAuthorize("@userSecurity.isOwnerOrAdmin(#userId)")
    public Page<Transaction> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable);
    }

    @Override
    @PreAuthorize("@userSecurity.isCardOwnerOrAdmin(#cardId)")
    public Page<Transaction> getCardTransactions(Long cardId, Pageable pageable) {
        return transactionRepository.findByCardId(cardId, pageable);
    }

    @Override
    @PreAuthorize("@userSecurity.hasAdminRole()")
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }
}
