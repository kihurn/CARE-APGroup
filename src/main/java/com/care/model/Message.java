package com.care.model;

import java.time.LocalDateTime;

/**
 * Message Model - Represents a message in a chat session
 * Corresponds to the Messages table in the database
 */
public class Message {
    private int messageId;
    private int sessionId;
    private String senderType; // USER, BOT, AGENT
    private String content;
    private LocalDateTime timestamp;
    
    // Constructors
    public Message() {
    }
    
    public Message(int messageId, int sessionId, String senderType, String content) {
        this.messageId = messageId;
        this.sessionId = sessionId;
        this.senderType = senderType;
        this.content = content;
    }
    
    // Getters and Setters
    public int getMessageId() {
        return messageId;
    }
    
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
    
    public int getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getSenderType() {
        return senderType;
    }
    
    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setCreatedAt(String createdAt) {
        // Alias for timestamp, parse string to LocalDateTime
        this.timestamp = createdAt != null ? LocalDateTime.parse(createdAt.replace(" ", "T")) : null;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        // Alias for timestamp
        this.timestamp = createdAt;
    }
    
    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", sessionId=" + sessionId +
                ", senderType='" + senderType + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

