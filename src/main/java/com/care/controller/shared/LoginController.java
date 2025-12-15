package com.care.controller.shared;

import com.care.model.User;
import com.care.service.UserService;
import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for Login View
 * Handles user authentication
 */
public class LoginController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    
    private UserService userService;
    private ViewFactory viewFactory;
    private SessionManager sessionManager;
    
    public LoginController() {
        this.userService = new UserService();
        this.viewFactory = ViewFactory.getInstance();
        this.sessionManager = SessionManager.getInstance();
    }
    
    @FXML
    private void initialize() {
        // Add Enter key handler for password field
        passwordField.setOnAction(event -> handleLogin());
    }
    
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }
        
        // Authenticate user
        User user = userService.authenticate(email, password);
        
        if (user != null) {
            // Set session
            sessionManager.setCurrentUser(user);
            
            // Navigate to appropriate dashboard
            viewFactory.navigateToDashboard(user.getRole());
            
            System.out.println("Login successful: " + user.getName() + " (" + user.getRole() + ")");
        } else {
            showError("Invalid email or password");
        }
    }
    
    @FXML
    private void handleRegister() {
        viewFactory.showRegisterWindow();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}

