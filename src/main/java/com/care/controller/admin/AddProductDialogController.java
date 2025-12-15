package com.care.controller.admin;

import com.care.dao.ProductDAO;
import com.care.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Controller for Add Product Dialog
 * Handles product creation with optional manual upload
 */
public class AddProductDialogController {
    
    @FXML private TextField productNameField;
    @FXML private TextField modelVersionField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private VBox uploadZone;
    @FXML private HBox selectedFileBox;
    @FXML private Label selectedFileLabel;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private ProductDAO productDAO;
    private File selectedManualFile;
    private boolean productCreated = false;
    
    public AddProductDialogController() {
        this.productDAO = new ProductDAO();
    }
    
    @FXML
    private void initialize() {
        // Populate categories
        categoryCombo.getItems().addAll("Router", "Laptop", "Smart Device", "Other");
    }
    
    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Manual (PDF)");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        File file = fileChooser.showOpenDialog(uploadZone.getScene().getWindow());
        
        if (file != null) {
            selectedManualFile = file;
            selectedFileLabel.setText(file.getName());
            selectedFileBox.setVisible(true);
            System.out.println("✓ File selected: " + file.getName());
        }
    }
    
    @FXML
    private void handleClearFile() {
        selectedManualFile = null;
        selectedFileBox.setVisible(false);
        selectedFileLabel.setText("No file selected");
        System.out.println("File cleared");
    }
    
    @FXML
    private void handleSave() {
        String name = productNameField.getText().trim();
        String version = modelVersionField.getText().trim();
        String category = categoryCombo.getValue();
        
        // Validation
        if (name.isEmpty()) {
            showError("Product name is required");
            return;
        }
        
        if (version.isEmpty()) {
            showError("Model/version is required");
            return;
        }
        
        if (category == null || category.isEmpty()) {
            showError("Please select a category");
            return;
        }
        
        // Create product
        Product product = new Product();
        product.setName(name);
        product.setModelVersion(version);
        product.setCategory(category);
        
        try {
            int productId = productDAO.createProductWithManual(product, selectedManualFile);
            
            if (productId > 0) {
                productCreated = true;
                System.out.println("✓ Product created with ID: " + productId);
                closeDialog();
            } else {
                showError("Failed to create product. Please try again.");
            }
        } catch (Exception e) {
            System.err.println("Error creating product");
            e.printStackTrace();
            showError("Error: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Check if product was successfully created
     */
    public boolean isProductCreated() {
        return productCreated;
    }
}


