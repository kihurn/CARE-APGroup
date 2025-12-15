package com.care.controller.user;

import com.care.model.User;
import com.care.service.UserService;
import com.care.util.PasswordUtil;
import com.care.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;

/**
 * Controller for User Profile & Settings Page
 */
public class UserProfileController {
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField licenseKeyField;
    @FXML private Button saveProfileBtn;
    @FXML private Label profileStatusLabel;
    
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordBtn;
    @FXML private Label passwordStatusLabel;
    
    @FXML private ComboBox<String> languageComboBox;
    @FXML private CheckBox enable2FACheckBox;
    @FXML private Button savePreferencesBtn;
    @FXML private Label preferencesStatusLabel;
    
    @FXML private Label roleLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedAtLabel;
    
    private SessionManager sessionManager;
    private UserService userService;
    private User currentUser;
    
    public UserProfileController() {
        this.sessionManager = SessionManager.getInstance();
        this.userService = new UserService();
    }
    
    @FXML
    private void initialize() {
        System.out.println("Initializing UserProfileController...");
        
        // Setup language options
        languageComboBox.getItems().addAll(
            "English (en)",
            "Spanish (es)",
            "French (fr)",
            "German (de)",
            "Chinese (zh)",
            "Japanese (ja)"
        );
        
        // Load current user data
        loadUserData();
    }
    
    /**
     * Load current user's data into the form fields
     */
    private void loadUserData() {
        if (!sessionManager.isLoggedIn()) {
            showError(profileStatusLabel, "No user logged in");
            return;
        }
        
        try {
            // Get fresh user data from database
            int userId = sessionManager.getCurrentUser().getUserId();
            currentUser = userService.getUserById(userId);
            
            if (currentUser == null) {
                showError(profileStatusLabel, "Failed to load user data");
                return;
            }
            
            // Populate profile fields
            nameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
            licenseKeyField.setText(currentUser.getLicenseKey() != null ? currentUser.getLicenseKey() : "N/A");
            
            // Populate preferences
            String preferredLang = currentUser.getPreferredLanguage();
            if (preferredLang != null) {
                // Map language code to display name
                switch (preferredLang) {
                    case "en": languageComboBox.setValue("English (en)"); break;
                    case "es": languageComboBox.setValue("Spanish (es)"); break;
                    case "fr": languageComboBox.setValue("French (fr)"); break;
                    case "de": languageComboBox.setValue("German (de)"); break;
                    case "zh": languageComboBox.setValue("Chinese (zh)"); break;
                    case "ja": languageComboBox.setValue("Japanese (ja)"); break;
                    default: languageComboBox.setValue("English (en)");
                }
            } else {
                languageComboBox.setValue("English (en)");
            }
            
            enable2FACheckBox.setSelected(currentUser.is2faEnabled());
            
            // Populate account info
            roleLabel.setText(currentUser.getRole());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            if (currentUser.getCreatedAt() != null) {
                createdAtLabel.setText(currentUser.getCreatedAt().format(formatter));
            }
            if (currentUser.getUpdatedAt() != null) {
                updatedAtLabel.setText(currentUser.getUpdatedAt().format(formatter));
            }
            
            System.out.println("✓ User data loaded successfully");
            
        } catch (Exception e) {
            System.err.println("Error loading user data: " + e.getMessage());
            e.printStackTrace();
            showError(profileStatusLabel, "Error loading user data");
        }
    }
    
