package com.care.model;

import java.time.LocalDateTime;

/**
 * User Model - Represents a user in the CARE system
 * Corresponds to the Users table in the database
 */
public class User {
    private int userId;
    private String email;
    private String passwordHash;
    private String role; // USER, ADMIN, AGENT
    private String name;
    private String licenseKey;
    private boolean is2faEnabled;
    private String preferredLanguage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public User() {
    }
    
    public User(int userId, String email, String passwordHash, String role, String name) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.name = name;
        this.is2faEnabled = false;
        this.preferredLanguage = "en";
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLicenseKey() {
        return licenseKey;
    }
    
    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }
    
    public boolean is2faEnabled() {
        return is2faEnabled;
    }
    
    public void setIs2faEnabled(boolean is2faEnabled) {
        this.is2faEnabled = is2faEnabled;
    }
    
    public String getPreferredLanguage() {
        return preferredLanguage;
    }
    
    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

