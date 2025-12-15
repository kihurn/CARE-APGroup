package com.care.model;

import java.time.LocalDateTime;

/**
 * Ticket Model - Represents an escalated support ticket
 * Corresponds to the Tickets table in the database
 */
public class Ticket {
    private int ticketId;
    private int sessionId;
    private Integer assignedAgentId; // Can be null
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    
    // Constructors
    public Ticket() {
    }
    
    public Ticket(int ticketId, int sessionId, String status, String priority) {
        this.ticketId = ticketId;
        this.sessionId = sessionId;
        this.status = status;
        this.priority = priority;
    }
    
    // Getters and Setters
    public int getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }
    
    public int getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    
    public Integer getAssignedAgentId() {
        return assignedAgentId;
    }
    
    public void setAssignedAgentId(Integer assignedAgentId) {
        this.assignedAgentId = assignedAgentId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
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
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId=" + ticketId +
                ", sessionId=" + sessionId +
                ", assignedAgentId=" + assignedAgentId +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }
}

