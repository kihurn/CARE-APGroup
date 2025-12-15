package com.care.controller.user;

import com.care.model.Product;
import com.care.service.ProductService;
import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Product Selection View
 * Loads real products from database
 */
public class SelectProductController {
    
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> productComboBox;
    @FXML private VBox productDetailsBox;
    @FXML private Label productNameLabel;
    @FXML private Label productVersionLabel;
    @FXML private Button startChatBtn;
    
    private ViewFactory viewFactory;
    private ProductService productService;
    private Product selectedProduct;
    private Map<String, Product> productMap; // Maps product name to Product object
    
    public SelectProductController() {
        this.viewFactory = ViewFactory.getInstance();
        this.productService = new ProductService();
        this.productMap = new HashMap<>();
    }
    
    @FXML
    private void initialize() {
        System.out.println("Initializing SelectProductController...");
        
        // Load categories from database
        loadCategories();
        
        // Listen for product selection
        productComboBox.setOnAction(event -> handleProductSelected());
    }
    
    /**
     * Load categories from database
     */
    private void loadCategories() {
        try {
            List<String> categories = productService.getAllCategories();
            
            if (categories != null && !categories.isEmpty()) {
                categoryComboBox.getItems().clear();
                categoryComboBox.getItems().addAll(categories);
                System.out.println("✓ Loaded " + categories.size() + " categories");
            } else {
                System.out.println("⚠ No categories found in database");
                // Fallback to default categories
                categoryComboBox.getItems().addAll("Router", "Laptop", "Smart Device");
            }
        } catch (Exception e) {
            System.err.println("Error loading categories");
            e.printStackTrace();
            // Fallback
            categoryComboBox.getItems().addAll("Router", "Laptop", "Smart Device");
        }
    }
    
    @FXML
    private void handleCategoryChange() {
        String selectedCategory = categoryComboBox.getValue();
        
        if (selectedCategory != null) {
            productComboBox.setDisable(false);
            productComboBox.getItems().clear();
            productMap.clear();
            productDetailsBox.setVisible(false);
            startChatBtn.setDisable(true);
            
            // Load products for selected category from database
            try {
                List<Product> products = productService.getProductsByCategory(selectedCategory);
                
                if (products != null && !products.isEmpty()) {
                    for (Product product : products) {
                        String displayName = product.getName() + " (" + product.getModelVersion() + ")";
                        productComboBox.getItems().add(displayName);
                        productMap.put(displayName, product);
                    }
                    System.out.println("✓ Loaded " + products.size() + " products for category: " + selectedCategory);
                } else {
                    System.out.println("⚠ No products found for category: " + selectedCategory);
                }
            } catch (Exception e) {
                System.err.println("Error loading products for category: " + selectedCategory);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handle product selection from dropdown
     */
    private void handleProductSelected() {
        String productKey = productComboBox.getValue();
        
        if (productKey != null && productMap.containsKey(productKey)) {
            selectedProduct = productMap.get(productKey);
            
            // Show product details
            productDetailsBox.setVisible(true);
            productNameLabel.setText(selectedProduct.getName());
            productVersionLabel.setText("Version: " + selectedProduct.getModelVersion() + 
                                       " | Category: " + selectedProduct.getCategory());
            startChatBtn.setDisable(false);
            
            System.out.println("✓ Selected product: " + selectedProduct.getName());
        }
    }
    
    @FXML
    private void handleStartChat() {
        if (selectedProduct != null) {
            System.out.println("Starting chat for product: " + selectedProduct.getName() + 
                             " (ID: " + selectedProduct.getProductId() + ")");
            
            // Store selected product in session for chat
            SessionManager.getInstance().setSelectedProduct(selectedProduct);
            
            // Navigate to chat area
            viewFactory.setUserSelectedMenuItem("ChatArea");
        } else {
            System.err.println("No product selected");
        }
    }
}

