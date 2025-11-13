package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание карты")
public class CardCreateRequest {

    @Schema(description = "Держатель карты", example = "IVAN IVANOV")
    @Size(min = 2, max = 100, message = "Имя держателя карты должно содержать от 2 до 100 символов")
    @NotBlank(message = "Имя держателя карты не может быть пустым")
    @Pattern(regexp = "^[A-Z\\s]+$", message = "Имя держателя карты должно содержать только заглавные латинские буквы и пробелы")
    private String cardHolder;

}
