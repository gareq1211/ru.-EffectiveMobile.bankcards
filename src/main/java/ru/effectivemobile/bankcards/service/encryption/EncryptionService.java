package ru.effectivemobile.bankcards.service.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int KEY_LENGTH_BIT = 256;

    @Value("${encryption.secret-key:#{null}}") // Безопасное значение по умолчанию
    private String secretKeyBase64;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(secretKeyBase64)) {
            // Для тестов или разработки создаём временный ключ
            System.err.println("WARNING: encryption.secret-key not set, using development key");
            generateDevelopmentKey();
        } else {
            try {
                byte[] decodedKey = Base64.getDecoder().decode(secretKeyBase64);
                if (decodedKey.length != KEY_LENGTH_BIT / 8) { // 256 бит = 32 байта
                    throw new IllegalArgumentException("Key must be 256 bits (32 bytes)");
                }
                this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid encryption key format. Must be Base64 encoded 256-bit key.", e);
            }
        }
    }

    private void generateDevelopmentKey() {
        try {
            SecureRandom secureRandom = new SecureRandom();
            byte[] key = new byte[KEY_LENGTH_BIT / 8]; // 32 байта для AES-256
            secureRandom.nextBytes(key);
            this.secretKey = new SecretKeySpec(key, "AES");
            System.err.println("Development key generated. DO NOT USE IN PRODUCTION!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate development key", e);
        }
    }

    public String encrypt(String plainText) {
        validatePlainText(plainText);

        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(plainTextBytes);

            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new EncryptionException("Ошибка при шифровании", e);
        }
    }

    public String decrypt(String encryptedTextBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedTextBase64);

            if (combined.length < IV_LENGTH_BYTE + 1) {
                throw new IllegalArgumentException("Invalid encrypted text length");
            }

            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            byte[] encryptedBytes = new byte[combined.length - IV_LENGTH_BYTE];
            System.arraycopy(combined, IV_LENGTH_BYTE, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String result = new String(decryptedBytes, StandardCharsets.UTF_8);

            validatePlainText(result); // Проверяем что расшифровали валидный PAN

            return result;

        } catch (Exception e) {
            throw new EncryptionException("Ошибка при дешифровании", e);
        }
    }

    private void validatePlainText(String plainText) {
        if (!plainText.matches("\\d{16}")) {
            throw new IllegalArgumentException("PAN must be exactly 16 digits");
        }
    }

    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}