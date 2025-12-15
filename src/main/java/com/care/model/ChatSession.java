package com.care.model;

import java.time.LocalDateTime;

/**
 * ChatSession Model - Represents a chat session between user and bot/agent
 * Corresponds to the ChatSessions table in the database
 */
public class ChatSession {
    private int sessionId;
    private int userId;
    private Integer productId; // Can be null
    private Integer assignedAgentId; // Can be null
    private String status; // ACTIVE, CLOSED, ESCALATED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    
    // Constructors
    public ChatSession() {
    }
    
    public ChatSession(int sessionId, int userId, Integer productId, String status) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.productId = productId;
        this.status = status;
    }
    
    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public Integer getProductId() {
        return productId;
    }
    
    public void setProductId(Integer productId) {
        this.productId = productId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getAssignedAgentId() {
        return assignedAgentId;
    }
    
    public void setAssignedAgentId(Integer assignedAgentId) {
        this.assignedAgentId = assignedAgentId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        // Parse string to LocalDateTime if needed
        this.createdAt = createdAt != null ? LocalDateTime.parse(createdAt.replace(" ", "T")) : null;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        // Parse string to LocalDateTime if needed
        this.updatedAt = updatedAt != null ? LocalDateTime.parse(updatedAt.replace(" ", "T")) : null;
    }
    
    public LocalDateTime getClosedAt() {
        return closedAt;
    }
    
    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
    
    public void setClosedAt(String closedAt) {
        // Parse string to LocalDateTime if needed
        this.closedAt = closedAt != null ? LocalDateTime.parse(closedAt.replace(" ", "T")) : null;
    }
    
    @Override
    public String toString() {
        return "ChatSession{" +
                "sessionId=" + sessionId +
                ", userId=" + userId +
                ", productId=" + productId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

