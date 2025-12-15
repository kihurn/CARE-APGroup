package com.care.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Singleton class to handle view navigation and scene switching
 * Centralizes FXML loading and window management
 * Implements Observer pattern for dynamic content loading
 */
public class ViewFactory {
    private static ViewFactory instance;
    private Stage primaryStage;
    
    // Navigation properties for dynamic content loading
    private final ObjectProperty<String> userSelectedMenuItem = new SimpleObjectProperty<>();
    private final ObjectProperty<String> adminSelectedMenuItem = new SimpleObjectProperty<>();
    private final ObjectProperty<String> agentSelectedMenuItem = new SimpleObjectProperty<>();
    
    // View paths
    private static final String LOGIN_VIEW = "/com/care/view/shared/Login.fxml";
    private static final String REGISTER_VIEW = "/com/care/view/shared/Register.fxml";
    private static final String USER_DASHBOARD_VIEW = "/com/care/view/user/UserDashboard.fxml";
    private static final String ADMIN_DASHBOARD_VIEW = "/com/care/view/admin/AdminDashboard.fxml";
    private static final String AGENT_DASHBOARD_VIEW = "/com/care/view/agent/AgentDashboard.fxml";
    
    // Stylesheet
    private static final String MAIN_CSS = "/com/care/styles/main.css";
    
    /**
     * Private constructor to prevent instantiation
     */
    private ViewFactory() {
    }
    
    /**
     * Get the singleton instance of ViewFactory
     * 
     * @return ViewFactory instance
     */
    public static synchronized ViewFactory getInstance() {
        if (instance == null) {
            instance = new ViewFactory();
        }
        return instance;
    }
    
    /**
     * Set the primary stage
     * 
     * @param stage Primary stage from App
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    /**
     * Get the primary stage
     * 
     * @return Primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Load and display the Login window
     */
    public void showLoginWindow() {
        loadView(LOGIN_VIEW, "CARE - Login", 1000, 700);
    }
    
    /**
     * Load and display the Register window
     */
    public void showRegisterWindow() {
        loadView(REGISTER_VIEW, "CARE - Register", 1000, 800);
    }
    
    /**
     * Load and display the User Dashboard
     */
    public void showUserDashboard() {
        loadView(USER_DASHBOARD_VIEW, "CARE - User Dashboard", 1200, 800);
    }
    
    /**
     * Load and display the Admin Dashboard
     */
    public void showAdminDashboard() {
        loadView(ADMIN_DASHBOARD_VIEW, "CARE - Admin Dashboard", 1400, 900);
    }
    
    /**
     * Load and display the Agent Dashboard
     */
    public void showAgentDashboard() {
        loadView(AGENT_DASHBOARD_VIEW, "CARE - Agent Dashboard", 1300, 850);
    }
    
    /**
     * Generic method to load an FXML view and set it as the scene
     * 
     * @param fxmlPath Path to FXML file
     * @param title Window title
     * @param width Window width
     * @param height Window height
     */
    private void loadView(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, width, height);
            
            // Apply CSS stylesheet
            String css = getClass().getResource(MAIN_CSS).toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("FXML file not found: " + fxmlPath);
            e.printStackTrace();
        }
    }
    
    /**
     * Navigate to the appropriate dashboard based on user role
     * 
     * @param role User role (USER, ADMIN, AGENT)
     */
    public void navigateToDashboard(String role) {
        switch (role.toUpperCase()) {
            case "ADMIN":
                showAdminDashboard();
                break;
            case "AGENT":
                showAgentDashboard();
                break;
            case "USER":
            default:
                showUserDashboard();
                break;
        }
    }
    
    /**
     * Close the current window
     */
    public void closeWindow() {
        if (primaryStage != null) {
            primaryStage.close();
        }
    }
    
    // ============================================
    // Navigation Property Getters/Setters
    // ============================================
    
    /**
     * Get the user selected menu item property
     * Dashboards can listen to this property to load content dynamically
     */
    public ObjectProperty<String> userSelectedMenuItemProperty() {
        return userSelectedMenuItem;
    }
    
    public String getUserSelectedMenuItem() {
        return userSelectedMenuItem.get();
    }
    
    public void setUserSelectedMenuItem(String menuItem) {
        this.userSelectedMenuItem.set(menuItem);
    }
    
    /**
     * Get the admin selected menu item property
     */
    public ObjectProperty<String> adminSelectedMenuItemProperty() {
        return adminSelectedMenuItem;
    }
    
    public String getAdminSelectedMenuItem() {
        return adminSelectedMenuItem.get();
    }
    
    public void setAdminSelectedMenuItem(String menuItem) {
        this.adminSelectedMenuItem.set(menuItem);
    }
    
    /**
     * Get the agent selected menu item property
     */
    public ObjectProperty<String> agentSelectedMenuItemProperty() {
        return agentSelectedMenuItem;
    }
    
    public String getAgentSelectedMenuItem() {
        return agentSelectedMenuItem.get();
    }
    
    public void setAgentSelectedMenuItem(String menuItem) {
        this.agentSelectedMenuItem.set(menuItem);
    }
    
    // ============================================
    // Child View Loaders (User Module)
    // ============================================
    
    /**
     * Load a child view and return the Parent node
     * Used for dynamic content loading in dashboard center area
     */
    public Parent loadUserChildView(String viewName) {
        String fxmlPath = "/com/care/view/user/" + viewName + ".fxml";
        return loadFXML(fxmlPath);
    }
    
    public Parent loadAdminChildView(String viewName) {
        String fxmlPath = "/com/care/view/admin/" + viewName + ".fxml";
        return loadFXML(fxmlPath);
    }
    
    public Parent loadAgentChildView(String viewName) {
        String fxmlPath = "/com/care/view/agent/" + viewName + ".fxml";
        return loadFXML(fxmlPath);
    }
    
    /**
     * Generic FXML loader that returns Parent node
     */
    private Parent loadFXML(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            return loader.load();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }
}

