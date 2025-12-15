package com.care.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt
 */
public class PasswordUtil {
    
    // BCrypt workload factor (10 = 2^10 rounds, good balance of security and performance)
    private static final int WORKLOAD = 10;
    
    /**
     * Hash a password using BCrypt
     * @param plainTextPassword The plain text password to hash
     * @return The hashed password
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(plainTextPassword, salt);
    }
    
    /**
     * Verify a password against a hash
     * @param plainTextPassword The plain text password to verify
     * @param hashedPassword The hashed password from database
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format (might be plain text from old data)
            System.err.println("Warning: Invalid password hash format - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a string is a valid BCrypt hash
     * @param hash The string to check
     * @return true if it's a valid BCrypt hash, false otherwise
     */
    public static boolean isBCryptHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return false;
        }
        
        // BCrypt hashes start with $2a$, $2b$, or $2y$ and are 60 characters long
        return hash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}

