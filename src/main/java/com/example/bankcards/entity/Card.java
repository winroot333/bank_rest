package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encrypted_card_number", nullable = false, columnDefinition = "TEXT")
    private String encryptedCardNumber;

    @Column(name = "masked_number", length = 19, nullable = false, updatable = false)
    private String maskedNumber;

    @Column(name = "card_holder", length = 100, nullable = false)
    @Size(max = 100, message = "Card holder name must be less than 100 characters")
    private String cardHolder;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private CardStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cards_owner"))
    private User owner;

    /**
     * Ставим статус EXPIRED при загрузке или обновлении
     */
    @PrePersist
    @PreUpdate
    private void validateExpiration() {
        if (expirationDate.isBefore(LocalDate.now())) {
            this.status = CardStatus.EXPIRED;
        }
    }

}
