package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardCreateRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.UserSecurity;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardNumberUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Тесты для контроллера управления картами /api/cards")
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    PageResponse<CardResponse> pageResponse;
    @MockitoBean
    private CardService cardService;
    @MockitoBean
    private CardMapper cardMapper;
    @MockitoBean
    private UserSecurity userSecurity;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CardNumberUtil cardNumberUtil;
    
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private CardCreateRequest cardCreateRequest;
    private Card card;
    private CardResponse cardResponse;
    private Page<Card> cardPage;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("IVAN IVANOV")
                .email("ivan@example.com")
                .build();

        cardCreateRequest = new CardCreateRequest("IVAN IVANOV");

        card = Card.builder()
                .id(1L)
                .encryptedCardNumber("MTIzNDU2NzgxMjM0NTY3OA==")
                .maskedNumber("**** **** **** 5678")
                .cardHolder("IVAN IVANOV")
                .expirationDate(LocalDate.now().plusYears(3))
                .balance(new BigDecimal("0.00"))
                .status(CardStatus.ACTIVE)
                .owner(user)
                .build();

        cardResponse = new CardResponse(
                1L,
                "**** **** **** 5678",
                "IVAN IVANOV",
                LocalDate.now().plusYears(3),
                CardStatus.ACTIVE,
                new BigDecimal("0.00")
        );

        cardPage = new PageImpl<>(List.of(card), PageRequest.of(0, 10), 1);
        pageResponse = new PageResponse<>(
                List.of(cardResponse), 0, 1, 1, 10);
    }

    @DisplayName("POST /api/cards Должен успешно создать новую карту")
    @Test
    void createCard_ShouldCreateCardSuccessfully() throws Exception {
        when(userSecurity.getLoggedInUserId()).thenReturn(1L);
        when(cardService.createCard("IVAN IVANOV", 1L)).thenReturn(card);
        when(cardMapper.toResponse(card)).thenReturn(cardResponse);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardHolder").value("IVAN IVANOV"))
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 5678"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(cardService, times(1)).createCard("IVAN IVANOV", 1L);
    }

    @DisplayName("POST /api/cards Должен вернуть 400 при невалидных данных")
    @Test
    void createCard_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        CardCreateRequest invalidRequest = new CardCreateRequest("");

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("GET /api/cards/{cardId} Должен успешно получить карту по ID")
    @Test
    void getCard_ShouldReturnCardSuccessfully() throws Exception {
        when(cardService.getCardById(1L)).thenReturn(card);
        when(cardMapper.toResponse(card)).thenReturn(cardResponse);

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 5678"))
                .andExpect(jsonPath("$.cardHolder").value("IVAN IVANOV"))
                .andExpect(jsonPath("$.balance").value(0.00));

        verify(cardService, times(1)).getCardById(1L);
    }

    @DisplayName("GET /api/cards/{cardId} Должен вернуть 404 когда карта не найдена")
    @Test
    void getCard_WhenCardNotFound_ShouldReturnNotFound() throws Exception {
        when(cardService.getCardById(1L))
                .thenThrow(new CardNotFoundException("Карта не найдена"));

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Карта не найдена"));
    }

    @DisplayName("GET /api/cards/user/{userId} Должен успешно получить карты пользователя")
    @Test
    void getUserCards_ShouldReturnUserCardsSuccessfully() throws Exception {

        when(cardService.getAllUserCards(1L, PageRequest.of(0, 10, Sort.by("id").ascending())))
                .thenReturn(cardPage);
        when(cardMapper.toPageResponse(cardPage)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/cards/user/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(cardService, times(1)).getAllUserCards(1L, PageRequest.of(0, 10, Sort.by("id").ascending()));
    }

    @DisplayName("GET /api/cards/user/{userId} Должен успешно получить карты пользователя по статусу")
    @Test
    void getUserCards_WithStatusFilter_ShouldReturnFilteredCards() throws Exception {

        when(cardService.getUserCardsByStatus(1L, CardStatus.ACTIVE, PageRequest.of(0, 10, Sort.by("id").ascending())))
                .thenReturn(cardPage);
        when(cardMapper.toPageResponse(cardPage)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/cards/user/1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));

        verify(cardService, times(1)).getUserCardsByStatus(1L, CardStatus.ACTIVE, PageRequest.of(0, 10, Sort.by("id").ascending()));
    }

    @DisplayName("GET /api/cards Должен успешно получить все карты (для ADMIN)")
    @Test
    void getAllCards_ShouldReturnAllCardsSuccessfully() throws Exception {

        when(cardService.getAllCards(PageRequest.of(0, 10, Sort.by("id").descending())))
                .thenReturn(cardPage);
        when(cardMapper.toPageResponse(cardPage)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(cardService, times(1)).getAllCards(PageRequest.of(0, 10, Sort.by("id").descending()));
    }

    @DisplayName("GET /api/cards Должен успешно получить все карты по статусу")
    @Test
    void getAllCards_WithStatusFilter_ShouldReturnFilteredCards() throws Exception {

        when(cardService.getCardsByStatus(CardStatus.ACTIVE, PageRequest.of(0, 10, Sort.by("id").descending())))
                .thenReturn(cardPage);
        when(cardMapper.toPageResponse(cardPage)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));

        verify(cardService, times(1)).getCardsByStatus(CardStatus.ACTIVE, PageRequest.of(0, 10, Sort.by("id").descending()));
    }

    @DisplayName("PATCH /api/cards/{cardId}/status Должен успешно изменить статус карты")
    @Test
    void updateCardStatus_ShouldUpdateStatusSuccessfully() throws Exception {
        Card updatedCard = Card.builder()
                .id(1L)
                .encryptedCardNumber("MTIzNDU2NzgxMjM0NTY3OA==")
                .maskedNumber("**** **** **** 5678")
                .cardHolder("IVAN IVANOV")
                .expirationDate(LocalDate.now().plusYears(3))
                .balance(new BigDecimal("0.00"))
                .status(CardStatus.BLOCKED)
                .owner(user)
                .build();

        CardResponse updatedResponse = new CardResponse(
                1L,
                "**** **** **** 5678",
                "IVAN IVANOV",
                LocalDate.now().plusYears(3),
                CardStatus.BLOCKED,
                new BigDecimal("0.00")
        );

        when(cardService.updateCardStatus(1L, CardStatus.BLOCKED)).thenReturn(updatedCard);
        when(cardMapper.toResponse(updatedCard)).thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/cards/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"BLOCKED\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(cardService, times(1)).updateCardStatus(1L, CardStatus.BLOCKED);
    }

    @DisplayName("PATCH /api/cards/{cardId}/status Должен вернуть 404 когда карта не найдена")
    @Test
    void updateCardStatus_WhenCardNotFound_ShouldReturnNotFound() throws Exception {
        when(cardService.updateCardStatus(1L, CardStatus.BLOCKED))
                .thenThrow(new CardNotFoundException("Карта не найдена"));

        mockMvc.perform(patch("/api/cards/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"BLOCKED\""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Карта не найдена"));
    }

    @DisplayName("DELETE /api/cards/{cardId} Должен успешно удалить карту")
    @Test
    void deleteCard_ShouldDeleteCardSuccessfully() throws Exception {
        doNothing().when(cardService).deleteCard(1L, 1L);

        mockMvc.perform(delete("/api/cards/1")
                        .param("userId", "1"))
                .andExpect(status().isNoContent());

        verify(cardService, times(1)).deleteCard(1L, 1L);
    }

    @DisplayName("DELETE /api/cards/{cardId} Должен вернуть 404 когда карта не найдена")
    @Test
    void deleteCard_WhenCardNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new CardNotFoundException("Карта не найдена"))
                .when(cardService).deleteCard(1L, 1L);

        mockMvc.perform(delete("/api/cards/1")
                        .param("userId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Карта не найдена"));
    }

    @DisplayName("GET /api/cards/user/{userId} Должен вернуть 400 при невалидных параметрах пагинации")
    @Test
    void getUserCards_WithInvalidPagination_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/cards/user/1")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("GET /api/cards/user/{userId} Должен вернуть карты с истекшим сроком действия")
    @Test
    void getUserCards_WithExpiredCards_ShouldReturnExpiredStatus() throws Exception {
        Card expiredCard = Card.builder()
                .id(2L)
                .encryptedCardNumber("MTIzNDU2NzgxMjM0NTY3OA==")
                .maskedNumber("**** **** **** 9999")
                .cardHolder("IVAN IVANOV")
                .expirationDate(LocalDate.now().minusDays(1))
                .balance(new BigDecimal("100.00"))
                .status(CardStatus.EXPIRED)
                .owner(user)
                .build();

        CardResponse expiredResponse = new CardResponse(
                2L,
                "**** **** **** 9999",
                "IVAN IVANOV",
                LocalDate.now().minusDays(1),
                CardStatus.EXPIRED,
                new BigDecimal("100.00")
        );

        Page<Card> expiredCardPage = new PageImpl<>(List.of(expiredCard));
        PageResponse<CardResponse> pageResponse = new PageResponse<>(
                List.of(expiredResponse), 0, 1, 1, 10);

        when(cardService.getUserCardsByStatus(1L, CardStatus.EXPIRED, PageRequest.of(0, 10, Sort.by("id").ascending())))
                .thenReturn(expiredCardPage);
        when(cardMapper.toPageResponse(expiredCardPage)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/cards/user/1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "EXPIRED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("EXPIRED"))
                .andExpect(jsonPath("$.content[0].expirationDate").exists());
    }
}