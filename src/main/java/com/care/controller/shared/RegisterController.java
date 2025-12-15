package com.care.controller.shared;

import com.care.model.User;
import com.care.service.UserService;
import com.care.util.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for Register View
 * Handles new user registration
 */
public class RegisterController {
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField licenseKeyField;
    @FXML private Label errorLabel;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    
    private UserService userService;
    private ViewFactory viewFactory;
    
    public RegisterController() {
        this.userService = new UserService();
        this.viewFactory = ViewFactory.getInstance();
    }
    
    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String licenseKey = licenseKeyField.getText().trim();
        
        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all required fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }
        
        // Email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Please enter a valid email address");
            return;
        }
        
        // Create new user
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPasswordHash(password); // In production, hash this!
        newUser.setRole("USER");
        newUser.setLicenseKey(licenseKey.isEmpty() ? null : licenseKey);
        
        boolean success = userService.registerUser(newUser);
        
        if (success) {
            System.out.println("Registration successful for: " + email);
            viewFactory.showLoginWindow();
        } else {
            showError("Registration failed. Email may already be in use.");
        }
    }
    
    @FXML
    private void handleBack() {
        viewFactory.showLoginWindow();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}

