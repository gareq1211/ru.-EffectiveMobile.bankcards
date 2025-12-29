package ru.effectivemobile.bankcards.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.effectivemobile.bankcards.entity.Card;
import ru.effectivemobile.bankcards.service.encryption.EncryptionService;
import ru.effectivemobile.bankcards.util.CardUtils;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class CardMapper {

    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");
    private final EncryptionService encryptionService;

    public CardDto toDto(Card card) {
        try {
            String decryptedPan = encryptionService.decrypt(card.getEncryptedPan());
            String maskedPan = CardUtils.maskPan(decryptedPan);

            return new CardDto(
                    card.getId(),
                    maskedPan,
                    card.getOwnerName(),
                    card.getExpiryDate().format(EXPIRY_FORMATTER),
                    card.getStatus().name(),
                    card.getBalance()
            );
        } catch (Exception e) {
            // В случае ошибки дешифрования возвращаем замаскированную версию из зашифрованного текста
            String fallbackMasked = "**** **** **** " + card.getEncryptedPan().substring(
                    Math.max(0, card.getEncryptedPan().length() - 4));
            return new CardDto(
                    card.getId(),
                    fallbackMasked,
                    card.getOwnerName(),
                    card.getExpiryDate().format(EXPIRY_FORMATTER),
                    card.getStatus().name(),
                    card.getBalance()
            );
        }
    }
}