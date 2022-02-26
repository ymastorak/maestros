package com.ymastorak.maestros.utils;

import java.math.BigDecimal;
import java.util.Locale;

public class Utils {

    public static String normalizeGreekString(String value) {
        final String grCharKey = "αάβγδεέζηήθιίϊ̈́ϊκλμνξοόπρσςτυύφχψωώ";
        final String enCharVal = "aavgdeeziiTiiiiiklmnKooprsstuufhPoo";

        value = value.replaceAll("\\s","").toLowerCase(Locale.ROOT);
        char[] nameChars = new char[value.length()];
        value.getChars(0, value.length(), nameChars, 0);

        for (int i=0; i < nameChars.length; i++) {
            int grIndex = grCharKey.indexOf(nameChars[i]);
            if (grIndex >= 0) {
                nameChars[i] = enCharVal.charAt(grIndex);
            }
        }
        return new String(nameChars).replaceAll("[^a-zA-Z0-9]", "");
    }

    public static int extractInteger(String value) {
        return Integer.parseInt(value.replaceAll("[^0-9]", ""));
    }

    public static String moneyAmountToString(BigDecimal amount) {
        return amount.toPlainString();
    }
}
