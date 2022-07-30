package io.github.dankoller.antifraud.util;

import java.util.regex.Pattern;

public class IPAddressValidator {

    /**
     * Checks if a given IP address is invalid using regular expression.
     *
     * @param ip IP address to be checked
     * @return True if the IP address is invalid, false otherwise
     */
    public static boolean isNonValidIp(String ip) {
        Pattern pattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");

        return !pattern.matcher(ip).matches();
    }
}
