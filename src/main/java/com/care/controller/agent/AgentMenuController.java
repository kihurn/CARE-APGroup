package com.care.controller.agent;

import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

/**
 * Controller for Agent Menu (Sidebar Navigation)
 */
public class AgentMenuController {
    
    @FXML private Text agentNameText;
    @FXML private Text agentRoleText;
    @FXML private Button dashboardBtn;
    @FXML private Button logoutBtn;
    
    private SessionManager sessionManager;
    private ViewFactory viewFactory;
    
    public AgentMenuController() {
        this.sessionManager = SessionManager.getInstance();
        this.viewFactory = ViewFactory.getInstance();
    }
    
    @FXML
    private void initialize() {
        // Set agent info
        if (sessionManager.isLoggedIn()) {
            agentNameText.setText(sessionManager.getCurrentUser().getName());
            agentRoleText.setText(sessionManager.getCurrentUser().getRole());
        }
    }
    
    @FXML
    private void handleDashboard() {
        System.out.println("Dashboard clicked");
        viewFactory.setAgentSelectedMenuItem("AgentOverview");
    }
    
    @FXML
    private void handleLogout() {
        System.out.println("Logout clicked");
        sessionManager.logout();
        viewFactory.showLoginWindow();
    }
}

