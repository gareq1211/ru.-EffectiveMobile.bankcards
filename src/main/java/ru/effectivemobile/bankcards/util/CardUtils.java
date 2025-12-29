package ru.effectivemobile.bankcards.util;

public class CardUtils {

    public static String maskPan(String pan) {
        if (pan == null || pan.length() < 4) {
            return "****";
        }
        // Формат: **** **** **** 1234
        return "**** **** **** " + pan.substring(pan.length() - 4);
    }

    public static String maskPanForDisplay(String pan) {
        if (pan == null || pan.length() != 16) {
            return maskPan(pan);
        }
        // Формат: 1234 56** **** 1234
        return pan.substring(0, 4) + " " +
                pan.substring(4, 6) + "** **** " +
                pan.substring(12);
    }
}