package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardCreateRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.security.UserSecurity;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.mapper.CardMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Управление картами")
@ApiResponses(@ApiResponse(responseCode = "200", useReturnTypeSchema = true))
public class CardController {
    private final CardService cardService;
    private final CardMapper cardMapper;
    private final UserSecurity userSecurity;


    @Operation(summary = "Создать новую карту")

    @PostMapping
    public CardResponse createCard(@RequestBody @Valid CardCreateRequest request) {
        Card createdCard = cardService.createCard(request.getCardHolder(), userSecurity.getLoggedInUserId());
        return cardMapper.toResponse(createdCard);
    }

    @Operation(summary = "Получить карту по ID")
    @GetMapping("/{cardId}")
    public CardResponse getCard(@PathVariable Long cardId) {
        Card card = cardService.getCardById(cardId);
        return cardMapper.toResponse(card);
    }

    @Operation(summary = "Получить все карты пользователя")
    @GetMapping("/user/{userId}")
    public PageResponse<CardResponse> getUserCards(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) CardStatus status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Card> cardsPage;

        if (status != null) {
            cardsPage = cardService.getUserCardsByStatus(userId, status, pageable);
        } else {
            cardsPage = cardService.getAllUserCards(userId, pageable);
        }

        return cardMapper.toPageResponse(cardsPage);
    }

    @Operation(summary = "Получить все карты (только для ADMIN)")
    @GetMapping
    public PageResponse<CardResponse> getAllCards(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) CardStatus status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Card> cardsPage;

        if (status != null) {
            cardsPage = cardService.getCardsByStatus(status, pageable);
        } else {
            cardsPage = cardService.getAllCards(pageable);
        }

        return cardMapper.toPageResponse(cardsPage);
    }

    @Operation(summary = "Изменить статус карты")
    @PatchMapping("/{cardId}/status")
    public CardResponse updateCardStatus(
            @PathVariable Long cardId,
            @RequestBody @Valid CardStatus status) {

        Card updatedCard = cardService.updateCardStatus(cardId, status);
        return cardMapper.toResponse(updatedCard);
    }

    @Operation(summary = "Удалить карту")
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long cardId,
            @RequestParam Long userId) {

        cardService.deleteCard(cardId, userId);
        return ResponseEntity.noContent().build();
    }
}
