package com.care.controller.agent;

import com.care.dao.ChatSessionDAO;
import com.care.dao.MessageDAO;
import com.care.dao.ProductDAO;
import com.care.dao.TicketDAO;
import com.care.dao.UserDAO;
import com.care.model.ChatSession;
import com.care.model.Message;
import com.care.model.Product;
import com.care.model.Ticket;
import com.care.model.User;
import com.care.service.TicketService;
import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Agent Dashboard View
 * Handles viewing and managing assigned tickets
 */
public class AgentDashboardController {
    
    @FXML private Label agentNameLabel;
    @FXML private TableView<TicketDisplay> ticketsTable;
    @FXML private TableColumn<TicketDisplay, Integer> ticketIdCol;
    @FXML private TableColumn<TicketDisplay, Integer> sessionIdCol;
    @FXML private TableColumn<TicketDisplay, String> userNameCol;
    @FXML private TableColumn<TicketDisplay, String> productNameCol;
    @FXML private TableColumn<TicketDisplay, String> statusCol;
    @FXML private TableColumn<TicketDisplay, String> priorityCol;
    @FXML private TableColumn<TicketDisplay, String> createdAtCol;
    @FXML private TableColumn<TicketDisplay, Void> actionsCol;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Text totalTicketsText;
    @FXML private Text openTicketsText;
    @FXML private Text inProgressTicketsText;
    
    private SessionManager sessionManager;
    private ViewFactory viewFactory;
    private TicketDAO ticketDAO;
    private TicketService ticketService;
    private ChatSessionDAO chatSessionDAO;
    private MessageDAO messageDAO;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private List<TicketDisplay> allTickets;
    
    public AgentDashboardController() {
        this.sessionManager = SessionManager.getInstance();
        this.viewFactory = ViewFactory.getInstance();
        this.ticketDAO = new TicketDAO();
        this.ticketService = new TicketService();
        this.chatSessionDAO = new ChatSessionDAO();
        this.messageDAO = new MessageDAO();
        this.userDAO = new UserDAO();
        this.productDAO = new ProductDAO();
    }
    
