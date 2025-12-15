package com.care.model;

import java.time.LocalDateTime;

/**
 * Product Model - Represents a product in the CARE system
 * Corresponds to the Products table in the database
 */
public class Product {
    private int productId;
    private String name;
    private String modelVersion;
    private String category;
    private LocalDateTime createdAt;
    
    // Constructors
    public Product() {
    }
    
    public Product(int productId, String name, String modelVersion, String category) {
        this.productId = productId;
        this.name = name;
        this.modelVersion = modelVersion;
        this.category = category;
    }
    
    // Getters and Setters
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getModelVersion() {
        return modelVersion;
    }
    
    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", modelVersion='" + modelVersion + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}

