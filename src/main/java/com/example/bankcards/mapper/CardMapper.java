package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

/**
 * Мэппер для преобразования Сущностей карт в ДТО
 */
@Mapper(componentModel = "spring")
public interface CardMapper {

    CardResponse toResponse(Card entity);

    default PageResponse<CardResponse> toPageResponse(Page<Card> page) {
        List<CardResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.of(
                new PageImpl<>(content, page.getPageable(), page.getTotalElements())
        );
    }

}
