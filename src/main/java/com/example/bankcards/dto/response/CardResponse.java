package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Ответ с информацией о карте")
public class CardResponse {

    @Schema(description = "ID карты", example = "1")
    private Long id;

    @Schema(description = "Маскированный номер карты", example = "123456******5678")
    private String maskedNumber;

    @Schema(description = "Держатель карты", example = "IVAN IVANOV")
    private String cardHolder;

    @Schema(description = "Срок действия карты", example = "2025-12-31")
    private LocalDate expirationDate;

    @Schema(description = "Статус карты", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Баланс карты", example = "1500.75")
    private BigDecimal balance;
}
