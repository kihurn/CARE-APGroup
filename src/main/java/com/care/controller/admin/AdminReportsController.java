package com.care.controller.admin;

import com.care.model.Message;
import com.care.service.AnalyticsService;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * Controller for Admin Reports and Analytics
 */
public class AdminReportsController {
    
    @FXML private Text totalChatsText;
    @FXML private Text escalationRateText;
    @FXML private Text resolvedTicketsText;
    @FXML private Text avgResponseTimeText;
    
    @FXML private BarChart<String, Number> productEscalationChart;
    @FXML private PieChart ticketStatusChart;
    @FXML private LineChart<String, Number> sessionsTimeChart;
    @FXML private BarChart<String, Number> resolutionTypeChart;
    @FXML private LineChart<String, Number> escalationTrendsChart;
    
    @FXML private Text satisfactionScoreText;
    @FXML private Text satisfactionDetailsText;
    
    @FXML private TableView<AnalyticsService.IssueData> topIssuesTable;
    @FXML private TableColumn<AnalyticsService.IssueData, String> keywordCol;
    @FXML private TableColumn<AnalyticsService.IssueData, String> productNameCol;
    @FXML private TableColumn<AnalyticsService.IssueData, Integer> occurrencesCol;
    @FXML private TableColumn<AnalyticsService.IssueData, Integer> escalationCountCol;
    @FXML private TableColumn<AnalyticsService.IssueData, String> issueRateCol;
    
    @FXML private TableView<AnalyticsService.AgentPerformance> agentPerformanceTable;
    @FXML private TableColumn<AnalyticsService.AgentPerformance, String> agentNameCol;
    @FXML private TableColumn<AnalyticsService.AgentPerformance, Integer> assignedTicketsCol;
    @FXML private TableColumn<AnalyticsService.AgentPerformance, Integer> resolvedTicketsCol;
    @FXML private TableColumn<AnalyticsService.AgentPerformance, String> resolutionRateCol;
    @FXML private TableColumn<AnalyticsService.AgentPerformance, String> avgResolutionTimeCol;
    
    @FXML private TableView<AnalyticsService.EscalationDetail> escalationDetailsTable;
    @FXML private TableColumn<AnalyticsService.EscalationDetail, Integer> escalationTicketIdCol;
    @FXML private TableColumn<AnalyticsService.EscalationDetail, String> escalationUserCol;
    @FXML private TableColumn<AnalyticsService.EscalationDetail, String> escalationProductCol;
    @FXML private TableColumn<AnalyticsService.EscalationDetail, String> escalationDateCol;
    @FXML private TableColumn<AnalyticsService.EscalationDetail, Integer> escalationMessagesCol;
    @FXML private TableColumn<AnalyticsService.EscalationDetail, String> escalationPriorityCol;
    @FXML private TableColumn<AnalyticsService.EscalationDetail, Void> escalationActionsCol;
    
    private AnalyticsService analyticsService;
    
    public AdminReportsController() {
        this.analyticsService = new AnalyticsService();
    }
    
    @FXML
    private void initialize() {
        System.out.println("Initializing AdminReportsController...");
        setupTables();
        loadAnalytics();
    }
    
