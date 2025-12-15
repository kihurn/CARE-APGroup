package com.care.controller.admin;

import com.care.model.User;
import com.care.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.UUID;

/**
 * Controller for Add User Dialog
 */
public class AddUserDialogController {
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField licenseKeyField;
    @FXML private Label errorLabel;
    @FXML private Button createButton;
    @FXML private Button cancelButton;
    
    private UserService userService;
    private boolean userCreated = false;
    
    public AddUserDialogController() {
        this.userService = new UserService();
    }
    
    @FXML
    private void initialize() {
        roleComboBox.getItems().setAll("USER", "ADMIN", "AGENT");
        roleComboBox.setValue("USER");
    }
    
    @FXML
    private void handleCreateUser() {
        // Hide previous error
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        // Validate inputs
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        String licenseKey = licenseKeyField.getText().trim();
        
        // Validation
        if (name.isEmpty()) {
            showError("Please enter a name.");
            return;
        }
        
        if (email.isEmpty()) {
            showError("Please enter an email address.");
            return;
        }
        
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter a password.");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        
        if (role == null || role.isEmpty()) {
            showError("Please select a role.");
            return;
        }
        
        // Generate license key if empty
        if (licenseKey.isEmpty()) {
            licenseKey = generateLicenseKey();
        }
        
        try {
            // Check if email already exists
            User existingUser = userService.getUserByEmail(email);
            if (existingUser != null) {
                showError("A user with this email already exists.");
                return;
            }
            
            // Create user
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPasswordHash(password); // Note: In production, this should be hashed!
            newUser.setRole(role);
            newUser.setLicenseKey(licenseKey);
            
            boolean success = userService.registerUser(newUser);
            
            if (success) {
                userCreated = true;
                showSuccess("User created successfully!");
                
                // Close dialog after a short delay
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        javafx.application.Platform.runLater(this::closeDialog);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                showError("Failed to create user. Please try again.");
            }
            
        } catch (Exception e) {
            System.err.println("Error creating user");
            e.printStackTrace();
            showError("Error: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void showSuccess(String message) {
        errorLabel.setText("✅ " + message);
        errorLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    private String generateLicenseKey() {
        // Generate a simple license key (format: XXXX-XXXX-XXXX)
        String uuid = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        return uuid.substring(0, 4) + "-" + uuid.substring(4, 8) + "-" + uuid.substring(8, 12);
    }
    
    public boolean isUserCreated() {
        return userCreated;
    }
}

