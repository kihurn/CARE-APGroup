package com.care.controller.user;

import com.care.dao.ChatSessionDAO;
import com.care.dao.MessageDAO;
import com.care.dao.ProductDAO;
import com.care.model.ChatSession;
import com.care.model.Message;
import com.care.model.Product;
import com.care.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for User History View
 */
public class UserHistoryController {
    
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Label totalSessionsLabel;
    @FXML private TableView<ChatSessionDisplay> historyTable;
    @FXML private TableColumn<ChatSessionDisplay, Integer> sessionIdCol;
    @FXML private TableColumn<ChatSessionDisplay, String> dateCol;
    @FXML private TableColumn<ChatSessionDisplay, String> productCol;
    @FXML private TableColumn<ChatSessionDisplay, String> statusCol;
    @FXML private TableColumn<ChatSessionDisplay, Integer> messagesCountCol;
    @FXML private TableColumn<ChatSessionDisplay, Void> actionsCol;
    
    private SessionManager sessionManager;
    private ChatSessionDAO chatSessionDAO;
    private MessageDAO messageDAO;
    private ProductDAO productDAO;
    private List<ChatSession> allSessions;
    
    public UserHistoryController() {
        this.sessionManager = SessionManager.getInstance();
        this.chatSessionDAO = new ChatSessionDAO();
        this.messageDAO = new MessageDAO();
        this.productDAO = new ProductDAO();
    }
    
