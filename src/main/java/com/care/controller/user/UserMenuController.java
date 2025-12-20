package com.care.controller.user;

import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

/**
 * Controller for User Menu (Sidebar Navigation)
 */
public class UserMenuController {
    
    @FXML private Text userNameText;
    @FXML private Text userRoleText;
    @FXML private Button newChatBtn;
    @FXML private Button historyBtn;
    // @FXML private Button knowledgeBaseBtn; // Removed from UI
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;
    
    private SessionManager sessionManager;
    private ViewFactory viewFactory;
    
    public UserMenuController() {
        this.sessionManager = SessionManager.getInstance();
        this.viewFactory = ViewFactory.getInstance();
    }
    
    @FXML
    private void initialize() {
        // Set user info
        if (sessionManager.isLoggedIn()) {
            userNameText.setText(sessionManager.getCurrentUser().getName());
            userRoleText.setText(sessionManager.getCurrentUser().getRole());
        }
    }
    
    @FXML
    private void handleNewChat() {
        System.out.println("New Chat clicked");
        // Clear any existing chat session when starting a new chat
        sessionManager.clearCurrentChatSession();
        viewFactory.setUserSelectedMenuItem("SelectProduct");
    }
    
    @FXML
    private void handleHistory() {
        System.out.println("History clicked");
        // Clear current chat session when viewing history
        // This ensures a clean state when continuing a different chat
        sessionManager.clearCurrentChatSession();
        viewFactory.setUserSelectedMenuItem("UserHistory");
    }
    
    /*
    @FXML
    private void handleKnowledgeBase() {
        System.out.println("Knowledge Base clicked");
        viewFactory.setUserSelectedMenuItem("KnowledgeBase");
    }
    */
    
    @FXML
    private void handleProfile() {
        System.out.println("Profile clicked");
        // Clear chat session when navigating away from chat
        sessionManager.clearCurrentChatSession();
        viewFactory.setUserSelectedMenuItem("Profile");
    }
    
    @FXML
    private void handleLogout() {
        System.out.println("Logout clicked");
        sessionManager.logout();
        viewFactory.showLoginWindow();
    }
}

