package com.care.controller.admin;

import com.care.dao.ChatSessionDAO;
import com.care.dao.TicketDAO;
import com.care.model.Message;
import com.care.service.AnalyticsService;
import com.care.service.ProductService;
import com.care.service.ReportGeneratorService;
import com.care.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for Admin Overview (Dashboard Statistics)
 * Shows real-time statistics and escalation analytics with dummy data
 */
public class AdminOverviewController {
    
    @FXML private Text totalUsersText;
    @FXML private Text totalChatsText;
    @FXML private Text activeSessionsText;
    @FXML private Text totalProductsText;
    @FXML private Text escalationRateText;
    @FXML private Text openTicketsText;
    @FXML private Text resolvedTicketsText;
    @FXML private Text satisfactionText;
    
    @FXML private BarChart<String, Number> productEscalationChart;
    @FXML private PieChart resolutionTypeChart;
    @FXML private LineChart<String, Number> escalationTrendsChart;
    
    @FXML private TableView<EscalationRow> escalationDetailsTable;
    @FXML private TableColumn<EscalationRow, Integer> ticketIdCol;
    @FXML private TableColumn<EscalationRow, String> userNameCol;
    @FXML private TableColumn<EscalationRow, String> productNameCol;
    @FXML private TableColumn<EscalationRow, String> escalatedAtCol;
    @FXML private TableColumn<EscalationRow, Integer> messagesCol;
    @FXML private TableColumn<EscalationRow, String> priorityCol;
    @FXML private TableColumn<EscalationRow, String> statusCol;
    @FXML private TableColumn<EscalationRow, Void> actionsCol;
    
    private UserService userService;
    private ProductService productService;
    private ChatSessionDAO chatSessionDAO;
    private TicketDAO ticketDAO;
    private AnalyticsService analyticsService;
    private ReportGeneratorService reportGenerator;
    
    public AdminOverviewController() {
        this.userService = new UserService();
        this.productService = new ProductService();
        this.chatSessionDAO = new ChatSessionDAO();
        this.ticketDAO = new TicketDAO();
        this.analyticsService = new AnalyticsService();
        this.reportGenerator = new ReportGeneratorService();
    }
    
    @FXML
    private void initialize() {
        System.out.println("Initializing Enhanced AdminOverviewController...");
        setupTable();
        loadDashboard();
    }
    
