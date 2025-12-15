package com.care.controller.user;

import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

/**
 * Controller for User Dashboard (Mother Container)
 * Implements dynamic content loading based on menu selection
 */
public class UserDashboardController {
    
    @FXML private StackPane centerContentPane;
    
    private SessionManager sessionManager;
    private ViewFactory viewFactory;
    
    public UserDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.viewFactory = ViewFactory.getInstance();
    }
    
    @FXML
    private void initialize() {
        System.out.println("UserDashboard initialized");
        
        // Listen to menu selection changes
        viewFactory.userSelectedMenuItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("Menu item selected: " + newValue);
                loadChildView(newValue);
            }
        });
        
        // Load default view (Welcome/SelectProduct)
        loadChildView("SelectProduct");
    }
    
    /**
     * Dynamically load child view into center pane
     */
    private void loadChildView(String viewName) {
        try {
            Parent childView = viewFactory.loadUserChildView(viewName);
            
            if (childView != null) {
                centerContentPane.getChildren().clear();
                centerContentPane.getChildren().add(childView);
                System.out.println("Loaded view: " + viewName);
            } else {
                System.err.println("Failed to load view: " + viewName);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading child view: " + viewName);
            e.printStackTrace();
        }
    }
    
    /**
     * Handle navigation to specific views
     */
    public void showWelcome() {
        loadChildView("SelectProduct");
    }
    
    public void showHistory() {
        loadChildView("UserHistory");
    }
    
    public void showChat() {
        loadChildView("ChatArea");
    }
}
