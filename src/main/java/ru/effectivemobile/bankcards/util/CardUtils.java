package ru.effectivemobile.bankcards.util;

public class CardUtils {

    /**
     * Маскирует PAN: "1234567890123456" → "**** **** **** 3456"
     */
    public static String maskPan(String pan) {
        if (pan == null || pan.length() < 4) {
            return "****";
        }
        String last4 = pan.substring(pan.length() - 4);
        return "**** **** **** " + last4;
    }
}