    @FXML
    private void initialize() {
        // Setup table resize policy
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Setup status filter
        statusFilterCombo.getItems().addAll("All", "ACTIVE", "CLOSED", "ESCALATED");
        statusFilterCombo.setValue("All");
        
        // Setup table columns
        sessionIdCol.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getSessionId()).asObject());
        dateCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        productCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProductName()));
        statusCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        messagesCountCol.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getMessageCount()).asObject());
        
        // Setup actions column
        setupActionsColumn();
        
        // Load data
        loadHistory();
    }
    
    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing history...");
        loadHistory();
    }
    
    @FXML
    private void handleFilterChange() {
        String filter = statusFilterCombo.getValue();
        System.out.println("Filtering by: " + filter);
        applyFilter();
    }
    
    /**
     * Setup actions column with Continue and Delete buttons
     */
    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button continueBtn = new Button("💬 Continue");
            private final Button deleteBtn = new Button("🗑️ Delete");
            private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(5, continueBtn, deleteBtn);
            
            {
                continueBtn.getStyleClass().add("primary-button");
                deleteBtn.getStyleClass().add("danger-button");
                continueBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                container.setAlignment(javafx.geometry.Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    ChatSessionDisplay display = getTableView().getItems().get(getIndex());
                    ChatSession session = display.getSession();
                    
                    // Disable continue button only if chat is CLOSED
                    if ("CLOSED".equals(session.getStatus())) {
                        continueBtn.setDisable(true);
                        continueBtn.setText("❌ Ended");
                    } else {
                        continueBtn.setDisable(false);
                        if ("ESCALATED".equals(session.getStatus())) {
                            continueBtn.setText("💬 View Chat");
                        } else {
                            continueBtn.setText("💬 Continue");
                        }
                    }
                    
                    continueBtn.setOnAction(e -> handleContinueChat(session));
                    deleteBtn.setOnAction(e -> handleDeleteSession(session));
                    setGraphic(container);
                }
            }
        });
    }
    
    /**
     * Load chat history from database
     */
    private void loadHistory() {
        try {
            if (!sessionManager.isLoggedIn()) {
                System.err.println("No user logged in");
                return;
            }
            
            int userId = sessionManager.getCurrentUser().getUserId();
            allSessions = chatSessionDAO.getByUserId(userId);
            
            System.out.println("✓ Loaded " + allSessions.size() + " chat sessions for user: " + userId);
            
            // Apply filter
            applyFilter();
            
        } catch (Exception e) {
            System.err.println("Error loading chat history");
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Load History");
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }
    
    /**
     * Apply filter to chat sessions
     */
    private void applyFilter() {
        String filter = statusFilterCombo.getValue();
        
        List<ChatSession> filteredSessions = allSessions;
        if (!"All".equals(filter)) {
            filteredSessions = allSessions.stream()
                .filter(session -> filter.equals(session.getStatus()))
                .collect(Collectors.toList());
        }
        
        // Convert to display objects
        historyTable.getItems().clear();
        for (ChatSession session : filteredSessions) {
            ChatSessionDisplay display = new ChatSessionDisplay(session);
            historyTable.getItems().add(display);
        }
        
        totalSessionsLabel.setText("Total Sessions: " + filteredSessions.size());
    }
    
    /**
     * Handle continue chat button click - Resume chatting in this session
     */
    private void handleContinueChat(ChatSession session) {
        System.out.println("Continue chat for session: " + session.getSessionId());
        
        if ("CLOSED".equals(session.getStatus())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cannot Continue");
            alert.setHeaderText("Chat Has Ended");
            alert.setContentText("This chat session has been closed. Please start a new chat.");
            alert.show();
            return;
        }
        
        try {
            // Store session in SessionManager so ChatArea can load it
            sessionManager.setCurrentChatSession(session);
            
            // Navigate to ChatArea
            com.care.util.ViewFactory viewFactory = com.care.util.ViewFactory.getInstance();
            viewFactory.setUserSelectedMenuItem("ChatArea");
            
            System.out.println("✓ Navigating to ChatArea with session " + session.getSessionId());
            
        } catch (Exception e) {
            System.err.println("Error continuing chat: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Continue Chat");
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }
    
    /**
     * Handle delete session button click
     */
    private void handleDeleteSession(ChatSession session) {
        System.out.println("Delete session: " + session.getSessionId());
        
        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Chat Session");
        confirmAlert.setHeaderText("Delete Session #" + session.getSessionId() + "?");
        confirmAlert.setContentText("This will permanently delete this chat session and all messages.\nThis action cannot be undone.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete session (CASCADE will delete messages automatically)
                    String deleteQuery = "DELETE FROM chat_sessions WHERE session_id = ?";
                    java.sql.Connection conn = com.care.util.DatabaseDriver.getInstance().getConnection();
                    java.sql.PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    stmt.setInt(1, session.getSessionId());
                    int rowsDeleted = stmt.executeUpdate();
                    stmt.close();
                    
                    if (rowsDeleted > 0) {
                        System.out.println("✓ Session deleted successfully");
                        
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText("Chat Deleted");
                        successAlert.setContentText("Chat session has been deleted successfully.");
                        successAlert.show();
                        
                        // Refresh the list
                        loadHistory();
                    } else {
                        throw new Exception("No rows deleted");
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error deleting session: " + e.getMessage());
                    e.printStackTrace();
                    
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to Delete");
                    alert.setContentText("Error: " + e.getMessage());
                    alert.show();
                }
            }
        });
    }
    
    /**
     * Inner class for table display
     */
    private class ChatSessionDisplay {
        private ChatSession session;
        private int sessionId;
        private String formattedDate;
        private String productName;
        private String status;
        private int messageCount;
        
        public ChatSessionDisplay(ChatSession session) {
            this.session = session;
            this.sessionId = session.getSessionId();
            this.status = session.getStatus();
            
            // Format date
            if (session.getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
                this.formattedDate = session.getCreatedAt().format(formatter);
            } else {
                this.formattedDate = "N/A";
            }
            
            // Get product name
            if (session.getProductId() != null) {
                Product product = productDAO.findById(session.getProductId());
                this.productName = product != null ? product.getName() : "Unknown Product";
            } else {
                this.productName = "No Product";
            }
            
            // Get message count
            List<Message> messages = messageDAO.getBySessionId(session.getSessionId());
            this.messageCount = messages.size();
        }
        
        public ChatSession getSession() { return session; }
        public int getSessionId() { return sessionId; }
        public String getFormattedDate() { return formattedDate; }
        public String getProductName() { return productName; }
        public String getStatus() { return status; }
        public int getMessageCount() { return messageCount; }
    }
}

