package com.care.controller.admin;

import com.care.service.AnalyticsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

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
            
            // Load tables
            loadTopIssues();
            loadAgentPerformance();
            
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
}

