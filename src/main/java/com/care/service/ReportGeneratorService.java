package com.care.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service for generating PDF reports with analytics data from SQL database
 */
public class ReportGeneratorService {
    
    private AnalyticsService analyticsService;
    
    public ReportGeneratorService() {
        this.analyticsService = new AnalyticsService();
    }
    
    /**
     * Generate a comprehensive PDF report with all analytics
     * 
     * @param outputPath Path where the PDF should be saved
     * @return true if successful, false otherwise
     */
    public boolean generateAnalyticsReport(String outputPath) {
        PDDocument document = new PDDocument();
        
        try {
            // Create title page
            PDPage titlePage = new PDPage(PDRectangle.A4);
            document.addPage(titlePage);
            createTitlePage(document, titlePage);
            
            // Create metrics page with charts
            PDPage metricsPage = new PDPage(PDRectangle.A4);
            document.addPage(metricsPage);
            createMetricsPageWithCharts(document, metricsPage);
            
            // Create escalations page
            PDPage escalationsPage = new PDPage(PDRectangle.A4);
            document.addPage(escalationsPage);
            createEscalationsPage(document, escalationsPage);
            
            // Save the document
            document.save(outputPath);
            document.close();
            
            System.out.println("✓ PDF report generated: " + outputPath);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error generating PDF report");
            e.printStackTrace();
            try {
                document.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }
    
    /**
     * Create a bar chart for product escalations
     */
    private BufferedImage createProductEscalationChart(Map<String, Integer> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        if (data.isEmpty()) {
            dataset.addValue(0, "Escalations", "No Data");
        } else {
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                dataset.addValue(entry.getValue(), "Escalations", entry.getKey());
            }
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Escalations by Product",
            "Product",
            "Count",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );
        
        // Customize chart appearance
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(240, 240, 240));
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        plot.getRenderer().setSeriesPaint(0, new Color(102, 126, 234)); // Purple-blue
        
        // Render to image
        return chart.createBufferedImage(500, 300);
    }
    
    /**
     * Create a pie chart for resolution types
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private BufferedImage createResolutionTypeChart(Map<String, Double> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        if (data.isEmpty() || data.values().stream().allMatch(v -> v == 0.0)) {
            dataset.setValue("No Data", 100);
        } else {
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Resolution Type Distribution",
            dataset,
            true,
            true,
            false
        );
        
        // Customize chart appearance
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setSectionPaint("AI Resolved", new Color(67, 233, 123)); // Green
        plot.setSectionPaint("Escalated to Human", new Color(250, 112, 154)); // Pink
        plot.setSectionPaint("No Data", new Color(200, 200, 200));
        
        // Render to image
        return chart.createBufferedImage(500, 300);
    }
    
    /**
     * Convert BufferedImage to PDImageXObject for embedding in PDF
     */
    private PDImageXObject convertToPDImage(PDDocument document, BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return PDImageXObject.createFromByteArray(document, baos.toByteArray(), "chart");
    }
    
    private void createTitlePage(PDDocument document, PDPage page) throws IOException {
        PDPageContentStream content = new PDPageContentStream(document, page);
        
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();
        
        // Title
        content.setFont(PDType1Font.HELVETICA_BOLD, 32);
        String title = "CARE Analytics Report";
        float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * 32;
        content.beginText();
        content.newLineAtOffset((pageWidth - titleWidth) / 2, pageHeight - 200);
        content.showText(title);
        content.endText();
        
        // Subtitle
        content.setFont(PDType1Font.HELVETICA, 16);
        String subtitle = "Customer Assistance and Resource Engine";
        float subtitleWidth = PDType1Font.HELVETICA.getStringWidth(subtitle) / 1000 * 16;
        content.beginText();
        content.newLineAtOffset((pageWidth - subtitleWidth) / 2, pageHeight - 240);
        content.showText(subtitle);
        content.endText();
        
        // Date
        content.setFont(PDType1Font.HELVETICA, 12);
        String dateStr = "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        float dateWidth = PDType1Font.HELVETICA.getStringWidth(dateStr) / 1000 * 12;
        content.beginText();
        content.newLineAtOffset((pageWidth - dateWidth) / 2, pageHeight - 280);
        content.showText(dateStr);
        content.endText();
        
        // Decorative line
        content.setLineWidth(2);
        content.moveTo(100, pageHeight - 320);
        content.lineTo(pageWidth - 100, pageHeight - 320);
        content.stroke();
        
        content.close();
    }
    
