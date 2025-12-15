package com.care.controller.admin;

import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

/**
 * Controller for Admin Dashboard (Mother Container)
 * Implements dynamic content loading based on menu selection
 */
public class AdminDashboardController {
    
    @FXML private StackPane centerContentPane;
    
    private SessionManager sessionManager;
    private ViewFactory viewFactory;
    
    public AdminDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.viewFactory = ViewFactory.getInstance();
    }
    
    @FXML
    private void initialize() {
        System.out.println("AdminDashboard initialized");
        
        // Listen to menu selection changes
        viewFactory.adminSelectedMenuItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("Admin menu item selected: " + newValue);
                loadChildView(newValue);
            }
        });
        
        // Load default view (Dashboard Overview)
        loadChildView("AdminOverview");
    }
    
    /**
     * Dynamically load child view into center pane
     */
    private void loadChildView(String viewName) {
        try {
            Parent childView = viewFactory.loadAdminChildView(viewName);
            
            if (childView != null) {
                centerContentPane.getChildren().clear();
                centerContentPane.getChildren().add(childView);
                System.out.println("Loaded admin view: " + viewName);
            } else {
                System.err.println("Failed to load admin view: " + viewName);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading admin child view: " + viewName);
            e.printStackTrace();
        }
    }
    
    /**
     * Handle navigation to specific views (can be called programmatically)
     */
    public void showDashboard() {
        loadChildView("AdminOverview");
    }
    
    public void showUsers() {
        loadChildView("AdminUsers");
    }
    
    public void showProducts() {
        loadChildView("AdminProducts");
    }
    
    public void showKnowledgeBase() {
        loadChildView("AdminKB");
    }
}
