package com.care.controller.admin;

import com.care.dao.ChatSessionDAO;
import com.care.dao.MessageDAO;
import com.care.dao.ProductDAO;
import com.care.dao.UserDAO;
import com.care.model.ChatSession;
import com.care.model.Message;
import com.care.model.Product;
import com.care.model.Ticket;
import com.care.model.User;
import com.care.service.TicketService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Admin Tickets Management
 */
public class AdminTicketsController {
    
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> priorityFilterCombo;
    @FXML private Label totalTicketsLabel;
    @FXML private Label openTicketsLabel;
    @FXML private Label inProgressTicketsLabel;
    @FXML private Label resolvedTicketsLabel;
    @FXML private TableView<TicketDisplay> ticketsTable;
    @FXML private TableColumn<TicketDisplay, Integer> ticketIdCol;
    @FXML private TableColumn<TicketDisplay, Integer> sessionIdCol;
    @FXML private TableColumn<TicketDisplay, String> userCol;
    @FXML private TableColumn<TicketDisplay, String> productCol;
    @FXML private TableColumn<TicketDisplay, String> priorityCol;
    @FXML private TableColumn<TicketDisplay, String> statusCol;
    @FXML private TableColumn<TicketDisplay, String> agentCol;
    @FXML private TableColumn<TicketDisplay, String> createdAtCol;
    @FXML private TableColumn<TicketDisplay, Void> actionsCol;
    
    private TicketService ticketService;
    private ChatSessionDAO chatSessionDAO;
    private MessageDAO messageDAO;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private List<Ticket> allTickets;
    
    public AdminTicketsController() {
        this.ticketService = new TicketService();
        this.chatSessionDAO = new ChatSessionDAO();
        this.messageDAO = new MessageDAO();
        this.userDAO = new UserDAO();
        this.productDAO = new ProductDAO();
    }
    
