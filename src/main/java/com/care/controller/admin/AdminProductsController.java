package com.care.controller.admin;

import com.care.model.Product;
import com.care.service.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for Admin Products Management
 */
public class AdminProductsController {
    
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private TextField searchField;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> productIdCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, String> versionCol;
    @FXML private TableColumn<Product, String> categoryCol;
    @FXML private TableColumn<Product, String> createdAtCol;
    @FXML private TableColumn<Product, Void> actionsCol;
    @FXML private Label totalProductsLabel;
    
    private ProductService productService;
    
    public AdminProductsController() {
        this.productService = new ProductService();
    }
    
    @FXML
    private void initialize() {
        System.out.println("Initializing AdminProductsController...");
        
        // Use unconstrained so we can control widths precisely (golden standard)
        productsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Setup category filter with database categories
        loadCategoryFilter();
        
        // Setup table columns
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        versionCol.setCellValueFactory(new PropertyValueFactory<>("modelVersion"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        // Setup actions column with delete button
        setupActionsColumn();
        
        // Load data
        loadProducts();
    }
    
    /**
     * Setup actions column with delete buttons
     */
    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button("🗑️ Delete");
            
            {
                deleteBtn.getStyleClass().add("danger-button");
                // Match golden standard: constrain button width via CSS
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-min-width: 70px; -fx-max-width: 80px;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    deleteBtn.setOnAction(e -> handleDeleteProduct(product));
                    setGraphic(deleteBtn);
                }
            }
        });
        
        // Give actions column a stable preferred width
        actionsCol.setPrefWidth(190);
    }
    
    /**
     * Load category filter from database
     */
    private void loadCategoryFilter() {
        try {
            List<String> categories = productService.getAllCategories();
            categoryFilterCombo.getItems().clear();
            categoryFilterCombo.getItems().add("All Categories");
            categoryFilterCombo.getItems().addAll(categories);
            categoryFilterCombo.setValue("All Categories");
        } catch (Exception e) {
            System.err.println("Error loading categories");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddProduct() {
        System.out.println("Opening Add Product dialog...");
        
        try {
            // Load the dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/care/view/admin/AddProductDialog.fxml"));
            Parent dialogRoot = loader.load();
            
            // Create dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Product");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(true);
            dialogStage.setMinHeight(750);
            
            Scene dialogScene = new Scene(dialogRoot, 600, 780);
            
            // Apply CSS
            String css = getClass().getResource("/com/care/styles/main.css").toExternalForm();
            dialogScene.getStylesheets().add(css);
            
            dialogStage.setScene(dialogScene);
            
            // Show dialog and wait for close
            dialogStage.showAndWait();
            
            // Check if product was created
            AddProductDialogController controller = loader.getController();
            if (controller.isProductCreated()) {
                System.out.println("✓ Product created, refreshing table...");
                loadProducts(); // Refresh table
            }
            
        } catch (Exception e) {
            System.err.println("Error opening Add Product dialog");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing products...");
        loadProducts();
    }
    
    /**
     * Handle delete product
     */
    private void handleDeleteProduct(Product product) {
        System.out.println("Delete product: " + product.getName());
        
        // Show confirmation dialog
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Product");
        confirmAlert.setHeaderText("Delete " + product.getName() + "?");
        confirmAlert.setContentText("This will also delete the associated manual and cannot be undone.\n\n" +
                                   "Any chat sessions or tickets related to this product will remain but show as 'Product Deleted'.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Delete from database
                    boolean success = productService.deleteProduct(product.getProductId());
                    
                    if (success) {
                        System.out.println("✓ Product deleted successfully");
                        
                        // Show success message
                        javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("Product Deleted");
                        successAlert.setContentText(product.getName() + " has been deleted successfully.");
                        successAlert.show();
                        
                        // Refresh table
                        loadProducts();
                    } else {
                        System.err.println("✗ Failed to delete product");
                        
                        // Show error message
                        javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("Delete Failed");
                        errorAlert.setContentText("Failed to delete " + product.getName() + ". Please try again.");
                        errorAlert.show();
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting product");
                    e.printStackTrace();
                    
                    javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Delete Failed");
                    errorAlert.setContentText("Error: " + e.getMessage());
                    errorAlert.show();
                }
            }
        });
    }
    
    @FXML
    private void handleFilterChange() {
        String filter = categoryFilterCombo.getValue();
        System.out.println("Filtering by: " + filter);
        
        try {
            productsTable.getItems().clear();
            
            if (filter == null || filter.equals("All Categories")) {
                List<Product> products = productService.getAllProducts();
                productsTable.getItems().addAll(products);
            } else {
                List<Product> products = productService.getProductsByCategory(filter);
                productsTable.getItems().addAll(products);
            }
            
            totalProductsLabel.setText("Total Products: " + productsTable.getItems().size());
            System.out.println("✓ Filtered to " + productsTable.getItems().size() + " products");
            autoResizeColumns();
        } catch (Exception e) {
            System.err.println("Error filtering products");
            e.printStackTrace();
        }
    }
    
    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            productsTable.getItems().clear();
            productsTable.getItems().addAll(products);
            totalProductsLabel.setText("Total Products: " + products.size());
            System.out.println("✓ Loaded " + products.size() + " products from database");
            autoResizeColumns();
        } catch (Exception e) {
            System.err.println("Error loading products");
            e.printStackTrace();
            totalProductsLabel.setText("Total Products: 0");
        }
    }
    
    /**
     * Auto-resize columns with a fixed preferred width for actions (golden standard)
     */
    private void autoResizeColumns() {
        // Keep actions column stable
        actionsCol.setPrefWidth(190);
        
        // Auto-fit other columns based on header/text
        for (TableColumn<?, ?> column : productsTable.getColumns()) {
            if (column == actionsCol) continue;
            double max = column.getText() != null ? column.getText().length() * 10 : 80;
            column.setPrefWidth(Math.max(90, max));
        }
    }
}