    private void setupTables() {
        // Top Issues Table
        keywordCol.setCellValueFactory(new PropertyValueFactory<>("keyword"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        occurrencesCol.setCellValueFactory(new PropertyValueFactory<>("occurrences"));
        escalationCountCol.setCellValueFactory(new PropertyValueFactory<>("escalations"));
        issueRateCol.setCellValueFactory(new PropertyValueFactory<>("formattedIssueRate"));
        
        // Agent Performance Table
        agentNameCol.setCellValueFactory(new PropertyValueFactory<>("agentName"));
        assignedTicketsCol.setCellValueFactory(new PropertyValueFactory<>("assignedTickets"));
        resolvedTicketsCol.setCellValueFactory(new PropertyValueFactory<>("resolvedTickets"));
        resolutionRateCol.setCellValueFactory(new PropertyValueFactory<>("formattedResolutionRate"));
        avgResolutionTimeCol.setCellValueFactory(new PropertyValueFactory<>("formattedAvgResolutionTime"));
        
        // Escalation Details Table
        escalationTicketIdCol.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        escalationUserCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        escalationProductCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        escalationDateCol.setCellValueFactory(new PropertyValueFactory<>("escalatedAt"));
        escalationMessagesCol.setCellValueFactory(new PropertyValueFactory<>("messageCount"));
        escalationPriorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        
        // Add "View Chat" button to actions column
        escalationActionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("💬 View Chat");
            
            {
                viewButton.getStyleClass().add("primary-button");
                viewButton.setOnAction(event -> {
                    AnalyticsService.EscalationDetail detail = getTableView().getItems().get(getIndex());
                    showConversationDialog(detail);
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
        System.out.println("Refreshing analytics...");
        loadAnalytics();
    }
    
    @FXML
    private void handleExport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Report");
        alert.setHeaderText("Export Feature");
        alert.setContentText("Export functionality would generate a PDF or CSV report with all analytics data.\n\n" +
                           "This is a placeholder - full implementation would use libraries like Apache PDFBox or OpenCSV.");
        alert.showAndWait();
    }
    
    private void loadAnalytics() {
        try {
            // Load key metrics
            loadKeyMetrics();
            
            // Load charts
            loadProductEscalationChart();
            loadTicketStatusChart();
            loadSessionsTimeChart();
            loadResolutionTypeChart();
            loadEscalationTrendsChart();
            
            // Load satisfaction score
            loadSatisfactionScore();
            
            // Load tables
            loadTopIssues();
            loadAgentPerformance();
            loadEscalationDetails();
            
            System.out.println("✓ Analytics loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading analytics");
            e.printStackTrace();
        }
    }
    
    private void loadKeyMetrics() {
        int totalChats = analyticsService.getTotalChatSessions();
        totalChatsText.setText(String.valueOf(totalChats));
        
        double escalationRate = analyticsService.getEscalationRate();
        escalationRateText.setText(String.format("%.1f%%", escalationRate));
        
        int resolvedTickets = analyticsService.getResolvedTicketsCount();
        resolvedTicketsText.setText(String.valueOf(resolvedTickets));
        
        double avgResponseTime = analyticsService.getAverageResponseTime();
        avgResponseTimeText.setText(String.format("%.1f min", avgResponseTime));
    }
    
    private void loadProductEscalationChart() {
        Map<String, Integer> escalationsByProduct = analyticsService.getEscalationsByProduct();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Escalations");
        
        for (Map.Entry<String, Integer> entry : escalationsByProduct.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        productEscalationChart.getData().clear();
        productEscalationChart.getData().add(series);
        
        // Style the chart
        productEscalationChart.setLegendVisible(false);
    }
    
    private void loadTicketStatusChart() {
        Map<String, Integer> statusDistribution = analyticsService.getTicketStatusDistribution();
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Integer> entry : statusDistribution.entrySet()) {
            if (entry.getValue() > 0) { // Only show non-zero values
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }
        
        ticketStatusChart.setData(pieChartData);
        
        // Add color styling to pie chart slices
        for (PieChart.Data data : pieChartData) {
            String status = data.getName();
            String color = getColorForStatus(status);
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        }
    }
    
    private void loadSessionsTimeChart() {
        Map<String, Integer> sessionsByDate = analyticsService.getSessionsByDate();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sessions");
        
        for (Map.Entry<String, Integer> entry : sessionsByDate.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        sessionsTimeChart.getData().clear();
        sessionsTimeChart.getData().add(series);
        sessionsTimeChart.setLegendVisible(false);
    }
    
    private void loadTopIssues() {
        List<AnalyticsService.IssueData> issues = analyticsService.getTopIssues();
        topIssuesTable.getItems().clear();
        topIssuesTable.getItems().addAll(issues);
        
        System.out.println("✓ Loaded " + issues.size() + " top issues");
    }
    
    private void loadAgentPerformance() {
        List<AnalyticsService.AgentPerformance> performances = analyticsService.getAgentPerformance();
        agentPerformanceTable.getItems().clear();
        agentPerformanceTable.getItems().addAll(performances);
        
        System.out.println("✓ Loaded " + performances.size() + " agent performance records");
    }
    
    private String getColorForStatus(String status) {
        switch (status) {
            case "OPEN":
                return "#dc3545"; // Red
            case "IN_PROGRESS":
                return "#007bff"; // Blue
            case "RESOLVED":
                return "#28a745"; // Green
            case "CLOSED":
                return "#6c757d"; // Gray
            default:
                return "#667eea"; // Purple (default)
        }
    }
    
    private void loadResolutionTypeChart() {
        Map<String, Double> resolutionTypes = analyticsService.getResolutionTypeDistribution();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Resolution Type");
        
        for (Map.Entry<String, Double> entry : resolutionTypes.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        resolutionTypeChart.getData().clear();
        resolutionTypeChart.getData().add(series);
        resolutionTypeChart.setLegendVisible(true);
    }
    
    private void loadEscalationTrendsChart() {
        Map<String, Integer> escalationsByHour = analyticsService.getEscalationsByHour();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Escalations");
        
        // Only show every 2 hours for readability
        int count = 0;
        for (Map.Entry<String, Integer> entry : escalationsByHour.entrySet()) {
            if (count % 2 == 0 || entry.getValue() > 0) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            count++;
        }
        
        escalationTrendsChart.getData().clear();
        escalationTrendsChart.getData().add(series);
        escalationTrendsChart.setLegendVisible(false);
    }
    
    private void loadSatisfactionScore() {
        double score = analyticsService.getUserSatisfactionScore();
        satisfactionScoreText.setText(String.format("%.1f", score));
        
        int totalSessions = analyticsService.getTotalChatSessions();
        satisfactionDetailsText.setText("Based on " + totalSessions + " chat sessions (simulated data)");
    }
    
    private void loadEscalationDetails() {
        List<AnalyticsService.EscalationDetail> details = analyticsService.getEscalationDetails();
        escalationDetailsTable.getItems().clear();
        escalationDetailsTable.getItems().addAll(details);
        
        System.out.println("✓ Loaded " + details.size() + " escalation details");
    }
    
    private void showConversationDialog(AnalyticsService.EscalationDetail detail) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Escalation #" + detail.getTicketId() + " - Conversation History");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f5f7fa;");
        
        // Header info
        Label headerLabel = new Label("User: " + detail.getUserName() + " | Product: " + detail.getProductName());
        headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label infoLabel = new Label("Escalated at: " + detail.getEscalatedAt() + " | Priority: " + detail.getPriority());
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label descLabel = new Label("💬 This is what the user experienced with the AI bot before escalating:");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #444; -fx-font-style: italic;");
        
        // Conversation area
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));
        messagesBox.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        
        List<Message> messages = detail.getConversationHistory();
        if (messages.isEmpty()) {
            Label emptyLabel = new Label("No messages found in this conversation.");
            emptyLabel.setStyle("-fx-text-fill: #999;");
            messagesBox.getChildren().add(emptyLabel);
        } else {
            for (Message msg : messages) {
                VBox messageBox = new VBox(5);
                messageBox.setPadding(new Insets(10));
                
                String senderType = msg.getSenderType();
                String bgColor = "#e3f2fd"; // Default blue (BOT)
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
                        msg.getTimestamp().toString() : "Unknown time";
                Label timeLabel = new Label("🕐 " + timestamp);
                timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
                
                messageBox.getChildren().addAll(senderLabel, contentLabel, timeLabel);
                messagesBox.getChildren().add(messageBox);
            }
        }
        
        scrollPane.setContent(messagesBox);
        
        // Close button
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("secondary-button");
        closeButton.setOnAction(e -> dialog.close());
        
        content.getChildren().addAll(headerLabel, infoLabel, descLabel, scrollPane, closeButton);
        
        Scene scene = new Scene(content, 600, 550);
        scene.getStylesheets().add(getClass().getResource("/com/care/styles/main.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

