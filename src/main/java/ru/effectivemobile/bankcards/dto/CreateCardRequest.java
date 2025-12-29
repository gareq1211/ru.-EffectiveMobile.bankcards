package ru.effectivemobile.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import ru.effectivemobile.bankcards.validation.UniquePan;

import java.math.BigDecimal;

public record CreateCardRequest(
        @NotBlank(message = "PAN is required")
        @Pattern(regexp = "\\d{16}", message = "PAN must be exactly 16 digits")
        @UniquePan(message = "Card with this PAN already exists")
        String pan,

        @NotBlank(message = "Owner name is required")
        @Pattern(regexp = "^[a-zA-Z\\s]{2,100}$", message = "Owner name must contain only letters and spaces (2-100 characters)")
        String ownerName,

        @NotBlank(message = "Expiry date is required")
        @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Expiry date must be in MM/yy format")
        String expiryDate,

        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.00", message = "Initial balance cannot be negative")
        BigDecimal initialBalance,

        @NotNull(message = "User ID is required")
        Long userId
) {}