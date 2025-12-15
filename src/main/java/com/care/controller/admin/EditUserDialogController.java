package com.care.controller.admin;

import com.care.model.User;
import com.care.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for Edit User Dialog
 */
public class EditUserDialogController {
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField licenseKeyField;
    @FXML private CheckBox enable2FACheckBox;
    @FXML private Label errorLabel;
    
    private UserService userService;
    private User currentUser;
    private boolean userUpdated = false;
    
    public EditUserDialogController() {
        this.userService = new UserService();
    }
    
    @FXML
    private void initialize() {
        // Setup role options
        roleComboBox.getItems().addAll("USER", "ADMIN", "AGENT");
    }
    
    /**
     * Set the user to edit
     */
    public void setUser(User user) {
        this.currentUser = user;
        
        // Populate fields with current user data
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        roleComboBox.setValue(user.getRole());
        licenseKeyField.setText(user.getLicenseKey() != null ? user.getLicenseKey() : "No License Key");
        enable2FACheckBox.setSelected(user.is2faEnabled());
    }
    
    @FXML
    private void handleUpdate() {
        // Validate input
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();
        boolean is2FAEnabled = enable2FACheckBox.isSelected();
        
        if (name.isEmpty() || email.isEmpty() || role == null) {
            showError("Please fill in all required fields");
            return;
        }
        
        if (!email.contains("@")) {
            showError("Please enter a valid email address");
            return;
        }
        
        try {
            // Update user object
            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setRole(role);
            currentUser.setIs2faEnabled(is2FAEnabled);
            
            // Update in database
            boolean success = userService.updateUser(currentUser);
            
            if (success) {
                System.out.println("✓ User updated successfully: " + name);
                userUpdated = true;
                closeDialog();
            } else {
                showError("Failed to update user. Please try again.");
            }
            
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
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
        errorLabel.setManaged(true);
    }
    
    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
    
    public boolean isUserUpdated() {
        return userUpdated;
    }
}

