package ru.effectivemobile.bankcards.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class KeyGeneratorUtil {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // AES-256

        SecretKey secretKey = keyGen.generateKey();
        String base64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        System.out.println("Сгенерированный ключ (Base64):");
        System.out.println(base64Key);
        System.out.println("\nДобавьте этот ключ в application.yml как encryption.secret-key");
    }
}