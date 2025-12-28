package ru.effectivemobile.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateCardRequest(
        @NotBlank
        @Pattern(regexp = "\\d{16}", message = "PAN must be 16 digits")
        String pan,

        @NotBlank
        String ownerName,

        @NotBlank
        @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Expiry date must be in MM/yy format")
        String expiryDate,

        @NotNull
        @PositiveOrZero
        BigDecimal initialBalance,

        @NotNull
        Long userId
) {}