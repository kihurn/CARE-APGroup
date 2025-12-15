package com.care.controller.admin;

import com.care.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text; // Added for text measurement
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

/**
 * Controller for Admin Users Management
 */
public class AdminUsersController {
    
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private TextField searchField;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, String> licenseKeyCol;
    @FXML private TableColumn<User, String> createdAtCol;
    @FXML private TableColumn<User, Void> actionsCol;
    @FXML private Label totalUsersLabel;
    @FXML private Label adminsCountLabel;
    @FXML private Label agentsCountLabel;
    @FXML private Label usersCountLabel;
    
    private com.care.service.UserService userService;
    private List<User> allUsers;
    
    public AdminUsersController() {
        this.userService = new com.care.service.UserService();
    }
    
    @FXML
    private void initialize() {
        // ============================================================
        // 1. CHANGE RESIZE POLICY
        // ============================================================
        // We use UNCONSTRAINED so the table can scroll horizontally if needed,
        // preventing the "Actions" buttons from being squished.
        usersTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // 2. Setup Filters
        roleFilterCombo.getItems().addAll("All Roles", "USER", "ADMIN", "AGENT");
        roleFilterCombo.setValue("All Roles");
        
        // 3. Setup Factories (Data Binding)
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        licenseKeyCol.setCellValueFactory(new PropertyValueFactory<>("licenseKey"));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        createdAtCol.setCellValueFactory(cell -> {
            if (cell.getValue().getCreatedAt() == null) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(cell.getValue().getCreatedAt().format(fmt));
        });
        
        // 4. Force Actions Column Width (So buttons are always visible)
        actionsCol.setMinWidth(190);
        actionsCol.setPrefWidth(190);
        
        setupActionsColumn();
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
        
        loadUsers();
    }
    
    private void setupActionsColumn() {
        actionsCol.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox container = new HBox(8, editBtn, deleteBtn);
                    
                    {
                        // 1. Style the EDIT Button
                        editBtn.getStyleClass().add("secondary-button");
                        // IMPORTANT: Force min-width to be small (overriding CSS)
                        editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-min-width: 60px; -fx-max-width: 70px;");
                        
                        // 2. Style the DELETE Button
                        deleteBtn.getStyleClass().add("danger-button");
                        // IMPORTANT: Force min-width to be small
                        deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-min-width: 70px; -fx-max-width: 80px;");
                        
                        // 3. Center them
                        container.setAlignment(Pos.CENTER);
                        
                        editBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleEditUser(user);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleDeleteUser(user);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(container);
                        }
                    }
                };
            }
        });
    }
    
    @FXML
    private void handleAddUser() {
        System.out.println("Add User clicked");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/care/view/admin/AddUserDialog.fxml"));
            Parent dialogRoot = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Add New User");
            dialogStage.setResizable(false);
            
            Scene dialogScene = new Scene(dialogRoot, 500, 550);
            dialogScene.getStylesheets().add(getClass().getResource("/com/care/styles/main.css").toExternalForm());
            dialogStage.setScene(dialogScene);
            
            dialogStage.showAndWait();
            
            AddUserDialogController controller = loader.getController();
            if (controller.isUserCreated()) {
                loadUsers();
            }
            
        } catch (IOException e) {
            System.err.println("Error loading Add User dialog");
            e.printStackTrace();
        }
    }
    
    private void handleEditUser(User user) {
        if (user == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/care/view/admin/EditUserDialog.fxml"));
            Parent dialogRoot = loader.load();
            
            EditUserDialogController controller = loader.getController();
            controller.setUser(user);
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Edit User - " + user.getName());
            dialogStage.setResizable(false);
            
            Scene dialogScene = new Scene(dialogRoot, 500, 450);
            dialogScene.getStylesheets().add(getClass().getResource("/com/care/styles/main.css").toExternalForm());
            dialogStage.setScene(dialogScene);
            
            dialogStage.showAndWait();
            
            if (controller.isUserUpdated()) {
                loadUsers();
            }
            
        } catch (IOException e) {
            System.err.println("Error loading Edit User dialog");
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open Edit User dialog");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void handleDeleteUser(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User: " + user.getName() + "?");
        confirmAlert.setContentText("Are you sure you want to delete this user?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (userService.deleteUser(user.getUserId())) {
                    loadUsers();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadUsers();
    }
    
    @FXML
    private void handleFilterChange() {
        applyFilters();
    }
    
    private void loadUsers() {
        try {
            allUsers = userService.getAllUsers();
            applyFilters(); // This adds items to the table
            
            // NEW: Auto-resize columns AFTER data is loaded
            autoResizeColumns();
            
            System.out.println("✓ Loaded " + allUsers.size() + " users from database");
        } catch (Exception e) {
            System.err.println("Error loading users");
            e.printStackTrace();
        }
    }
    
    private void applyFilters() {
        if (allUsers == null) return;
        
        String roleFilter = roleFilterCombo.getValue();
        String searchText = searchField.getText().toLowerCase().trim();
        
        List<User> filteredUsers = allUsers.stream()
            .filter(user -> {
                boolean matchesRole = roleFilter.equals("All Roles") || 
                                     user.getRole().equalsIgnoreCase(roleFilter);
                
                boolean matchesSearch = searchText.isEmpty() ||
                                       user.getName().toLowerCase().contains(searchText) ||
                                       user.getEmail().toLowerCase().contains(searchText);
                
                return matchesRole && matchesSearch;
            })
            .collect(Collectors.toList());
        
        usersTable.getItems().clear();
        usersTable.getItems().addAll(filteredUsers);
        updateStats();
    }
    
    private void updateStats() {
        int total = usersTable.getItems().size();
        int admins = 0;
        int agents = 0;
        int users = 0;
        
        for (User user : usersTable.getItems()) {
            switch (user.getRole().toUpperCase()) {
                case "ADMIN": admins++; break;
                case "AGENT": agents++; break;
                case "USER": users++; break;
            }
        }
        
        totalUsersLabel.setText("Total Users: " + total);
        adminsCountLabel.setText("Admins: " + admins);
        agentsCountLabel.setText("Agents: " + agents);
        usersCountLabel.setText("Users: " + users);
    }

    /**
     * NEW HELPER METHOD: Automatically adjusts column widths based on content.
     */
    private void autoResizeColumns() {
        // 1. Force fixed width for Actions and ID
        actionsCol.setPrefWidth(190);
        userIdCol.setPrefWidth(50);
        
        // 2. Auto-fit text columns based on content length
        TableColumn<?, ?>[] textColumns = {nameCol, emailCol, roleCol, licenseKeyCol, createdAtCol};

        for (TableColumn<?, ?> column : textColumns) {
            Text t = new Text();
            double max = column.getText().length() * 10.0; // Start with header width approximation
            
            // Scan first 50 rows to find longest text
            int rowsToScan = Math.min(usersTable.getItems().size(), 50);
            
            for (int i = 0; i < rowsToScan; i++) {
                if (column.getCellData(i) != null) {
                    t.setText(column.getCellData(i).toString());
                    // Calculate width of text
                    double calcwidth = t.getLayoutBounds().getWidth();
                    if (calcwidth > max) {
                        max = calcwidth;
                    }
                }
            }
            // Add padding (30px) and set width
            column.setPrefWidth(max + 30.0);
        }
    }
}