    private void setupTable() {
        ticketIdCol.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        escalatedAtCol.setCellValueFactory(new PropertyValueFactory<>("escalatedAt"));
        messagesCol.setCellValueFactory(new PropertyValueFactory<>("messageCount"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Add "View Chat" button
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View Chat");
            
            {
                viewButton.getStyleClass().add("primary-button");
                viewButton.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-min-width: 80px; -fx-max-width: 100px;");
                viewButton.setOnAction(event -> {
                    EscalationRow row = getTableView().getItems().get(getIndex());
                    showConversationDialog(row);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing dashboard...");
        loadDashboard();
    }
    
    @FXML
    private void handleFullReport() {
        System.out.println("Generating PDF report...");
        
        try {
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Analytics Report");
            fileChooser.setInitialFileName("CARE_Analytics_Report_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            
            // Set initial directory to user's Documents folder
            String userHome = System.getProperty("user.home");
            File documentsDir = new File(userHome, "Documents");
            if (documentsDir.exists()) {
                fileChooser.setInitialDirectory(documentsDir);
            }
            
            // Show save dialog
            Stage stage = (Stage) escalationDetailsTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                // Generate the PDF
                boolean success = reportGenerator.generateAnalyticsReport(file.getAbsolutePath());
                
                if (success) {
                    // Show success alert with option to open
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Report Generated");
                    alert.setHeaderText("PDF Report Created Successfully!");
                    alert.setContentText("Report saved to:\n" + file.getAbsolutePath() + 
                                       "\n\nWould you like to open it now?");
                    
                    ButtonType openButton = new ButtonType("Open Report");
                    ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(openButton, closeButton);
                    
                    alert.showAndWait().ifPresent(response -> {
                        if (response == openButton) {
                            try {
                                Desktop.getDesktop().open(file);
                            } catch (Exception e) {
                                System.err.println("Could not open PDF: " + e.getMessage());
                            }
                        }
                    });
                } else {
                    showError("Failed to generate PDF report. Check console for details.");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error generating PDF report");
            e.printStackTrace();
            showError("Error generating report: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Report Generation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void loadDashboard() {
        try {
            // Load real data where available, supplement with dummy data
            loadKeyMetrics();
            loadCharts();
            loadEscalationDetails();
            
            System.out.println("✓ Dashboard loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading dashboard");
            e.printStackTrace();
        }
    }
    
    private void loadKeyMetrics() {
        try {
            // Load real data from SQL database
            int totalUsers = userService.getAllUsers().size();
            int totalProducts = productService.getAllProducts().size();
            int activeSessions = chatSessionDAO.getActiveSessions().size();
            int openTickets = ticketDAO.getByStatus("OPEN").size() + 
                             ticketDAO.getByStatus("IN_PROGRESS").size();
            int totalChats = analyticsService.getTotalChatSessions();
            double escalationRate = analyticsService.getEscalationRate();
            int resolvedTickets = analyticsService.getResolvedTicketsCount();
            double satisfaction = analyticsService.getUserSatisfactionScore();
            
            // Display real data from database
            totalUsersText.setText(String.valueOf(totalUsers));
            totalChatsText.setText(String.valueOf(totalChats));
            activeSessionsText.setText(String.valueOf(activeSessions));
            totalProductsText.setText(String.valueOf(totalProducts));
            escalationRateText.setText(String.format("%.1f%%", escalationRate));
            openTicketsText.setText(String.valueOf(openTickets));
            resolvedTicketsText.setText(String.valueOf(resolvedTickets));
            satisfactionText.setText(String.format("%.1f", satisfaction));
            
            System.out.println("✓ Metrics loaded from database:");
            System.out.println("  - Total Users: " + totalUsers);
            System.out.println("  - Total Chats: " + totalChats);
            System.out.println("  - Active Sessions: " + activeSessions);
            System.out.println("  - Total Products: " + totalProducts);
            System.out.println("  - Escalation Rate: " + String.format("%.1f%%", escalationRate));
            System.out.println("  - Open Tickets: " + openTickets);
            System.out.println("  - Resolved Tickets: " + resolvedTickets);
            System.out.println("  - Satisfaction: " + String.format("%.1f", satisfaction));
            
        } catch (Exception e) {
            System.err.println("Error loading metrics from database: " + e.getMessage());
            e.printStackTrace();
            
            // Show zeros if database has errors
            totalUsersText.setText("0");
            totalChatsText.setText("0");
            activeSessionsText.setText("0");
            totalProductsText.setText("0");
            escalationRateText.setText("0.0%");
            openTicketsText.setText("0");
            resolvedTicketsText.setText("0");
            satisfactionText.setText("0.0");
        }
    }
    
    private void loadCharts() {
        try {
            loadProductEscalationChart();
            System.out.println("✓ Product escalation chart loaded");
        } catch (Exception e) {
            System.err.println("Error loading product chart: " + e.getMessage());
        }
        
        try {
            loadResolutionTypeChart();
            System.out.println("✓ Resolution type chart loaded");
        } catch (Exception e) {
            System.err.println("Error loading resolution chart: " + e.getMessage());
        }
    }
    
    private void loadProductEscalationChart() {
        try {
            // Load real escalation data from SQL database
            Map<String, Integer> escalations = analyticsService.getEscalationsByProduct();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Escalations");
            
            if (escalations.isEmpty()) {
                // Show "No Data" if database is empty
                series.getData().add(new XYChart.Data<>("No Data Yet", 0));
            } else {
                // Display real data from database
                for (Map.Entry<String, Integer> entry : escalations.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
                System.out.println("✓ Loaded " + escalations.size() + " products with escalations");
            }
            
            productEscalationChart.getData().clear();
            productEscalationChart.getData().add(series);
        } catch (Exception e) {
            System.err.println("Error loading product escalation chart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadResolutionTypeChart() {
        try {
            // Load real resolution data from SQL database
            Map<String, Double> distribution = analyticsService.getResolutionTypeDistribution();
            
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            
            if (distribution.isEmpty() || distribution.values().stream().allMatch(v -> v == 0.0)) {
                // Show "No Data" if database is empty
                pieData.add(new PieChart.Data("No Data Yet (100%)", 100.0));
            } else {
                // Display real data from database
                for (Map.Entry<String, Double> entry : distribution.entrySet()) {
                    pieData.add(new PieChart.Data(entry.getKey() + " (" + 
                        String.format("%.1f%%", entry.getValue()) + ")", entry.getValue()));
                }
                System.out.println("✓ Loaded resolution distribution: " + distribution);
            }
            
            resolutionTypeChart.setData(pieData);
        } catch (Exception e) {
            System.err.println("Error loading resolution type chart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadEscalationTrendsChart() {
        try {
            Map<String, Integer> trends = analyticsService.getEscalationsByHour();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Escalations");
            
            if (trends.isEmpty() || trends.values().stream().allMatch(v -> v == 0)) {
                // Dummy data - peak hours
                int[] dummyData = {1, 2, 3, 5, 8, 12, 15, 18, 14, 10, 7, 5, 4, 6, 9, 13, 16, 12, 8, 5, 3, 2, 1, 1};
                for (int i = 0; i < 24; i += 2) {
                    series.getData().add(new XYChart.Data<>(String.format("%02d:00", i), dummyData[i]));
                }
            } else {
                int count = 0;
                for (Map.Entry<String, Integer> entry : trends.entrySet()) {
                    if (count % 2 == 0) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    count++;
                }
            }
            
            escalationTrendsChart.getData().clear();
            escalationTrendsChart.getData().add(series);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadEscalationDetails() {
        try {
            ObservableList<EscalationRow> rows = FXCollections.observableArrayList();
            
            // Load real escalation data from SQL database
            List<AnalyticsService.EscalationDetail> details = analyticsService.getEscalationDetails();
            
            for (AnalyticsService.EscalationDetail detail : details) {
                rows.add(new EscalationRow(
                    detail.getTicketId(),
                    detail.getUserName(),
                    detail.getProductName(),
                    detail.getEscalatedAt(),
                    detail.getMessageCount(),
                    detail.getPriority(),
                    getTicketStatus(detail.getTicketId()),
                    detail.getConversationHistory()
                ));
            }
            
            escalationDetailsTable.setItems(rows);
            System.out.println("✓ Loaded " + rows.size() + " escalation details from database");
            
        } catch (Exception e) {
            System.err.println("Error loading escalation details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get ticket status from database
     */
    private String getTicketStatus(int ticketId) {
        try {
            return ticketDAO.findById(ticketId).getStatus();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
    
    private List<EscalationRow> createDummyEscalations() {
        List<EscalationRow> dummy = new ArrayList<>();
        
        // Escalation 1
        dummy.add(new EscalationRow(
            101,
            "John Smith",
            "Ruckus R650 WiFi AP",
            "Dec 15, 14:23",
            12,
            "HIGH",
            "IN_PROGRESS",
            createDummyMessages("Ruckus R650", "John", "WiFi keeps dropping connection")
        ));
        
        // Escalation 2
        dummy.add(new EscalationRow(
            102,
            "Sarah Johnson",
            "Cisco 2960 Switch",
            "Dec 15, 13:45",
            8,
            "MEDIUM",
            "OPEN",
            createDummyMessages("Cisco Switch", "Sarah", "Port configuration not working")
        ));
        
        // Escalation 3
        dummy.add(new EscalationRow(
            103,
            "Mike Williams",
            "TP-Link AX3000",
            "Dec 15, 12:30",
            15,
            "CRITICAL",
            "IN_PROGRESS",
            createDummyMessages("TP-Link Router", "Mike", "Cannot access admin panel")
        ));
        
        // Escalation 4
        dummy.add(new EscalationRow(
            104,
            "Emily Davis",
            "Dell PowerEdge R740",
            "Dec 15, 11:15",
            10,
            "HIGH",
            "RESOLVED",
            createDummyMessages("Dell Server", "Emily", "Server not booting properly")
        ));
        
        // Escalation 5
        dummy.add(new EscalationRow(
            105,
            "David Brown",
            "HP LaserJet Pro",
            "Dec 15, 10:00",
            6,
            "LOW",
            "OPEN",
            createDummyMessages("HP Printer", "David", "Print quality issues")
        ));
        
        return dummy;
    }
    
    private List<Message> createDummyMessages(String product, String userName, String issue) {
        List<Message> messages = new ArrayList<>();
        
        Message m1 = new Message();
        m1.setSenderType("USER");
        m1.setContent("Hi, I'm having issues with my " + product + ". " + issue + ".");
        m1.setTimestamp(LocalDateTime.now().minusMinutes(30));
        messages.add(m1);
        
        Message m2 = new Message();
        m2.setSenderType("BOT");
        m2.setContent("Hello " + userName + "! I'm here to help with your " + product + ". Let me check the common solutions for this issue.");
        m2.setTimestamp(LocalDateTime.now().minusMinutes(29));
        messages.add(m2);
        
        Message m3 = new Message();
        m3.setSenderType("USER");
        m3.setContent("I've already tried resetting it multiple times but the problem persists. This is urgent!");
        m3.setTimestamp(LocalDateTime.now().minusMinutes(27));
        messages.add(m3);
        
        Message m4 = new Message();
        m4.setSenderType("BOT");
        m4.setContent("I understand this is frustrating. Based on your description, this might require advanced troubleshooting. Let me escalate this to our technical support team.");
        m4.setTimestamp(LocalDateTime.now().minusMinutes(26));
        messages.add(m4);
        
        Message m5 = new Message();
        m5.setSenderType("SYSTEM");
        m5.setContent("Escalation requested. Connecting to live support agent...");
        m5.setTimestamp(LocalDateTime.now().minusMinutes(25));
        messages.add(m5);
        
        return messages;
    }
    
    private void showConversationDialog(EscalationRow row) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Ticket #" + row.getTicketId() + " - Conversation History");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f5f7fa;");
        
        // Header
        Text headerLabel = new Text("User: " + row.getUserName() + " | Product: " + row.getProductName());
        headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #333;");
        
        Text infoLabel = new Text("Escalated at: " + row.getEscalatedAt() + " | Priority: " + row.getPriority() + " | Status: " + row.getStatus());
        infoLabel.setStyle("-fx-font-size: 12px; -fx-fill: #666;");
        
        Text descLabel = new Text("💬 AI Bot Conversation Before Escalation:");
        descLabel.setStyle("-fx-font-size: 12px; -fx-fill: #444; -fx-font-style: italic;");
        
        // Messages
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));
        messagesBox.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        
        for (Message msg : row.getConversationHistory()) {
            VBox messageBox = new VBox(5);
            messageBox.setPadding(new Insets(10));
            
            String senderType = msg.getSenderType();
            String bgColor = "#e3f2fd";
            String textColor = "#1565c0";
            String icon = "🤖";
            
            if ("USER".equals(senderType)) {
                bgColor = "#f3e5f5";
                textColor = "#6a1b9a";
                icon = "👤";
            } else if ("AGENT".equals(senderType)) {
                bgColor = "#e8f5e9";
                textColor = "#2e7d32";
                icon = "👨‍💼";
            } else if ("SYSTEM".equals(senderType)) {
                bgColor = "#fff3e0";
                textColor = "#e65100";
                icon = "⚙️";
            }
            
            messageBox.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 8;");
            
            Label senderLabel = new Label(icon + " " + senderType);
            senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: " + textColor + ";");
            
            Label contentLabel = new Label(msg.getContent());
            contentLabel.setWrapText(true);
            contentLabel.setMaxWidth(520);
            contentLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 13px;");
            
            String timestamp = msg.getTimestamp() != null ? 
                    msg.getTimestamp().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")) : 
                    "Just now";
            Label timeLabel = new Label("🕐 " + timestamp);
            timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
            
            messageBox.getChildren().addAll(senderLabel, contentLabel, timeLabel);
            messagesBox.getChildren().add(messageBox);
        }
        
        scrollPane.setContent(messagesBox);
        
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("secondary-button");
        closeButton.setOnAction(e -> dialog.close());
        
        content.getChildren().addAll(headerLabel, infoLabel, descLabel, scrollPane, closeButton);
        
        Scene scene = new Scene(content, 600, 550);
        scene.getStylesheets().add(getClass().getResource("/com/care/styles/main.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    // Inner class for table rows
    public static class EscalationRow {
        private final int ticketId;
        private final String userName;
        private final String productName;
        private final String escalatedAt;
        private final int messageCount;
        private final String priority;
        private final String status;
        private final List<Message> conversationHistory;
        
        public EscalationRow(int ticketId, String userName, String productName, String escalatedAt,
                           int messageCount, String priority, String status, List<Message> conversationHistory) {
            this.ticketId = ticketId;
            this.userName = userName;
            this.productName = productName;
            this.escalatedAt = escalatedAt;
            this.messageCount = messageCount;
            this.priority = priority;
            this.status = status;
            this.conversationHistory = conversationHistory;
        }
        
        public int getTicketId() { return ticketId; }
        public String getUserName() { return userName; }
        public String getProductName() { return productName; }
        public String getEscalatedAt() { return escalatedAt; }
        public int getMessageCount() { return messageCount; }
        public String getPriority() { return priority; }
        public String getStatus() { return status; }
        public List<Message> getConversationHistory() { return conversationHistory; }
    }
}
