package ru.effectivemobile.bankcards.dto;

import ru.effectivemobile.bankcards.entity.Card;
import ru.effectivemobile.bankcards.util.CardUtils;

import java.time.format.DateTimeFormatter;

public class CardMapper {

    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    public static CardDto toDto(Card card) {
        return new CardDto(
                card.getId(),
                CardUtils.maskPan(card.getEncryptedPan()), // ⚠️ временно: encryptedPan == pan
                card.getOwnerName(),
                card.getExpiryDate().format(EXPIRY_FORMATTER),
                card.getStatus().name(),
                card.getBalance()
        );
    }
}