package com.care.service;

import com.care.dao.UserDAO;
import com.care.model.User;
import com.care.util.PasswordUtil;

import java.util.List;

/**
 * Service layer for User operations
 * Handles business logic for user-related operations
 */
public class UserService {
    
    private UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Authenticate a user with email and password
     * 
     * @param email User email
     * @param password User password (plain text)
     * @return User object if authentication successful, null otherwise
     */
    public User authenticate(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        
        // Get user by email first
        User user = userDAO.findByEmail(email);
        
        if (user == null) {
            System.out.println("Authentication failed: User not found for: " + email);
            return null;
        }
        
        // Check if password is BCrypt hash or plain text (for backward compatibility)
        String storedPassword = user.getPasswordHash();
        boolean isAuthenticated = false;
        
        if (PasswordUtil.isBCryptHash(storedPassword)) {
            // Verify against BCrypt hash
            isAuthenticated = PasswordUtil.verifyPassword(password, storedPassword);
        } else {
            // Backward compatibility: plain text comparison
            // This allows existing users with plain text passwords to still login
            isAuthenticated = password.equals(storedPassword);
            
            // Optionally: upgrade to hashed password on successful login
            if (isAuthenticated) {
                System.out.println("Upgrading plain text password to BCrypt hash for: " + email);
                user.setPasswordHash(PasswordUtil.hashPassword(password));
                userDAO.update(user);
            }
        }
        
        if (isAuthenticated) {
            System.out.println("✓ Authentication successful for: " + email);
            return user;
        } else {
            System.out.println("✗ Authentication failed: Invalid password for: " + email);
            return null;
        }
    }
    
    /**
     * Register a new user
     * 
     * @param user User object with registration information (password in plain text)
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(User user) {
        // Check if email already exists
        User existingUser = userDAO.findByEmail(user.getEmail());
        
        if (existingUser != null) {
            System.out.println("Registration failed: Email already exists");
            return false;
        }
        
        // Hash the password before storing
        String plainPassword = user.getPasswordHash();
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        user.setPasswordHash(hashedPassword);
        
        boolean success = userDAO.insert(user);
        
        if (success) {
            System.out.println("✓ User registered successfully: " + user.getEmail());
        }
        
        return success;
    }
    
    /**
     * Get user by ID
     * 
     * @param userId User ID
     * @return User object or null if not found
     */
    public User getUserById(int userId) {
        return userDAO.findById(userId);
    }
    
    /**
     * Get user by email
     * 
     * @param email User email
     * @return User object or null if not found
     */
    public User getUserByEmail(String email) {
        return userDAO.findByEmail(email);
    }
    
    /**
     * Update user information
     * 
     * @param user User object with updated information
     * @return true if update successful, false otherwise
     */
    public boolean updateUser(User user) {
        return userDAO.update(user);
    }
    
    /**
     * Delete a user
     * 
     * @param userId User ID to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        return userDAO.delete(userId);
    }
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    
    /**
     * Validate password strength
     * 
     * @param password Password to validate
     * @return true if password meets requirements, false otherwise
     */
    public boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // Additional password requirements can be added here
        // e.g., must contain uppercase, lowercase, numbers, special characters
        
        return true;
    }
    
    /**
     * Validate email format
     * 
     * @param email Email to validate
     * @return true if email format is valid, false otherwise
     */
    public boolean validateEmailFormat(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

