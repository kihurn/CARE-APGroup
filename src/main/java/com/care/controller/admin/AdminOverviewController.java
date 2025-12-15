package com.care.controller.admin;

import com.care.dao.ChatSessionDAO;
import com.care.dao.TicketDAO;
import com.care.service.ProductService;
import com.care.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

/**
 * Controller for Admin Overview (Dashboard Statistics)
 * Shows real-time statistics from database
 */
public class AdminOverviewController {
    
    @FXML private Text totalUsersLabel;
    @FXML private Text activeSessionsLabel;
    @FXML private Text openTicketsLabel;
    @FXML private Text totalProductsLabel;
    @FXML private TableView<?> recentActivityTable;
    
    private UserService userService;
    private ProductService productService;
    private ChatSessionDAO chatSessionDAO;
    private TicketDAO ticketDAO;
    
    public AdminOverviewController() {
        this.userService = new UserService();
        this.productService = new ProductService();
        this.chatSessionDAO = new ChatSessionDAO();
        this.ticketDAO = new TicketDAO();
    }
    
    @FXML
    private void initialize() {
        System.out.println("Initializing AdminOverviewController...");
        loadStatistics();
    }
    
    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing statistics...");
        loadStatistics();
    }
    
    @FXML
    private void handleFullReport() {
        System.out.println("Generating full report...");
        // TODO: Implement full report generation
    }
    
    private void loadStatistics() {
        try {
            // Load total users
            int totalUsers = userService.getAllUsers().size();
            totalUsersLabel.setText(String.valueOf(totalUsers));
            
            // Load total products
            int totalProducts = productService.getAllProducts().size();
            totalProductsLabel.setText(String.valueOf(totalProducts));
            
            // Load active sessions
            int activeSessions = chatSessionDAO.getActiveSessions().size();
            activeSessionsLabel.setText(String.valueOf(activeSessions));
            
            // Load open tickets (OPEN + IN_PROGRESS)
            int openTickets = ticketDAO.getByStatus("OPEN").size() + 
                             ticketDAO.getByStatus("IN_PROGRESS").size();
            openTicketsLabel.setText(String.valueOf(openTickets));
            
            System.out.println("✓ Loaded statistics: " + totalUsers + " users, " + 
                             totalProducts + " products, " + activeSessions + " active sessions, " +
                             openTickets + " open tickets");
        } catch (Exception e) {
            System.err.println("Error loading statistics");
            e.printStackTrace();
            totalUsersLabel.setText("0");
            activeSessionsLabel.setText("0");
            openTicketsLabel.setText("0");
            totalProductsLabel.setText("0");
        }
    }
}

