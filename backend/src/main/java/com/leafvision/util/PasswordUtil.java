package com.leafvision.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10000;
    private static final String ALGORITHM = "SHA-256";

    public static String encode(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            byte[] hash = hashPassword(password, salt, ITERATIONS);
            
            return ITERATIONS + ":" + 
                   Base64.getEncoder().encodeToString(salt) + ":" + 
                   Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to encode password", e);
        }
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        
        String[] parts = encodedPassword.split(":");
        if (parts.length != 3) {
            return false;
        }
        
        try {
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hash = Base64.getDecoder().decode(parts[2]);
            
            byte[] testHash = hashPassword(rawPassword, salt, iterations);
            
            return MessageDigest.isEqual(hash, testHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] hashPassword(String password, byte[] salt, int iterations) 
            throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        digest.update(salt);
        
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        
        for (int i = 1; i < iterations; i++) {
            digest.reset();
            hash = digest.digest(hash);
        }
        
        return hash;
    }
}