    private void createMetricsPageWithCharts(PDDocument document, PDPage page) throws IOException {
        PDPageContentStream content = new PDPageContentStream(document, page);
        
        float margin = 50;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float pageWidth = page.getMediaBox().getWidth();
        
        // Page title
        content.setFont(PDType1Font.HELVETICA_BOLD, 20);
        content.beginText();
        content.newLineAtOffset(margin, yPosition);
        content.showText("Key Performance Metrics");
        content.endText();
        yPosition -= 35;
        
        // Get analytics data from database
        int totalChats = analyticsService.getTotalChatSessions();
        double escalationRate = analyticsService.getEscalationRate();
        int resolvedTickets = analyticsService.getResolvedTicketsCount();
        double avgResponseTime = analyticsService.getAverageResponseTime();
        double satisfaction = analyticsService.getUserSatisfactionScore();
        
        // Display metrics in a compact format
        content.setFont(PDType1Font.HELVETICA, 11);
        String[] metrics = {
            String.format("Total Chats: %d | Escalation Rate: %.1f%% | Resolved: %d", 
                totalChats, escalationRate, resolvedTickets),
            String.format("Avg Response Time: %.1f min | Satisfaction: %.1f/5.0", 
                avgResponseTime, satisfaction)
        };
        
        for (String metric : metrics) {
            content.beginText();
            content.newLineAtOffset(margin, yPosition);
            content.showText(metric);
            content.endText();
            yPosition -= 18;
        }
        
        yPosition -= 15;
        
        // Add Product Escalation Chart
        try {
            Map<String, Integer> productData = analyticsService.getEscalationsByProduct();
            BufferedImage chartImage = createProductEscalationChart(productData);
            PDImageXObject pdImage = convertToPDImage(document, chartImage);
            
            float chartWidth = 480;
            float chartHeight = 250;
            content.drawImage(pdImage, margin, yPosition - chartHeight, chartWidth, chartHeight);
            yPosition -= chartHeight + 20;
        } catch (Exception e) {
            System.err.println("Error adding product escalation chart: " + e.getMessage());
            content.setFont(PDType1Font.HELVETICA, 10);
            content.beginText();
            content.newLineAtOffset(margin, yPosition);
            content.showText("[Chart generation failed]");
            content.endText();
            yPosition -= 20;
        }
        
        // Add Resolution Type Chart
        try {
            Map<String, Double> resolutionData = analyticsService.getResolutionTypeDistribution();
            BufferedImage chartImage = createResolutionTypeChart(resolutionData);
            PDImageXObject pdImage = convertToPDImage(document, chartImage);
            
            float chartWidth = 400;
            float chartHeight = 200;
            float xOffset = (pageWidth - chartWidth) / 2; // Center the pie chart
            content.drawImage(pdImage, xOffset, yPosition - chartHeight, chartWidth, chartHeight);
            yPosition -= chartHeight;
        } catch (Exception e) {
            System.err.println("Error adding resolution type chart: " + e.getMessage());
            content.setFont(PDType1Font.HELVETICA, 10);
            content.beginText();
            content.newLineAtOffset(margin, yPosition);
            content.showText("[Chart generation failed]");
            content.endText();
        }
        
        // Footer
        content.setFont(PDType1Font.HELVETICA, 10);
        content.beginText();
        content.newLineAtOffset(margin, 50);
        content.showText("Page 1 of 2 - CARE Analytics Report");
        content.endText();
        
        content.close();
    }
    
