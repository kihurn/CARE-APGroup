package com.care.controller.admin;

import com.care.dao.KnowledgeBaseDAO;
import com.care.model.KnowledgeBase;
import com.care.model.Product;
import com.care.service.ProductService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * Controller for Admin Knowledge Base Management
 */
public class AdminKBController {
    
    @FXML private TableView<ProductKB> productsKBTable;
    @FXML private TableColumn<ProductKB, Integer> productIdCol;
    @FXML private TableColumn<ProductKB, String> productNameCol;
    @FXML private TableColumn<ProductKB, String> categoryCol;
    @FXML private TableColumn<ProductKB, String> manualStatusCol;
    @FXML private TableColumn<ProductKB, String> filePathCol;
    @FXML private TableColumn<ProductKB, Void> actionsCol;
    @FXML private Label totalProductsLabel;
    @FXML private Label manualsUploadedLabel;
    @FXML private Label manualsMissingLabel;
    
    private ProductService productService;
    private KnowledgeBaseDAO knowledgeBaseDAO;
    
    public AdminKBController() {
        this.productService = new ProductService();
        this.knowledgeBaseDAO = new KnowledgeBaseDAO();
    }
    
    @FXML
    private void initialize() {
        System.out.println("Initializing AdminKBController...");
        
        // 1. GOLDEN STANDARD: Use Unconstrained Policy to prevent squishing
        productsKBTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // 2. Setup Columns
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        manualStatusCol.setCellValueFactory(new PropertyValueFactory<>("manualStatus"));
        filePathCol.setCellValueFactory(new PropertyValueFactory<>("filePath"));
        
        // 3. Force Actions Column Width (Wide enough for "Update" + "Delete")
        actionsCol.setMinWidth(220);
        actionsCol.setPrefWidth(220);
        
        // 4. Setup Factories
        setupActionsColumn();
        setupStatusColumn(); // Optional: Make status green/red
        
        // 5. Load Data
        loadProductsWithManualStatus();
    }
    
