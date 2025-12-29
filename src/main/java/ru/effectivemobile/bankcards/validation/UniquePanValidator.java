package ru.effectivemobile.bankcards.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import ru.effectivemobile.bankcards.service.encryption.EncryptionService;
import ru.effectivemobile.bankcards.repository.CardRepository;

import java.util.List;

@RequiredArgsConstructor
public class UniquePanValidator implements ConstraintValidator<UniquePan, String> {

    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;

    @Override
    public boolean isValid(String pan, ConstraintValidatorContext context) {
        if (pan == null || pan.isBlank()) {
            return true;
        }

        // Получаем все зашифрованные PAN из базы
        List<String> allEncryptedPans = cardRepository.findAll().stream()
                .map(card -> card.getEncryptedPan())
                .toList();

        // Шифруем предоставленный PAN
        String encryptedPan = encryptionService.encrypt(pan);

        // Проверяем, что зашифрованный PAN не существует в базе
        return !allEncryptedPans.contains(encryptedPan);
    }
}