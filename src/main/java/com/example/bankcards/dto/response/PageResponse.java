package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO для единого стиля ответов с pagination
 *
 * @param <T> Тим элемента сущностей ответа
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Страничный ответ с пагинацией")
public class PageResponse<T> {

    @Schema(description = "Список элементов на текущей странице")
    private List<T> content;

    @Schema(description = "Текущая страница (начинается с 0)", example = "0")
    private int currentPage;

    @Schema(description = "Общее количество страниц", example = "5")
    private int totalPages;

    @Schema(description = "Общее количество элементов", example = "50")
    private long totalElements;

    @Schema(description = "Размер страницы", example = "10")
    private int pageSize;

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize()
        );
    }
}