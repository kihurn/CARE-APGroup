package com.care.dao;

import com.care.model.User;
import com.care.util.DatabaseDriver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Users table
 * Handles all database operations related to users
 */
public class UserDAO {
    
    private Connection connection;
    
    public UserDAO() {
        this.connection = DatabaseDriver.getInstance().getConnection();
    }
    
    /**
     * Find a user by email and password
     * 
     * @param email User email
     * @param password User password
     * @return User object or null if not found
     */
    public User findByEmailAndPassword(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email and password");
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Find a user by email
     * 
     * @param email User email
     * @return User object or null if not found
     */
    public User findByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email");
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Find a user by ID
     * 
     * @param userId User ID
     * @return User object or null if not found
     */
    public User findById(int userId) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID");
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Insert a new user into the database
     * 
     * @param user User object to insert
     * @return true if successful, false otherwise
     */
    public boolean insert(User user) {
        String query = "INSERT INTO users (email, password_hash, role, name, license_key, is_2fa_enabled, preferred_language) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getName());
            stmt.setString(5, user.getLicenseKey());
            stmt.setInt(6, user.is2faEnabled() ? 1 : 0);
            stmt.setString(7, user.getPreferredLanguage());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error inserting user");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing user
     * 
     * @param user User object with updated information
     * @return true if successful, false otherwise
     */
    public boolean update(User user) {
        String query = "UPDATE users SET email = ?, password_hash = ?, role = ?, name = ?, " +
                      "license_key = ?, is_2fa_enabled = ?, preferred_language = ? WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getName());
            stmt.setString(5, user.getLicenseKey());
            stmt.setInt(6, user.is2faEnabled() ? 1 : 0);
            stmt.setString(7, user.getPreferredLanguage());
            stmt.setInt(8, user.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a user by ID
     * 
     * @param userId User ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int userId) {
        String query = "DELETE FROM users WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users");
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Map ResultSet to User object
     * 
     * @param rs ResultSet from query
     * @return User object
     * @throws SQLException if column not found
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setName(rs.getString("name"));
        user.setLicenseKey(rs.getString("license_key"));
        user.setIs2faEnabled(rs.getInt("is_2fa_enabled") == 1);
        user.setPreferredLanguage(rs.getString("preferred_language"));
        try {
            java.sql.Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) {
                user.setCreatedAt(ts.toLocalDateTime());
            }
        } catch (Exception ignored) {}
        return user;
    }
}

