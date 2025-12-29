package ru.effectivemobile.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "Source card ID is required")
        Long fromCardId,

        @NotNull(message = "Target card ID is required")
        Long toCardId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount
) {}