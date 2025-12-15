package com.care.dao;

import com.care.model.KnowledgeBase;
import com.care.util.DatabaseDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Knowledge Base table
 * Handles manual/PDF file storage and retrieval
 */
public class KnowledgeBaseDAO {
    
    private Connection connection;
    private static final String MANUALS_DIR = "manuals/";
    
    public KnowledgeBaseDAO() {
        this.connection = DatabaseDriver.getInstance().getConnection();
        // Create manuals directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(MANUALS_DIR));
        } catch (IOException e) {
            System.err.println("Error creating manuals directory");
            e.printStackTrace();
        }
    }
    
    /**
     * Get all knowledge base articles
     */
    public List<KnowledgeBase> getAll() {
        List<KnowledgeBase> articles = new ArrayList<>();
        String query = "SELECT * FROM knowledge_base ORDER BY title";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                articles.add(mapResultSetToKB(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading knowledge base articles");
            e.printStackTrace();
        }
        
        return articles;
    }
    
    /**
     * Get knowledge base articles for a specific product
     */
    public KnowledgeBase getByProductId(int productId) {
        String query = "SELECT * FROM knowledge_base WHERE product_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToKB(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error loading KB for product: " + productId);
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Check if a product has a manual
     */
    public boolean hasManual(int productId) {
        String query = "SELECT COUNT(*) FROM knowledge_base WHERE product_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking manual for product: " + productId);
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Create knowledge base entry with file upload
     */
    public boolean createWithFile(int productId, String title, File pdfFile) {
        // Save file to disk
        String savedFilePath = saveFile(pdfFile, productId);
        
        if (savedFilePath == null) {
            return false;
        }
        
        // Read file content (for AI/search later)
        String content = extractTextFromPDF(pdfFile);
        
        String query = "INSERT INTO knowledge_base (product_id, title, content, file_path) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            stmt.setString(2, title);
            stmt.setString(3, content);
            stmt.setString(4, savedFilePath);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("✓ Manual uploaded for product ID: " + productId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating knowledge base entry");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update/replace manual for a product
     */
    public boolean updateManual(int productId, File pdfFile) {
        // Delete old file if exists
        KnowledgeBase existing = getByProductId(productId);
        if (existing != null && existing.getFilePath() != null) {
            deleteFile(existing.getFilePath());
        }
        
        // Save new file
        String savedFilePath = saveFile(pdfFile, productId);
        if (savedFilePath == null) {
            return false;
        }
        
        // Read content
        String content = extractTextFromPDF(pdfFile);
        
        String query = "UPDATE knowledge_base SET content = ?, file_path = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, content);
            stmt.setString(2, savedFilePath);
            stmt.setInt(3, productId);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("✓ Manual updated for product ID: " + productId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating manual");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete knowledge base entry
     */
    public boolean delete(int productId) {
        // Delete file first
        KnowledgeBase kb = getByProductId(productId);
        if (kb != null && kb.getFilePath() != null) {
            deleteFile(kb.getFilePath());
        }
        
        String query = "DELETE FROM knowledge_base WHERE product_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting KB entry");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Save PDF file to disk
     */
    private String saveFile(File sourceFile, int productId) {
        try {
            String fileName = "product_" + productId + "_" + sourceFile.getName();
            Path targetPath = Paths.get(MANUALS_DIR + fileName);
            
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("✓ File saved: " + targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            System.err.println("Error saving file");
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Delete file from disk
     */
    private void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
            System.out.println("✓ File deleted: " + filePath);
        } catch (IOException e) {
            System.err.println("Error deleting file: " + filePath);
            e.printStackTrace();
        }
    }
    
    /**
     * Extract text from PDF using Apache PDFBox
     */
    private String extractTextFromPDF(File pdfFile) {
        try {
            System.out.println("Extracting text from PDF: " + pdfFile.getName());
            
            // Load PDF document
            org.apache.pdfbox.pdmodel.PDDocument document = 
                org.apache.pdfbox.pdmodel.PDDocument.load(pdfFile);
            
            // Extract text
            org.apache.pdfbox.text.PDFTextStripper stripper = 
                new org.apache.pdfbox.text.PDFTextStripper();
            String text = stripper.getText(document);
            
            // Close document
            document.close();
            
            // Limit text size to avoid database issues (first 50000 characters)
            if (text.length() > 50000) {
                text = text.substring(0, 50000) + "\n... [Content truncated]";
                System.out.println("⚠ PDF text truncated to 50,000 characters");
            }
            
            System.out.println("✓ Extracted " + text.length() + " characters from PDF");
            return text;
            
        } catch (Exception e) {
            System.err.println("Error extracting text from PDF: " + e.getMessage());
            e.printStackTrace();
            // Return basic info if extraction fails
            return "Manual file: " + pdfFile.getName() + "\n[Text extraction failed: " + e.getMessage() + "]";
        }
    }
    
    /**
     * Map ResultSet to KnowledgeBase object
     */
    private KnowledgeBase mapResultSetToKB(ResultSet rs) throws SQLException {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setKbId(rs.getInt("kb_id"));
        kb.setProductId(rs.getInt("product_id"));
        kb.setTitle(rs.getString("title"));
        kb.setContent(rs.getString("content"));
        kb.setFilePath(rs.getString("file_path"));
        return kb;
    }
}

