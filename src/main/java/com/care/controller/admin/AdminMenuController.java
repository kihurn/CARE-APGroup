package com.care.controller.admin;

import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

/**
 * Controller for Admin Menu (Sidebar Navigation)
 */
public class AdminMenuController {
    
    @FXML private Text adminNameText;
    @FXML private Text adminRoleText;
    @FXML private Button dashboardBtn;
    @FXML private Button manageUsersBtn;
    @FXML private Button manageProductsBtn;
    @FXML private Button knowledgeBaseBtn;
    @FXML private Button ticketsBtn;
    @FXML private Button logoutBtn;
    
    private SessionManager sessionManager;
    private ViewFactory viewFactory;
    
    public AdminMenuController() {
        this.sessionManager = SessionManager.getInstance();
        this.viewFactory = ViewFactory.getInstance();
    }
    
    @FXML
    private void initialize() {
        // Set admin info
        if (sessionManager.isLoggedIn()) {
            adminNameText.setText(sessionManager.getCurrentUser().getName());
            adminRoleText.setText(sessionManager.getCurrentUser().getRole());
        }
    }
    
    @FXML
    private void handleDashboard() {
        System.out.println("Dashboard clicked");
        viewFactory.setAdminSelectedMenuItem("AdminOverview");
    }
    
    @FXML
    private void handleManageUsers() {
        System.out.println("Manage Users clicked");
        viewFactory.setAdminSelectedMenuItem("AdminUsers");
    }
    
    @FXML
    private void handleManageProducts() {
        System.out.println("Manage Products clicked");
        viewFactory.setAdminSelectedMenuItem("AdminProducts");
    }
    
    @FXML
    private void handleKnowledgeBase() {
        System.out.println("Knowledge Base clicked");
        viewFactory.setAdminSelectedMenuItem("AdminKB");
    }
    
    @FXML
    private void handleTickets() {
        System.out.println("View Tickets clicked");
        viewFactory.setAdminSelectedMenuItem("AdminTickets");
    }
    
    @FXML
    private void handleLogout() {
        System.out.println("Logout clicked");
        sessionManager.logout();
        viewFactory.showLoginWindow();
    }
}

