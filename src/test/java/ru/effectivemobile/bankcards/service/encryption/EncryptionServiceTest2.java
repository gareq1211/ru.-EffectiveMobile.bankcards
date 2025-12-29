package ru.effectivemobile.bankcards.service.encryption;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class EncryptionServiceTest2 {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    void shouldEncryptAndDecryptSuccessfully() {
        // given
        String originalPan = "4556737586899855";

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
        String pan = "4556737586899855";

        // when
        String encrypted1 = encryptionService.encrypt(pan);
        String encrypted2 = encryptionService.encrypt(pan);

        // then
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void shouldThrowExceptionForInvalidPanDuringEncryption() {
        // given
        String invalidPan = "123"; // Не 16 цифр

        // when & then
        assertThatThrownBy(() -> encryptionService.encrypt(invalidPan))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PAN must be exactly 16 digits");
    }

    @Test
    void shouldThrowExceptionForInvalidEncryptedText() {
        // given
        String invalidEncryptedText = "not-a-valid-base64-string";

        // when & then
        assertThatThrownBy(() -> encryptionService.decrypt(invalidEncryptedText))
                .isInstanceOf(EncryptionService.EncryptionException.class)
                .hasMessageContaining("Ошибка при дешифровании");
    }

    @Test
    void shouldThrowExceptionForTamperedEncryptedText() {
        // given
        String originalPan = "4556737586899855";
        String encrypted = encryptionService.encrypt(originalPan);
        String tampered = encrypted.substring(0, encrypted.length() - 5) + "AAAAA"; // Подменяем часть

        // when & then
        assertThatThrownBy(() -> encryptionService.decrypt(tampered))
                .isInstanceOf(EncryptionService.EncryptionException.class);
    }
}