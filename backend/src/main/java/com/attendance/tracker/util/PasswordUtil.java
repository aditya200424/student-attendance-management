package com.attendance.tracker.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 password hashing utility. No plain-text passwords are ever stored.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return hash(rawPassword).equals(hashedPassword);
    }
}
