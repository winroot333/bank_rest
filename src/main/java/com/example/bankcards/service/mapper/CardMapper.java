package com.example.bankcards.service.mapper;

import com.example.bankcards.dto.request.CardCreateRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Мэппер для преобразования Сущностей карт в ДТО
 */
@Mapper(componentModel = "spring")
public interface CardMapper {
    Card toEntity(CardCreateRequest dto);

    CardResponse toResponse(Card entity);
}
