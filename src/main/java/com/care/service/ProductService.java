package com.care.service;

import com.care.dao.ProductDAO;
import com.care.model.Product;

import java.util.List;

/**
 * Service layer for Product operations
 * Handles business logic for product-related operations
 */
public class ProductService {
    
    private ProductDAO productDAO;
    
    public ProductService() {
        this.productDAO = new ProductDAO();
    }
    
    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }
    
    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(String category) {
        return productDAO.getProductsByCategory(category);
    }
    
    /**
     * Get all unique categories
     */
    public List<String> getAllCategories() {
        return productDAO.getAllCategories();
    }
    
    /**
     * Get product by ID
     */
    public Product getProductById(int productId) {
        return productDAO.findById(productId);
    }
    
    /**
     * Add new product
     */
    public boolean addProduct(Product product) {
        // Validation
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            System.err.println("Product name cannot be empty");
            return false;
        }
        
        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            System.err.println("Product category cannot be empty");
            return false;
        }
        
        return productDAO.insert(product);
    }
    
    /**
     * Update existing product
     */
    public boolean updateProduct(Product product) {
        return productDAO.update(product);
    }
    
    /**
     * Delete product
     */
    public boolean deleteProduct(int productId) {
        return productDAO.delete(productId);
    }
}

