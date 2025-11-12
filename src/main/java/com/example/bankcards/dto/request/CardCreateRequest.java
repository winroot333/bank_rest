package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Запрос на создание карты")
public class CardCreateRequest {

    @Schema(description = "Номер карты", example = "1234567812345678")
    @Size(min = 16, max = 19, message = "Номер карты должен содержать от 16 до 19 символов")
    @NotBlank(message = "Номер карты не может быть пустым")
    @Pattern(regexp = "^[0-9]+$", message = "Номер карты должен содержать только цифры")
    private String cardNumber;

    @Schema(description = "Держатель карты", example = "IVAN IVANOV")
    @Size(min = 2, max = 100, message = "Имя держателя карты должно содержать от 2 до 100 символов")
    @NotBlank(message = "Имя держателя карты не может быть пустым")
    @Pattern(regexp = "^[A-Z\\s]+$", message = "Имя держателя карты должно содержать только заглавные латинские буквы и пробелы")
    private String cardHolder;

    @Schema(description = "Срок действия карты", example = "2025-12-31")
    @NotNull(message = "Срок действия карты не может быть пустым")
    @Future(message = "Срок действия карты должен быть в будущем")
    private LocalDate expirationDate;
}