    /**
     * Handle Save Profile button click
     */
    @FXML
    private void handleSaveProfile() {
        System.out.println("Saving profile changes...");
        
        String newName = nameField.getText().trim();
        
        // Validate
        if (newName.isEmpty()) {
            showError(profileStatusLabel, "Name cannot be empty");
            return;
        }
        
        try {
            // Update user object
            currentUser.setName(newName);
            
            // Save to database
            boolean success = userService.updateUser(currentUser);
            
            if (success) {
                // Update session manager with new data
                sessionManager.getCurrentUser().setName(newName);
                
                showSuccess(profileStatusLabel, "✅ Profile updated successfully!");
                
                System.out.println("✓ Profile updated successfully");
            } else {
                showError(profileStatusLabel, "❌ Failed to update profile");
            }
            
        } catch (Exception e) {
            System.err.println("Error saving profile: " + e.getMessage());
            e.printStackTrace();
            showError(profileStatusLabel, "❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Handle Change Password button click
     */
    @FXML
    private void handleChangePassword() {
        System.out.println("Changing password...");
        
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError(passwordStatusLabel, "All password fields are required");
            return;
        }
        
        if (newPassword.length() < 6) {
            showError(passwordStatusLabel, "New password must be at least 6 characters");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError(passwordStatusLabel, "New passwords do not match");
            return;
        }
        
        try {
            // Verify current password
            String storedPasswordHash = currentUser.getPasswordHash();
            boolean isCurrentPasswordValid;
            
            if (PasswordUtil.isBCryptHash(storedPasswordHash)) {
                isCurrentPasswordValid = PasswordUtil.verifyPassword(currentPassword, storedPasswordHash);
            } else {
                // Backward compatibility: plain text comparison
                isCurrentPasswordValid = currentPassword.equals(storedPasswordHash);
            }
            
            if (!isCurrentPasswordValid) {
                showError(passwordStatusLabel, "❌ Current password is incorrect");
                return;
            }
            
            // Hash new password
            String newPasswordHash = PasswordUtil.hashPassword(newPassword);
            currentUser.setPasswordHash(newPasswordHash);
            
            // Save to database
            boolean success = userService.updateUser(currentUser);
            
            if (success) {
                showSuccess(passwordStatusLabel, "✅ Password changed successfully!");
                
                // Clear password fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                
                System.out.println("✓ Password changed successfully");
            } else {
                showError(passwordStatusLabel, "❌ Failed to change password");
            }
            
        } catch (Exception e) {
            System.err.println("Error changing password: " + e.getMessage());
            e.printStackTrace();
            showError(passwordStatusLabel, "❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Handle Save Preferences button click
     */
    @FXML
    private void handleSavePreferences() {
        System.out.println("Saving preferences...");
        
        try {
            // Extract language code from selection
            String selectedLanguage = languageComboBox.getValue();
            String languageCode = "en"; // Default
            
            if (selectedLanguage != null) {
                if (selectedLanguage.contains("(en)")) languageCode = "en";
                else if (selectedLanguage.contains("(es)")) languageCode = "es";
                else if (selectedLanguage.contains("(fr)")) languageCode = "fr";
                else if (selectedLanguage.contains("(de)")) languageCode = "de";
                else if (selectedLanguage.contains("(zh)")) languageCode = "zh";
                else if (selectedLanguage.contains("(ja)")) languageCode = "ja";
            }
            
            // Update user object
            currentUser.setPreferredLanguage(languageCode);
            currentUser.setIs2faEnabled(enable2FACheckBox.isSelected());
            
            // Save to database
            boolean success = userService.updateUser(currentUser);
            
            if (success) {
                showSuccess(preferencesStatusLabel, "✅ Preferences saved successfully!");
                System.out.println("✓ Preferences saved successfully");
            } else {
                showError(preferencesStatusLabel, "❌ Failed to save preferences");
            }
            
        } catch (Exception e) {
            System.err.println("Error saving preferences: " + e.getMessage());
            e.printStackTrace();
            showError(preferencesStatusLabel, "❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Show error message on a label
     */
    private void showError(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        label.setVisible(true);
        label.setManaged(true);
        
        // Auto-hide after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
                    label.setText("");
                    label.setVisible(false);
                    label.setManaged(false);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Show success message on a label
     */
    private void showSuccess(Label label, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        label.setVisible(true);
        label.setManaged(true);
        
        // Auto-hide after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    label.setText("");
                    label.setVisible(false);
                    label.setManaged(false);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

