package ru.effectivemobile.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import ru.effectivemobile.bankcards.entity.CardStatus;

public record UpdateCardStatusRequest(
        @NotNull
        CardStatus status
) {}