    @FXML
    private void initialize() {
        if (sessionManager.isLoggedIn()) {
            agentNameLabel.setText(sessionManager.getCurrentUser().getName());
        }
        
        // Setup status filter
        statusFilter.getItems().addAll("All", "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> handleFilterChange());
        
        // Setup table columns
        setupTableColumns();
        
        // Setup actions column
        setupActionsColumn();
        
        // Load tickets
        loadTickets();
    }
    
    private void setupTableColumns() {
        ticketIdCol.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        sessionIdCol.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        // Style columns
        priorityCol.setCellFactory(column -> new TableCell<TicketDisplay, String>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority);
                    switch (priority) {
                        case "HIGH":
                        case "CRITICAL":
                            setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                            break;
                        case "MEDIUM":
                            setStyle("-fx-text-fill: #fd7e14; -fx-font-weight: 600;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #28a745;");
                            break;
                    }
                }
            }
        });
        
        statusCol.setCellFactory(column -> new TableCell<TicketDisplay, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "OPEN":
                            setStyle("-fx-text-fill: #dc3545; -fx-font-weight: 600;");
                            break;
                        case "IN_PROGRESS":
                            setStyle("-fx-text-fill: #007bff; -fx-font-weight: 600;");
                            break;
                        case "RESOLVED":
                            setStyle("-fx-text-fill: #28a745; -fx-font-weight: 600;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
    }
    
    private void setupActionsColumn() {
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️ View");
            private final Button replyBtn = new Button("💬 Reply");
            private final Button resolveBtn = new Button("✓ Resolve");
            private final HBox container = new HBox(5, viewBtn, replyBtn, resolveBtn);
            
            {
                viewBtn.getStyleClass().add("primary-button");
                replyBtn.getStyleClass().add("primary-button");
                resolveBtn.getStyleClass().add("success-button");
                
                viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                replyBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                resolveBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                
                container.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    TicketDisplay ticketDisplay = getTableView().getItems().get(getIndex());
                    viewBtn.setOnAction(e -> handleViewTicket(ticketDisplay));
                    replyBtn.setOnAction(e -> handleReplyToTicket(ticketDisplay));
                    resolveBtn.setOnAction(e -> handleResolveTicket(ticketDisplay));
                    
                    // Disable resolve button if already resolved
                    resolveBtn.setDisable(ticketDisplay.getStatus().equals("RESOLVED") || 
                                         ticketDisplay.getStatus().equals("CLOSED"));
                    
                    setGraphic(container);
                }
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        System.out.println("Refresh clicked");
        loadTickets();
    }
    
    private void handleFilterChange() {
        String filter = statusFilter.getValue();
        if (filter.equals("All")) {
            ticketsTable.getItems().setAll(allTickets);
        } else {
            List<TicketDisplay> filtered = allTickets.stream()
                    .filter(t -> t.getStatus().equals(filter))
                    .collect(Collectors.toList());
            ticketsTable.getItems().setAll(filtered);
        }
        updateStatistics();
    }
    
    private void loadTickets() {
        try {
            int agentId = sessionManager.getCurrentUser().getUserId();
            List<Ticket> tickets = ticketDAO.getByAgentId(agentId);
            
            allTickets = tickets.stream()
                    .map(this::createTicketDisplay)
                    .collect(Collectors.toList());
            
            ticketsTable.getItems().setAll(allTickets);
            updateStatistics();
            
            System.out.println("✓ Loaded " + tickets.size() + " tickets for agent ID: " + agentId);
        } catch (Exception e) {
            System.err.println("Error loading tickets");
            e.printStackTrace();
            showError("Failed to load tickets: " + e.getMessage());
        }
    }
    
    private TicketDisplay createTicketDisplay(Ticket ticket) {
        TicketDisplay display = new TicketDisplay();
        display.setTicket(ticket);
        display.setTicketId(ticket.getTicketId());
        display.setSessionId(ticket.getSessionId());
        display.setStatus(ticket.getStatus());
        display.setPriority(ticket.getPriority());
        
        if (ticket.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            display.setCreatedAt(ticket.getCreatedAt().format(formatter));
        } else {
            display.setCreatedAt("N/A");
        }
        
        // Load related data
        try {
            ChatSession session = chatSessionDAO.getById(ticket.getSessionId());
            if (session != null) {
                User user = userDAO.findById(session.getUserId());
                if (user != null) {
                    display.setUserName(user.getName());
                }
                
                Product product = productDAO.findById(session.getProductId());
                if (product != null) {
                    display.setProductName(product.getName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading related data for ticket " + ticket.getTicketId());
        }
        
        return display;
    }
    
    private void updateStatistics() {
        int total = ticketsTable.getItems().size();
        long open = ticketsTable.getItems().stream().filter(t -> t.getStatus().equals("OPEN")).count();
        long inProgress = ticketsTable.getItems().stream().filter(t -> t.getStatus().equals("IN_PROGRESS")).count();
        
        totalTicketsText.setText(String.valueOf(total));
        openTicketsText.setText(String.valueOf(open));
        inProgressTicketsText.setText(String.valueOf(inProgress));
    }
    
    private void handleViewTicket(TicketDisplay ticketDisplay) {
        try {
            List<Message> messages = messageDAO.getBySessionId(ticketDisplay.getSessionId());
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Ticket #" + ticketDisplay.getTicketId() + " - Conversation History");
            
            VBox container = new VBox(15);
            container.setPadding(new Insets(20));
            container.setStyle("-fx-background-color: #f8f9fa;");
            
            // Header
            Text header = new Text("Conversation History (" + messages.size() + " messages)");
            header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            // Info box
            VBox infoBox = new VBox(5);
            infoBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;");
            infoBox.getChildren().addAll(
                new Text("Customer: " + ticketDisplay.getUserName()),
                new Text("Product: " + ticketDisplay.getProductName()),
                new Text("Status: " + ticketDisplay.getStatus() + " | Priority: " + ticketDisplay.getPriority())
            );
            
            // Messages scroll pane
            VBox messagesBox = new VBox(10);
            messagesBox.setStyle("-fx-padding: 10;");
            
            for (Message msg : messages) {
                VBox msgBox = new VBox(5);
                msgBox.setStyle("-fx-background-color: " + 
                    (msg.getSenderType().equals("USER") ? "#e3f2fd" : 
                     msg.getSenderType().equals("BOT") ? "#f3e5f5" : "#e8f5e9") +
                    "; -fx-padding: 10; -fx-background-radius: 8;");
                
                Text senderText = new Text(msg.getSenderType().equals("USER") ? "👤 Customer" :
                                          msg.getSenderType().equals("BOT") ? "🤖 AI Assistant" : "👨‍💼 Agent");
                senderText.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                
                Text contentText = new Text(msg.getContent());
                contentText.setWrappingWidth(550);
                
                msgBox.getChildren().addAll(senderText, contentText);
                messagesBox.getChildren().add(msgBox);
            }
            
            ScrollPane scrollPane = new ScrollPane(messagesBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);
            scrollPane.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
            
            Button closeBtn = new Button("Close");
            closeBtn.getStyleClass().add("secondary-button");
            closeBtn.setOnAction(e -> dialogStage.close());
            
            HBox buttonBox = new HBox(closeBtn);
            buttonBox.setAlignment(Pos.CENTER);
            
            container.getChildren().addAll(header, infoBox, scrollPane, buttonBox);
            
            Scene scene = new Scene(container, 650, 600);
            scene.getStylesheets().add(getClass().getResource("/com/care/styles/main.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error viewing ticket conversation");
            e.printStackTrace();
            showError("Failed to load conversation: " + e.getMessage());
        }
    }
    
    private void handleReplyToTicket(TicketDisplay ticketDisplay) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Reply to Ticket #" + ticketDisplay.getTicketId());
        
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        Text header = new Text("Send Reply to Customer");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");
        infoBox.getChildren().addAll(
            new Text("Customer: " + ticketDisplay.getUserName()),
            new Text("Product: " + ticketDisplay.getProductName())
        );
        
        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Type your reply here...");
        replyArea.setWrapText(true);
        replyArea.setPrefRowCount(8);
        
        CheckBox markInProgressCheck = new CheckBox("Mark ticket as 'In Progress'");
        if (!ticketDisplay.getStatus().equals("OPEN")) {
            markInProgressCheck.setSelected(false);
            markInProgressCheck.setDisable(true);
        } else {
            markInProgressCheck.setSelected(true);
        }
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button sendBtn = new Button("💬 Send Reply");
        sendBtn.getStyleClass().add("primary-button");
        sendBtn.setOnAction(e -> {
            String reply = replyArea.getText().trim();
            if (reply.isEmpty()) {
                showError("Please enter a reply message.");
                return;
            }
            
            try {
                // Create agent message
                Message message = new Message();
                message.setSessionId(ticketDisplay.getSessionId());
                message.setSenderType("AGENT");
                message.setContent(reply);
                messageDAO.create(message);
                
                // Update ticket status if checked
                if (markInProgressCheck.isSelected()) {
                    ticketService.updateTicketStatus(ticketDisplay.getTicket().getTicketId(), "IN_PROGRESS");
                }
                
                showInfo("Reply sent successfully!");
                dialogStage.close();
                loadTickets(); // Refresh table
                
            } catch (Exception ex) {
                System.err.println("Error sending reply");
                ex.printStackTrace();
                showError("Failed to send reply: " + ex.getMessage());
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        cancelBtn.setOnAction(e -> dialogStage.close());
        
        buttonBox.getChildren().addAll(sendBtn, cancelBtn);
        
        container.getChildren().addAll(header, infoBox, new Text("Your Reply:"), replyArea, markInProgressCheck, buttonBox);
        
        Scene scene = new Scene(container, 600, 450);
        scene.getStylesheets().add(getClass().getResource("/com/care/styles/main.css").toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    private void handleResolveTicket(TicketDisplay ticketDisplay) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Resolve Ticket");
        confirmAlert.setHeaderText("Mark Ticket #" + ticketDisplay.getTicketId() + " as Resolved?");
        confirmAlert.setContentText("This will close the ticket and notify the customer.\n" +
                                   "Customer: " + ticketDisplay.getUserName() + "\n" +
                                   "Product: " + ticketDisplay.getProductName());
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = ticketService.updateTicketStatus(
                        ticketDisplay.getTicket().getTicketId(), "RESOLVED");
                    
                    if (success) {
                        // Add system message
                        Message systemMsg = new Message();
                        systemMsg.setSessionId(ticketDisplay.getSessionId());
                        systemMsg.setSenderType("SYSTEM");
                        systemMsg.setContent("✅ Ticket resolved by " + sessionManager.getCurrentUser().getName());
                        messageDAO.create(systemMsg);
                        
                        showInfo("Ticket #" + ticketDisplay.getTicketId() + " marked as resolved!");
                        loadTickets(); // Refresh
                    } else {
                        showError("Failed to resolve ticket.");
                    }
                } catch (Exception e) {
                    System.err.println("Error resolving ticket");
                    e.printStackTrace();
                    showError("Error: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleLogout() {
        sessionManager.logout();
        viewFactory.showLoginWindow();
    }
    
    @FXML
    private void handleExit() {
        System.exit(0);
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for table display
    public static class TicketDisplay {
        private Ticket ticket;
        private int ticketId;
        private int sessionId;
        private String userName;
        private String productName;
        private String status;
        private String priority;
        private String createdAt;
        
        public Ticket getTicket() { return ticket; }
        public void setTicket(Ticket ticket) { this.ticket = ticket; }
        
        public int getTicketId() { return ticketId; }
        public void setTicketId(int ticketId) { this.ticketId = ticketId; }
        
        public int getSessionId() { return sessionId; }
        public void setSessionId(int sessionId) { this.sessionId = sessionId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}

