package de.khudhurayaz.freshradar.util;

import org.springframework.security.core.Authentication;

import java.util.regex.Pattern;

public class Validation {
    public static String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static boolean patternMatches(String text, String pattern) {
        return Pattern.compile(pattern)
                .matcher(text).matches();
    }

    public static boolean hasRole(Authentication auth, String role){
        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(a -> role.equals(a.getAuthority())
                || ("ROLE_"  + role).equals(a.getAuthority()));
    }
}
