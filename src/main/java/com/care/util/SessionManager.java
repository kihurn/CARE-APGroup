package com.care.util;

import com.care.model.ChatSession;
import com.care.model.Product;
import com.care.model.User;

/**
 * Singleton class to manage user session
 * Stores currently logged-in user information, selected product, and current chat session
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Product selectedProduct;
    private ChatSession currentChatSession;
    
    /**
     * Private constructor to prevent instantiation
     */
    private SessionManager() {
        this.currentUser = null;
    }
    
    /**
     * Get the singleton instance of SessionManager
     * 
     * @return SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Set the current logged-in user
     * 
     * @param user User object
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Get the current logged-in user
     * 
     * @return User object or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if a user is currently logged in
     * 
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Get current user ID
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
    
    /**
     * Log out the current user
     */
    public void logout() {
        this.currentUser = null;
        this.selectedProduct = null;
        this.currentChatSession = null;
    }
    
    /**
     * Get the role of the current user
     * 
     * @return Role as String or null if no user is logged in
     */
    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
    
    /**
     * Set the selected product for chat
     */
    public void setSelectedProduct(Product product) {
        this.selectedProduct = product;
    }
    
    /**
     * Get the selected product
     */
    public Product getSelectedProduct() {
        return selectedProduct;
    }
    
    /**
     * Set the current chat session (for continuing previous chats)
     */
    public void setCurrentChatSession(ChatSession session) {
        this.currentChatSession = session;
    }
    
    /**
     * Get the current chat session
     */
    public ChatSession getCurrentChatSession() {
        return currentChatSession;
    }
    
    /**
     * Clear the current chat session
     */
    public void clearCurrentChatSession() {
        this.currentChatSession = null;
    }
}

