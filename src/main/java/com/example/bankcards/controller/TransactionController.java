package com.example.bankcards.controller;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.TransactionResponse;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.security.UserSecurity;
import com.example.bankcards.service.TransactionService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Управление транзакциями")
@ApiResponses(@ApiResponse(responseCode = "200", useReturnTypeSchema = true))
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final UserSecurity userSecurity;

    @Operation(summary = "Получить историю все транзакций (для Админа)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Транзакции успешно получены"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    public PageResponse<TransactionResponse> getUserTransactions(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactionsPage = transactionService.getAllTransactions(pageable);
        return transactionMapper.toPageResponse(transactionsPage);
    }

    @Operation(summary = "Перевод между своими картами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/transfer")
    public TransactionResponse transferBetweenCards(
            @RequestBody @Valid TransferRequest request) {

        Transaction transaction = transactionService.transferBetweenOwnCards(
                userSecurity.getLoggedInUserId(), request.getFromCardId(), request.getToCardId(),
                request.getAmount(), request.getDescription());
        return transactionMapper.toResponse(transaction);
    }

    @Operation(summary = "Получить историю транзакций пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Транзакции пользователя успешно получены"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/user/{userId}")
    public PageResponse<TransactionResponse> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactionsPage = transactionService.getUserTransactions(userId, pageable);
        return transactionMapper.toPageResponse(transactionsPage);
    }

    @Operation(summary = "Получить историю транзакций по карте")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Транзакции по карте успешно получены"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/card/{cardId}")
    public PageResponse<TransactionResponse> getCardTransactions(
            @PathVariable Long cardId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> transactionsPage = transactionService.getCardTransactions(cardId, pageable);
        return transactionMapper.toPageResponse(transactionsPage);
    }

}