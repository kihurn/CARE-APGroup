package com.care.controller.agent;

import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

/**
 * Controller for Agent Dashboard (Mother Container)
 * Implements dynamic content loading based on menu selection
 */
public class AgentDashboardController {
    
    @FXML private StackPane centerContentPane;
    
    private SessionManager sessionManager;
    private ViewFactory viewFactory;
    
    public AgentDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.viewFactory = ViewFactory.getInstance();
    }
    
    @FXML
    private void initialize() {
        System.out.println("AgentDashboard initialized");
        
        // Listen to menu selection changes
        viewFactory.agentSelectedMenuItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("Agent menu item selected: " + newValue);
                loadChildView(newValue);
            }
        });
        
        // Load default view (Agent Overview)
        loadChildView("AgentOverview");
    }
    
    /**
     * Dynamically load child view into center pane
     */
    private void loadChildView(String viewName) {
        try {
            Parent childView = viewFactory.loadAgentChildView(viewName);
            
            if (childView != null) {
                centerContentPane.getChildren().clear();
                centerContentPane.getChildren().add(childView);
                System.out.println("Loaded agent view: " + viewName);
            } else {
                System.err.println("Failed to load agent view: " + viewName);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading agent child view: " + viewName);
            e.printStackTrace();
        }
    }
    
    /**
     * Handle navigation to specific views (can be called programmatically)
     */
    public void showDashboard() {
        loadChildView("AgentOverview");
    }
}
