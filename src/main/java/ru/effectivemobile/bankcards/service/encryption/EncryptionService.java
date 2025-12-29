package ru.effectivemobile.bankcards.service.encryption;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private static final int TAG_LENGTH_BIT = 128; // 16 байт
    private static final int IV_LENGTH_BYTE = 12; // 12 байт для GCM (рекомендуется)

    @Value("${encryption.secret-key}")
    private String secretKeyBase64;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyBase64);
        this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    /**
     * Шифрует текст (PAN) с использованием AES-256-GCM
     * @param plainText исходный текст (16 цифр PAN)
     * @return строка в формате Base64(IV + encryptedText)
     */
    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(plainTextBytes);

            // Объединяем IV и зашифрованный текст
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании", e);
        }
    }

    /**
     * Дешифрует текст
     * @param encryptedTextBase64 зашифрованный текст в Base64
     * @return расшифрованный PAN
     */
    public String decrypt(String encryptedTextBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedTextBase64);

            // Извлекаем IV (первые IV_LENGTH_BYTE байт)
            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            // Извлекаем зашифрованный текст
            byte[] encryptedBytes = new byte[combined.length - IV_LENGTH_BYTE];
            System.arraycopy(combined, IV_LENGTH_BYTE, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при дешифровании", e);
        }
    }
}