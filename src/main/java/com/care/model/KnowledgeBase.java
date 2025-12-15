package com.care.model;

import java.time.LocalDateTime;

/**
 * KnowledgeBase Model - Represents a knowledge base article
 * Corresponds to the KnowledgeBase table in the database
 */
public class KnowledgeBase {
    private int kbId;
    private int productId;
    private String title;
    private String content;
    private String filePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public KnowledgeBase() {
    }
    
    public KnowledgeBase(int kbId, int productId, String title, String content) {
        this.kbId = kbId;
        this.productId = productId;
        this.title = title;
        this.content = content;
    }
    
    // Getters and Setters
    public int getKbId() {
        return kbId;
    }
    
    public void setKbId(int kbId) {
        this.kbId = kbId;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
        return "KnowledgeBase{" +
                "kbId=" + kbId +
                ", productId=" + productId +
                ", title='" + title + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}

