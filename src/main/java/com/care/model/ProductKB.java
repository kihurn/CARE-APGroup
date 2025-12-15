package com.care.model;

/**
 * Data Transfer Object for Product with Knowledge Base status
 * Used in Admin KB Management view
 */
public class ProductKB {
    private int productId;
    private String productName;
    private String category;
    private String modelVersion;
    private boolean hasManual;
    private String filePath;
    
    public ProductKB(Product product, boolean hasManual, String filePath) {
        this.productId = product.getProductId();
        this.productName = product.getName();
        this.category = product.getCategory();
        this.modelVersion = product.getModelVersion();
        this.hasManual = hasManual;
        this.filePath = filePath;
    }
    
    // Getters for TableView binding
    public int getProductId() {
        return productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getModelVersion() {
        return modelVersion;
    }
    
    public String getManualStatus() {
        return hasManual ? "✅ Uploaded" : "❌ Missing";
    }
    
    public boolean isHasManual() {
        return hasManual;
    }
    
    public String getFilePath() {
        if (filePath == null) return "N/A";
        // Extract just the filename from the path for display
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }
    
    public void setHasManual(boolean hasManual) {
        this.hasManual = hasManual;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