    /**
     * Optional: Color code the status column
     */
    private void setupStatusColumn() {
        manualStatusCol.setCellFactory(column -> new TableCell<ProductKB, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Uploaded")) {
                        setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            // Create UI elements once per cell
            private final Button uploadBtn = new Button("📄 Upload");
            private final Button deleteBtn = new Button("🗑️ Delete");
            private final HBox container = new HBox(8, uploadBtn, deleteBtn);
            
            {
                // Styling - Override global CSS to keep buttons compact
                uploadBtn.getStyleClass().add("primary-button");
                uploadBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-min-width: 90px; -fx-max-width: 100px;");
                
                deleteBtn.getStyleClass().add("danger-button");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-min-width: 80px; -fx-max-width: 90px;");
                
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                
                // Event Handlers
                uploadBtn.setOnAction(e -> {
                    ProductKB item = getTableView().getItems().get(getIndex());
                    if (item != null) handleUploadManual(item);
                });
                
                deleteBtn.setOnAction(e -> {
                    ProductKB item = getTableView().getItems().get(getIndex());
                    if (item != null) handleDeleteManual(item);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    ProductKB productKB = getTableView().getItems().get(getIndex());
                    
                    // Logic: Toggle buttons based on status
                    if (productKB.isHasManual()) {
                        // Manual Exists -> Show Update & Delete
                        uploadBtn.setText("📝 Update");
                        uploadBtn.getStyleClass().removeAll("primary-button");
                        uploadBtn.getStyleClass().add("secondary-button"); // Outline style for update
                        
                        if (!container.getChildren().contains(deleteBtn)) {
                            container.getChildren().add(deleteBtn);
                        }
                    } else {
                        // No Manual -> Show Upload only
                        uploadBtn.setText("📄 Upload");
                        uploadBtn.getStyleClass().removeAll("secondary-button");
                        uploadBtn.getStyleClass().add("primary-button"); // Solid style for new upload
                        
                        container.getChildren().remove(deleteBtn);
                    }
                    
                    setGraphic(container);
                }
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing knowledge base...");
        loadProductsWithManualStatus();
    }
    
    private void loadProductsWithManualStatus() {
        try {
            List<Product> products = productService.getAllProducts();
            productsKBTable.getItems().clear();
            
            int uploadedCount = 0;
            int missingCount = 0;
            
            for (Product product : products) {
                KnowledgeBase kb = knowledgeBaseDAO.getByProductId(product.getProductId());
                boolean hasManual = (kb != null);
                String filePath = hasManual ? kb.getFilePath() : "N/A";
                
                // Wrap in ProductKB
                ProductKB productKB = new ProductKB(product, hasManual, filePath);
                productsKBTable.getItems().add(productKB);
                
                if (hasManual) uploadedCount++; else missingCount++;
            }
            
            totalProductsLabel.setText("Total Products: " + products.size());
            manualsUploadedLabel.setText("Manuals Uploaded: " + uploadedCount);
            manualsMissingLabel.setText("Manuals Missing: " + missingCount);
            
            // Auto-size columns after loading data
            autoResizeColumns();
            
        } catch (Exception e) {
            System.err.println("Error loading products");
            e.printStackTrace();
        }
    }
    
    private void handleUploadManual(ProductKB productKB) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Manual PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File selectedFile = fileChooser.showOpenDialog(productsKBTable.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                boolean success;
                if (productKB.isHasManual()) {
                    success = knowledgeBaseDAO.updateManual(productKB.getProductId(), selectedFile);
                } else {
                    success = knowledgeBaseDAO.createWithFile(
                        productKB.getProductId(),
                        productKB.getProductName() + " Manual",
                        selectedFile
                    );
                }
                
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Manual Uploaded");
                    alert.setContentText("The manual for " + productKB.getProductName() + " has been processed.");
                    alert.showAndWait();
                    loadProductsWithManualStatus(); 
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void handleDeleteManual(ProductKB productKB) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Manual");
        confirmAlert.setHeaderText("Delete manual for " + productKB.getProductName() + "?");
        confirmAlert.setContentText("This will remove the manual from the AI knowledge base.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (knowledgeBaseDAO.delete(productKB.getProductId())) {
                    loadProductsWithManualStatus();
                }
            }
        });
    }

    /**
     * Helper: Auto-size columns based on content
     */
    private void autoResizeColumns() {
        // 1. Force fixed Action column
        actionsCol.setPrefWidth(220); 
        productIdCol.setPrefWidth(50);
        
        // 2. Auto-fit other text columns
        TableColumn<?, ?>[] cols = {productNameCol, categoryCol, manualStatusCol, filePathCol};
        
        for (TableColumn<?, ?> column : cols) {
            Text t = new Text();
            double max = column.getText() != null ? column.getText().length() * 10.0 : 80;
            
            int rows = Math.min(50, productsKBTable.getItems().size());
            for (int i = 0; i < rows; i++) {
                if (column.getCellData(i) != null) {
                    t.setText(column.getCellData(i).toString());
                    double calc = t.getLayoutBounds().getWidth();
                    if (calc > max) max = calc;
                }
            }
            column.setPrefWidth(max + 30); // Add padding
        }
    }

    // ==========================================
    // INNER CLASS: Wrapper for Table Display
    // ==========================================
    public static class ProductKB {
        private final int productId;
        private final String productName;
        private final String category;
        private final boolean hasManual;
        private final String filePath;
        private final String manualStatus;

        public ProductKB(Product p, boolean hasManual, String filePath) {
            this.productId = p.getProductId();
            this.productName = p.getName();
            this.category = p.getCategory();
            this.hasManual = hasManual;
            this.filePath = filePath;
            this.manualStatus = hasManual ? "Uploaded" : "Missing";
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCategory() { return category; }
        public boolean isHasManual() { return hasManual; }
        public String getFilePath() { return filePath; }
        public String getManualStatus() { return manualStatus; }
    }
}