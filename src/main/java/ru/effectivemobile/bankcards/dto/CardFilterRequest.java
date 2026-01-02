package ru.effectivemobile.bankcards.dto;

import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.effectivemobile.bankcards.entity.CardStatus;

public record CardFilterRequest(
        CardStatus status,
        Long userId,
        @Min(0) int page,
        @Min(1) int size,
        String sortBy,
        Sort.Direction direction
) {
     public CardFilterRequest {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Ограничиваем размер страницы
    }

    public Pageable toPageable() {
        if (sortBy != null && direction != null) {
            return PageRequest.of(page, size, Sort.by(direction, sortBy));
        }
        return PageRequest.of(page, size, Sort.by("id").descending()); // Сортировка по умолчанию
    }
    // Статический метод для создания с параметрами по умолчанию
    public static CardFilterRequest defaults() {
        return new CardFilterRequest(null, null, 0, 10, "id", Sort.Direction.DESC);
    }
}