    @Deprecated
    private void createMetricsPage(PDDocument document, PDPage page) throws IOException {
        PDPageContentStream content = new PDPageContentStream(document, page);
        
        float margin = 50;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float lineHeight = 20;
        
        // Page title
        content.setFont(PDType1Font.HELVETICA_BOLD, 20);
        content.beginText();
        content.newLineAtOffset(margin, yPosition);
        content.showText("Key Performance Metrics");
        content.endText();
        yPosition -= 40;
        
        // Get real analytics data from database
        int totalChats = analyticsService.getTotalChatSessions();
        double escalationRate = analyticsService.getEscalationRate();
        int resolvedTickets = analyticsService.getResolvedTicketsCount();
        double avgResponseTime = analyticsService.getAverageResponseTime();
        double satisfaction = analyticsService.getUserSatisfactionScore();
        
        content.setFont(PDType1Font.HELVETICA_BOLD, 14);
        
        // Total Chats
        content.beginText();
        content.newLineAtOffset(margin, yPosition);
        content.showText("Total Chat Sessions: " + totalChats);
        content.endText();
        yPosition -= lineHeight + 10;
        
        // Escalation Rate
        content.beginText();
        content.newLineAtOffset(margin, yPosition);
        content.showText(String.format("Escalation Rate: %.1f%%", escalationRate));
        content.endText();
        yPosition -= lineHeight + 10;
        
        // Resolved Tickets
        content.beginText();
        content.newLineAtOffset(margin, yPosition);
        content.showText("Resolved Tickets: " + resolvedTickets);
        content.endText();
        yPosition -= lineHeight + 10;
        
        // Average Response Time
        content.beginText();
        content.newLineAtOffset(margin, yPosition);
        content.showText(String.format("Avg Response Time: %.1f minutes", avgResponseTime));
        content.endText();
        yPosition -= lineHeight + 10;
        
        // User Satisfaction
        content.beginText();
        content.newLineAtOffset(margin, yPosition);
        content.showText(String.format("User Satisfaction: %.1f / 5.0", satisfaction));
        content.endText();
        yPosition -= lineHeight + 30;
        
        // Section: Product Escalations
        content.setFont(PDType1Font.HELVETICA_BOLD, 16);
        content.beginText();
        content.newLineAtOffset(margin, yPosition);
        content.showText("Escalations by Product");
        content.endText();
        yPosition -= lineHeight + 10;
        
        content.setFont(PDType1Font.HELVETICA, 12);
        
        Map<String, Integer> productEscalations = analyticsService.getEscalationsByProduct();
        
        if (productEscalations.isEmpty()) {
            content.beginText();
            content.newLineAtOffset(margin + 20, yPosition);
            content.showText("• No escalations recorded yet");
            content.endText();
            yPosition -= lineHeight;
        } else {
            for (Map.Entry<String, Integer> entry : productEscalations.entrySet()) {
                content.beginText();
                content.newLineAtOffset(margin + 20, yPosition);
                content.showText("• " + entry.getKey() + ": " + entry.getValue() + " escalations");
                content.endText();
                yPosition -= lineHeight;
            }
        }
        
        yPosition -= 20;
        
        // Resolution Type Distribution
        content.setFont(PDType1Font.HELVETICA_BOLD, 16);
        content.beginText();
        content.newLineAtOffset(margin, yPosition);
        content.showText("Resolution Type Distribution");
        content.endText();
        yPosition -= lineHeight + 10;
        
        content.setFont(PDType1Font.HELVETICA, 12);
        
        Map<String, Double> resolutionTypes = analyticsService.getResolutionTypeDistribution();
        
        if (resolutionTypes.isEmpty() || resolutionTypes.values().stream().allMatch(v -> v == 0.0)) {
            content.beginText();
            content.newLineAtOffset(margin + 20, yPosition);
            content.showText("• No resolution data available yet");
            content.endText();
            yPosition -= lineHeight;
        } else {
            for (Map.Entry<String, Double> entry : resolutionTypes.entrySet()) {
                content.beginText();
                content.newLineAtOffset(margin + 20, yPosition);
                content.showText(String.format("• %s: %.1f%%", entry.getKey(), entry.getValue()));
                content.endText();
                yPosition -= lineHeight;
            }
        }
        
        // Footer
        content.setFont(PDType1Font.HELVETICA, 10);
        content.beginText();
        content.newLineAtOffset(margin, 50);
        content.showText("Page 1 of 3 - CARE Analytics Report");
        content.endText();
        
        content.close();
    }
    
