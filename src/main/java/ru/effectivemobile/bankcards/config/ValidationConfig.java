package ru.effectivemobile.bankcards.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.effectivemobile.bankcards.repository.CardRepository;
import ru.effectivemobile.bankcards.service.encryption.EncryptionService;
import ru.effectivemobile.bankcards.validation.UniquePanValidator;

@Configuration
@RequiredArgsConstructor
public class ValidationConfig {

    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;

    @Bean
    public UniquePanValidator uniquePanValidator() {
        return new UniquePanValidator(cardRepository, encryptionService);
    }
}