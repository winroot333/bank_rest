package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.TransactionResponse;
import com.example.bankcards.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;


/**
 * Мэппер для преобразования Сущностей транзакций в ДТО
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "fromCardId", source = "fromCard.id")
    @Mapping(target = "toCardId", source = "toCard.id")
    TransactionResponse toResponse(Transaction transaction);

    default PageResponse<TransactionResponse> toPageResponse(Page<Transaction> page) {
        List<TransactionResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.of(
                new PageImpl<>(content, page.getPageable(), page.getTotalElements())
        );
    }

}
