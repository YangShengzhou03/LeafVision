package com.leafvision.util;

public class PasswordUtil {

    public static String encode(String password) {
        return password;
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return rawPassword.equals(encodedPassword);
    }
}
