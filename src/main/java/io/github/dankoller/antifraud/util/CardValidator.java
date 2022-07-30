package io.github.dankoller.antifraud.util;

public class CardValidator {

    /**
     * Checks if a card number is invalid using Luhn algorithm.
     *
     * @param cardNumber A card number to be validated
     * @return True if the card number is invalid, false otherwise
     */
    public static boolean isNonValid(String cardNumber) {
        if (cardNumber == null) return true;
        if (cardNumber.length() != 16) return true;

        int sum = 0;

        for (int i = cardNumber.length(); i > 0; i--) {
            int num = Character.getNumericValue(cardNumber.charAt(i - 1));

            if (i % 2 == 1) num *= 2;
            if (num > 9) num -= 9;

            sum += num;
        }

        return sum % 10 != 0;
    }
}