    @FXML
    private void initialize() {
        System.out.println("Initializing AdminTicketsController...");
        
        // Setup table resize policy
        ticketsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Setup filters
        statusFilterCombo.getItems().addAll("All Statuses", "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");
        statusFilterCombo.setValue("All Statuses");
        
        priorityFilterCombo.getItems().addAll("All Priorities", "LOW", "MEDIUM", "HIGH", "CRITICAL");
        priorityFilterCombo.setValue("All Priorities");
        
        // Setup table columns
        ticketIdCol.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getTicketId()).asObject());
        sessionIdCol.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getSessionId()).asObject());
        userCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUserName()));
        productCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProductName()));
        priorityCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPriority()));
        statusCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        agentCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getAgentName()));
        createdAtCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        
        // Setup actions column
        setupActionsColumn();
        
        // Load data
        loadTickets();
    }
    
    /**
     * Setup actions column with buttons
     */
    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button assignBtn = new Button("👤");
            private final Button resolveBtn = new Button("✓");
            
            {
                viewBtn.setTooltip(new Tooltip("View Conversation"));
                assignBtn.setTooltip(new Tooltip("Assign Agent"));
                resolveBtn.setTooltip(new Tooltip("Mark Resolved"));
                
                viewBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4 8;");
                assignBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4 8;");
                resolveBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4 8;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    TicketDisplay display = getTableView().getItems().get(getIndex());
                    
                    viewBtn.setOnAction(e -> handleViewConversation(display.getSessionId()));
                    assignBtn.setOnAction(e -> handleAssignAgent(display.getTicketId()));
                    resolveBtn.setOnAction(e -> handleResolve(display.getTicketId()));
                    
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, viewBtn, assignBtn, resolveBtn);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing tickets...");
        loadTickets();
    }
    
    @FXML
    private void handleFilterChange() {
        String statusFilter = statusFilterCombo.getValue();
        String priorityFilter = priorityFilterCombo.getValue();
        System.out.println("Filtering by status: " + statusFilter + ", priority: " + priorityFilter);
        applyFilters();
    }
    
    /**
     * Load tickets from database
     */
    private void loadTickets() {
        try {
            allTickets = ticketService.getAllTickets();
            System.out.println("✓ Loaded " + allTickets.size() + " tickets from database");
            
            applyFilters();
            
        } catch (Exception e) {
            System.err.println("Error loading tickets");
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Load Tickets");
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }
    
    /**
     * Apply filters to tickets
     */
    private void applyFilters() {
        String statusFilter = statusFilterCombo.getValue();
        String priorityFilter = priorityFilterCombo.getValue();
        
        List<Ticket> filteredTickets = allTickets;
        
        // Apply status filter
        if (!"All Statuses".equals(statusFilter)) {
            filteredTickets = filteredTickets.stream()
                .filter(ticket -> statusFilter.equals(ticket.getStatus()))
                .collect(Collectors.toList());
        }
        
        // Apply priority filter
        if (!"All Priorities".equals(priorityFilter)) {
            filteredTickets = filteredTickets.stream()
                .filter(ticket -> priorityFilter.equals(ticket.getPriority()))
                .collect(Collectors.toList());
        }
        
        // Convert to display objects
        ticketsTable.getItems().clear();
        for (Ticket ticket : filteredTickets) {
            TicketDisplay display = new TicketDisplay(ticket);
            ticketsTable.getItems().add(display);
        }
        
        // Update stats based on filtered tickets
        updateStats(filteredTickets);
    }
    
    /**
     * Update statistics labels based on the provided ticket list
     */
    private void updateStats(List<Ticket> tickets) {
        int total = tickets.size();
        int open = (int) tickets.stream().filter(t -> "OPEN".equals(t.getStatus())).count();
        int inProgress = (int) tickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        int resolved = (int) tickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();
        
        totalTicketsLabel.setText("Total Tickets: " + total);
        openTicketsLabel.setText("Open: " + open);
        inProgressTicketsLabel.setText("In Progress: " + inProgress);
        resolvedTicketsLabel.setText("Resolved: " + resolved);
    }
    
    /**
     * Handle view conversation button
     */
    private void handleViewConversation(int sessionId) {
        System.out.println("View conversation for session: " + sessionId);
        
        try {
            List<Message> messages = messageDAO.getBySessionId(sessionId);
            
            // Create dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Ticket Conversation");
            dialog.setHeaderText("Session #" + sessionId + " - " + messages.size() + " messages");
            
            // Create message display
            VBox messagesBox = new VBox(10);
            messagesBox.setStyle("-fx-padding: 10;");
            
            for (Message msg : messages) {
                String senderLabel = msg.getSenderType();
                String icon = "USER".equals(senderLabel) ? "👤" : 
                             "BOT".equals(senderLabel) ? "🤖" : "👨‍💼";
                
                Label messageLabel = new Label(icon + " " + senderLabel + ": " + msg.getContent());
                messageLabel.setWrapText(true);
                messageLabel.setMaxWidth(550);
                messageLabel.setStyle(
                    "USER".equals(senderLabel) ? 
                    "-fx-background-color: #e3f2fd; -fx-padding: 10; -fx-background-radius: 10;" :
                    "-fx-background-color: #f3e5f5; -fx-padding: 10; -fx-background-radius: 10;"
                );
                messagesBox.getChildren().add(messageLabel);
            }
            
            ScrollPane scrollPane = new ScrollPane(messagesBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(450);
            scrollPane.setPrefWidth(600);
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            dialog.show();
            
        } catch (Exception e) {
            System.err.println("Error loading conversation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle assign agent button
     */
    private void handleAssignAgent(int ticketId) {
        System.out.println("Assign agent to ticket: " + ticketId);
        
        try {
            // Get all agents
            List<User> agents = userDAO.findAll().stream()
                .filter(user -> "AGENT".equals(user.getRole()))
                .collect(Collectors.toList());
            
            if (agents.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Agents");
                alert.setHeaderText("No Agents Available");
                alert.setContentText("There are no agents in the system to assign.");
                alert.show();
                return;
            }
            
            // Create dialog
            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle("Assign Agent");
            dialog.setHeaderText("Select an agent to assign to Ticket #" + ticketId);
            
            // Create agent selection
            ComboBox<String> agentCombo = new ComboBox<>();
            for (User agent : agents) {
                agentCombo.getItems().add(agent.getUserId() + " - " + agent.getName());
            }
            agentCombo.setValue(agentCombo.getItems().get(0));
            
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            content.getChildren().addAll(new Label("Agent:"), agentCombo);
            
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    String selected = agentCombo.getValue();
                    return Integer.parseInt(selected.split(" - ")[0]);
                }
                return null;
            });
            
            dialog.showAndWait().ifPresent(agentId -> {
                boolean success = ticketService.assignTicketToAgent(ticketId, agentId);
                
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Agent Assigned");
                    alert.setContentText("Ticket #" + ticketId + " has been assigned.");
                    alert.show();
                    
                    loadTickets(); // Refresh
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error assigning agent: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle resolve button
     */
    private void handleResolve(int ticketId) {
        System.out.println("Resolve ticket: " + ticketId);
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Resolve Ticket");
        confirmAlert.setHeaderText("Mark Ticket #" + ticketId + " as Resolved?");
        confirmAlert.setContentText("This will close the ticket.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = ticketService.updateTicketStatus(ticketId, "RESOLVED");
                
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Ticket Resolved");
                    alert.setContentText("Ticket #" + ticketId + " has been marked as resolved.");
                    alert.show();
                    
                    loadTickets(); // Refresh
                }
            }
        });
    }
    
    /**
     * Inner class for table display
     */
    private class TicketDisplay {
        private int ticketId;
        private int sessionId;
        private String userName;
        private String productName;
        private String priority;
        private String status;
        private String agentName;
        private String formattedDate;
        
        public TicketDisplay(Ticket ticket) {
            this.ticketId = ticket.getTicketId();
            this.sessionId = ticket.getSessionId();
            this.priority = ticket.getPriority();
            this.status = ticket.getStatus();
            
            // Get session info
            ChatSession session = chatSessionDAO.getById(ticket.getSessionId());
            if (session != null) {
                // Get user name
                User user = userDAO.findById(session.getUserId());
                this.userName = user != null ? user.getName() : "Unknown User";
                
                // Get product name
                if (session.getProductId() != null) {
                    Product product = productDAO.findById(session.getProductId());
                    this.productName = product != null ? product.getName() : "Unknown Product";
                } else {
                    this.productName = "No Product";
                }
            } else {
                this.userName = "Unknown";
                this.productName = "Unknown";
            }
            
            // Get agent name
            if (ticket.getAssignedAgentId() != null) {
                User agent = userDAO.findById(ticket.getAssignedAgentId());
                this.agentName = agent != null ? agent.getName() : "Unassigned";
            } else {
                this.agentName = "Unassigned";
            }
            
            // Format date
            if (ticket.getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
                this.formattedDate = ticket.getCreatedAt().format(formatter);
            } else {
                this.formattedDate = "N/A";
            }
        }
        
        public int getTicketId() { return ticketId; }
        public int getSessionId() { return sessionId; }
        public String getUserName() { return userName; }
        public String getProductName() { return productName; }
        public String getPriority() { return priority; }
        public String getStatus() { return status; }
        public String getAgentName() { return agentName; }
        public String getFormattedDate() { return formattedDate; }
    }
}
