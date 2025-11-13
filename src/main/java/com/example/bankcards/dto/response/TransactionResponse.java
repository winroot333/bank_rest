package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для ответа по транзакции
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ с информацией о транзакции")
public class TransactionResponse {

    @Schema(description = "ID транзакции", example = "1")
    private Long id;

    @Schema(description = "ID карты отправителя", example = "1")
    private Long fromCardId;

    @Schema(description = "ID карты получателя", example = "2")
    private Long toCardId;

    @Schema(description = "Сумма транзакции", example = "1000.50")
    private BigDecimal amount;

    @Schema(description = "Дата и время транзакции", example = "2024-01-15T14:30:00")
    private LocalDateTime transactionDate;

    @Schema(description = "Статус транзакции", example = "COMPLETED")
    private TransactionStatus status;

    @Schema(description = "Описание транзакции", example = "Перевод за услуги")
    private String description;
}
