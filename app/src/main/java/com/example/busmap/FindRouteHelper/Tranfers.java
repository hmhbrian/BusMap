package com.example.busmap.FindRouteHelper;

import java.text.NumberFormat;
import java.util.Locale;

public class Tranfers {
    public static String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
    public static String formatTime(double timeInHours) {
        int hours = (int) timeInHours;
        int minutes = (int) ((timeInHours - hours) * 60);
        if(hours >= 1)
            return String.format("%d giờ %d phút", hours, minutes);
        return String.format("%d phút", minutes);
    }
    public static String extractNumbers(String input) {
        return input.replaceAll("[^0-9]", "");
    }

    public static String StringNumberExtractor (String input) {
        String numbers = extractNumbers(input);
        return numbers;
    }
}
