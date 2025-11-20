package com.levelup.backend.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PasswordUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtils() {}

    public static String generateSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String hashPassword(String saltBase64, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            byte[] combined = new byte[salt.length + passwordBytes.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(passwordBytes, 0, combined, salt.length, passwordBytes.length);
            return Base64.getEncoder().encodeToString(digest.digest(combined));
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 is not supported", e);
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
