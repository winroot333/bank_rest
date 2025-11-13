package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на перевод средств между картами")
public class TransferRequest {

    @Schema(description = "ID карты отправителя", example = "2")
    @NotNull(message = "ID карты отправителя не может быть пустым")
    private Long fromCardId;

    @Schema(description = "ID карты получателя", example = "3")
    @NotNull(message = "ID карты получателя не может быть пустым")
    private Long toCardId;

    @Schema(description = "Сумма перевода", example = "1000.50")
    @NotNull(message = "Сумма перевода не может быть пустой")
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше 0")
    private BigDecimal amount;

    @Schema(description = "Описание перевода", example = "Перевод за услуги")
    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;
}
