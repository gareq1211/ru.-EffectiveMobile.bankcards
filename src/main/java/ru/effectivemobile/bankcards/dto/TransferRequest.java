package ru.effectivemobile.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull
        Long fromCardId,

        @NotNull
        Long toCardId,

        @NotNull
        @Positive
        BigDecimal amount
) {}