    private void createEscalationsPage(PDDocument document, PDPage page) throws IOException {
        PDPageContentStream content = new PDPageContentStream(document, page);
        
        float margin = 50;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float lineHeight = 18;
        
        // Page title
        content.setFont(PDType1Font.HELVETICA_BOLD, 20);
        content.beginText();
        content.newLineAtOffset(margin, yPosition);
        content.showText("Recent Escalations");
        content.endText();
        yPosition -= 40;
        
        // Get real escalation data from database
        List<AnalyticsService.EscalationDetail> escalationDetails = analyticsService.getEscalationDetails();
        
        if (escalationDetails.isEmpty()) {
            content.setFont(PDType1Font.HELVETICA, 12);
            content.beginText();
            content.newLineAtOffset(margin, yPosition);
            content.showText("No escalations recorded in the system yet.");
            content.endText();
            yPosition -= lineHeight + 20;
        } else {
            content.setFont(PDType1Font.HELVETICA, 11);
            
            // Limit to top 10 escalations for the PDF
            int count = 0;
            for (AnalyticsService.EscalationDetail detail : escalationDetails) {
                if (count >= 10 || yPosition < 150) break; // Stop if page is full
                
                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                content.beginText();
                content.newLineAtOffset(margin, yPosition);
                content.showText("Ticket #" + detail.getTicketId() + " - Priority: " + detail.getPriority());
                content.endText();
                yPosition -= lineHeight;
                
                content.setFont(PDType1Font.HELVETICA, 11);
                content.beginText();
                content.newLineAtOffset(margin + 20, yPosition);
                content.showText("User: " + detail.getUserName());
                content.endText();
                yPosition -= lineHeight;
                
                content.beginText();
                content.newLineAtOffset(margin + 20, yPosition);
                content.showText("Product: " + detail.getProductName());
                content.endText();
                yPosition -= lineHeight;
                
                content.beginText();
                content.newLineAtOffset(margin + 20, yPosition);
                content.showText("Escalated: " + detail.getEscalatedAt());
                content.endText();
                yPosition -= lineHeight;
                
                content.beginText();
                content.newLineAtOffset(margin + 20, yPosition);
                content.showText("Messages: " + detail.getMessageCount());
                content.endText();
                yPosition -= lineHeight + 15;
                
                count++;
            }
        }
        
        // Summary section
        if (yPosition > 200) {
            yPosition -= 10;
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.beginText();
            content.newLineAtOffset(margin, yPosition);
            content.showText("Summary");
            content.endText();
            yPosition -= lineHeight + 10;
            
            content.setFont(PDType1Font.HELVETICA, 11);
            
            // Total escalations
            int totalEscalations = escalationDetails.size();
            content.beginText();
            content.newLineAtOffset(margin + 20, yPosition);
            content.showText("• Total escalations: " + totalEscalations);
            content.endText();
            yPosition -= lineHeight;
            
            // Average messages
            if (!escalationDetails.isEmpty()) {
                double avgMessages = escalationDetails.stream()
                    .mapToInt(AnalyticsService.EscalationDetail::getMessageCount)
                    .average()
                    .orElse(0.0);
                
                content.beginText();
                content.newLineAtOffset(margin + 20, yPosition);
                content.showText(String.format("• Average messages before escalation: %.1f", avgMessages));
                content.endText();
                yPosition -= lineHeight;
            }
            
            // Top products with escalations
            Map<String, Integer> productEscalations = analyticsService.getEscalationsByProduct();
            if (!productEscalations.isEmpty()) {
                String topProduct = productEscalations.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
                
                content.beginText();
                content.newLineAtOffset(margin + 20, yPosition);
                content.showText("• Most escalated product: " + topProduct);
                content.endText();
            }
        }
        
        // Footer
        content.setFont(PDType1Font.HELVETICA, 10);
        content.beginText();
        content.newLineAtOffset(margin, 50);
        content.showText("Page 2 of 2 - CARE Analytics Report");
        content.endText();
        
        content.close();
    }
}

