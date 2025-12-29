package ru.effectivemobile.bankcards.service.encryption;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EncryptionServiceTest {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    void shouldEncryptAndDecryptSuccessfully() {
        // given
        String originalPan = "1234567890123456";

        // when
        String encrypted = encryptionService.encrypt(originalPan);
        String decrypted = encryptionService.decrypt(encrypted);

        // then
        assertThat(encrypted).isNotEqualTo(originalPan);
        assertThat(encrypted).isNotBlank();
        assertThat(decrypted).isEqualTo(originalPan);
    }

    @Test
    void shouldProduceDifferentEncryptionEachTime() {
        // given
        String pan = "1234567890123456";

        // when
        String encrypted1 = encryptionService.encrypt(pan);
        String encrypted2 = encryptionService.encrypt(pan);

        // then
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }
}