package com.care.dao;

import com.care.model.Product;
import com.care.util.DatabaseDriver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Products table
 * Handles all database operations related to products
 */
public class ProductDAO {
    
    private Connection connection;
    
    public ProductDAO() {
        this.connection = DatabaseDriver.getInstance().getConnection();
    }
    
    /**
     * Get all products from database
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            
            System.out.println("Loaded " + products.size() + " products from database");
        } catch (SQLException e) {
            System.err.println("Error loading products");
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * Get product by ID
     */
    public Product getById(int productId) {
        String query = "SELECT * FROM products WHERE product_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error loading product: " + productId);
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE category = ? ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading products by category");
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * Get distinct categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT DISTINCT category FROM products ORDER BY category";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
            
            System.out.println("Loaded " + categories.size() + " categories from database");
        } catch (SQLException e) {
            System.err.println("Error loading categories");
            e.printStackTrace();
        }
        
        return categories;
    }
    
    /**
     * Get product by ID
     */
    public Product findById(int productId) {
        String query = "SELECT * FROM products WHERE product_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding product by ID");
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Insert new product
     */
    public boolean insert(Product product) {
        String query = "INSERT INTO products (name, model_version, category) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getModelVersion());
            stmt.setString(3, product.getCategory());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting product");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create product with manual (SQLite compatible)
     * Creates product first, then adds manual if provided
     * 
     * @param product Product to create
     * @param manualFile PDF file of the manual (can be null)
     * @return The new product ID, or -1 if failed
     */
    public int createProductWithManual(Product product, java.io.File manualFile) {
        String insertProductQuery = "INSERT INTO products (name, model_version, category) VALUES (?, ?, ?)";
        String getLastIdQuery = "SELECT last_insert_rowid() as id";
        
        try {
            // Ensure auto-commit is enabled
            boolean originalAutoCommit = connection.getAutoCommit();
            if (!originalAutoCommit) {
                connection.setAutoCommit(true);
                System.out.println("⚠ Auto-commit was OFF, enabled it");
            }
            
            // Insert product
            PreparedStatement productStmt = connection.prepareStatement(insertProductQuery);
            productStmt.setString(1, product.getName());
            productStmt.setString(2, product.getModelVersion());
            productStmt.setString(3, product.getCategory());
            
            int rowsAffected = productStmt.executeUpdate();
            productStmt.close();
            
            if (rowsAffected > 0) {
                // Get the last inserted ID using SQLite's last_insert_rowid()
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(getLastIdQuery);
                
                if (rs.next()) {
                    int productId = rs.getInt("id");
                    product.setProductId(productId);
                    rs.close();
                    stmt.close();
                    
                    System.out.println("✓ Product created with ID: " + productId + " (committed to database)");
                    
                    // If manual file provided, create KB entry
                    if (manualFile != null) {
                        try {
                            KnowledgeBaseDAO kbDAO = new KnowledgeBaseDAO();
                            boolean kbCreated = kbDAO.createWithFile(productId, 
                                                                     product.getName() + " Manual", 
                                                                     manualFile);
                            
                            if (kbCreated) {
                                System.out.println("✓ Manual uploaded for product ID: " + productId);
                            } else {
                                System.err.println("⚠ Product created but manual upload failed");
                            }
                        } catch (Exception e) {
                            System.err.println("⚠ Product created but error uploading manual: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    return productId;
                }
            }
            
            return -1;
            
        } catch (SQLException e) {
            System.err.println("Error creating product");
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Update existing product
     */
    public boolean update(Product product) {
        String query = "UPDATE products SET name = ?, model_version = ?, category = ? WHERE product_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getModelVersion());
            stmt.setString(3, product.getCategory());
            stmt.setInt(4, product.getProductId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating product");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete product (also deletes associated knowledge base entry)
     */
    public boolean delete(int productId) {
        try {
            // First, delete the knowledge base entry (includes file deletion)
            KnowledgeBaseDAO kbDAO = new KnowledgeBaseDAO();
            kbDAO.delete(productId); // This also deletes the PDF file
            
            // Then delete the product
            String query = "DELETE FROM products WHERE product_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, productId);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            if (rowsAffected > 0) {
                System.out.println("✓ Product deleted (ID: " + productId + ")");
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error deleting product");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Map ResultSet to Product object
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setName(rs.getString("name"));
        product.setModelVersion(rs.getString("model_version"));
        product.setCategory(rs.getString("category"));
        return product;
    }
}

