package ru.effectivemobile.bankcards.dto;

import java.math.BigDecimal;

public record CardDto(
        Long id,
        String maskedPan,
        String ownerName,
        String expiryDate,
        String status,
        BigDecimal balance
) {}