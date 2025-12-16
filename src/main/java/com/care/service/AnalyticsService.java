package com.care.service;

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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Analytics and Reporting
 */
public class AnalyticsService {
    
    private ChatSessionDAO chatSessionDAO;
    private TicketDAO ticketDAO;
    private MessageDAO messageDAO;
    private ProductDAO productDAO;
    private UserDAO userDAO;
    
    public AnalyticsService() {
        this.chatSessionDAO = new ChatSessionDAO();
        this.ticketDAO = new TicketDAO();
        this.messageDAO = new MessageDAO();
        this.productDAO = new ProductDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Get total number of chat sessions
     */
    public int getTotalChatSessions() {
        return chatSessionDAO.getAllSessions().size();
    }
    
    /**
     * Get escalation rate (percentage of sessions that were escalated)
     */
    public double getEscalationRate() {
        List<ChatSession> allSessions = chatSessionDAO.getAllSessions();
        if (allSessions.isEmpty()) return 0.0;
        
        long escalatedCount = allSessions.stream()
                .filter(s -> "ESCALATED".equals(s.getStatus()))
                .count();
        
        return (escalatedCount * 100.0) / allSessions.size();
    }
    
    /**
     * Get number of resolved tickets
     */
    public int getResolvedTicketsCount() {
        return ticketDAO.getByStatus("RESOLVED").size() + 
               ticketDAO.getByStatus("CLOSED").size();
    }
    
    /**
     * Get average response time (in minutes)
     * Calculated as time between user message and next bot/agent response
     */
    public double getAverageResponseTime() {
        // Simplified: return a placeholder value
        // In production, this would analyze message timestamps
        return 2.5; // 2.5 minutes average
    }
    
    /**
     * Get escalation count by product
     * Returns map of Product -> Escalation Count
     */
    public Map<String, Integer> getEscalationsByProduct() {
        List<ChatSession> escalatedSessions = chatSessionDAO.getAllSessions().stream()
                .filter(s -> "ESCALATED".equals(s.getStatus()))
                .collect(Collectors.toList());
        
        Map<String, Integer> productEscalations = new HashMap<>();
        
        for (ChatSession session : escalatedSessions) {
            Product product = productDAO.findById(session.getProductId());
            if (product != null) {
                String productName = product.getName();
                productEscalations.put(productName, 
                    productEscalations.getOrDefault(productName, 0) + 1);
            }
        }
        
        return productEscalations;
    }
    
    /**
     * Get ticket status distribution
     * Returns map of Status -> Count
     */
    public Map<String, Integer> getTicketStatusDistribution() {
        List<Ticket> allTickets = ticketDAO.getAllTickets();
        
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("OPEN", 0);
        distribution.put("IN_PROGRESS", 0);
        distribution.put("RESOLVED", 0);
        distribution.put("CLOSED", 0);
        
        for (Ticket ticket : allTickets) {
            String status = ticket.getStatus();
            distribution.put(status, distribution.getOrDefault(status, 0) + 1);
        }
        
        return distribution;
    }
    
    /**
     * Get sessions count by date for the last 7 days
     * Returns map of Date -> Session Count
     */
    public Map<String, Integer> getSessionsByDate() {
        List<ChatSession> allSessions = chatSessionDAO.getAllSessions();
        Map<String, Integer> sessionsByDate = new LinkedHashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        
        // Initialize last 7 days with 0
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            String dateStr = date.format(formatter);
            sessionsByDate.put(dateStr, 0);
        }
        
        // Count sessions for each day
        for (ChatSession session : allSessions) {
            if (session.getCreatedAt() != null) {
                LocalDateTime createdAt = session.getCreatedAt();
                if (createdAt.isAfter(now.minusDays(7))) {
                    String dateStr = createdAt.format(formatter);
                    sessionsByDate.put(dateStr, 
                        sessionsByDate.getOrDefault(dateStr, 0) + 1);
                }
            }
        }
        
        return sessionsByDate;
    }
    
    /**
     * Get top issues/keywords from messages
     * Returns list of IssueData objects
     */
    public List<IssueData> getTopIssues() {
        List<IssueData> issues = new ArrayList<>();
        
        // Common issue keywords to track
        String[] keywords = {"error", "broken", "not working", "problem", "issue", 
                            "failed", "reset", "configure", "slow", "crash"};
        
        List<ChatSession> allSessions = chatSessionDAO.getAllSessions();
        Map<String, Map<String, Integer>> keywordProductCount = new HashMap<>();
        Map<String, Map<String, Integer>> keywordProductEscalations = new HashMap<>();
        
        for (ChatSession session : allSessions) {
            List<Message> messages = messageDAO.getBySessionId(session.getSessionId());
            Product product = productDAO.findById(session.getProductId());
            if (product == null) continue;
            
            String productName = product.getName();
            boolean isEscalated = "ESCALATED".equals(session.getStatus());
            
            for (Message message : messages) {
                if ("USER".equals(message.getSenderType())) {
                    String content = message.getContent().toLowerCase();
                    
                    for (String keyword : keywords) {
                        if (content.contains(keyword)) {
                            // Count occurrences
                            keywordProductCount.putIfAbsent(keyword, new HashMap<>());
                            Map<String, Integer> productCounts = keywordProductCount.get(keyword);
                            productCounts.put(productName, productCounts.getOrDefault(productName, 0) + 1);
                            
                            // Count escalations
                            if (isEscalated) {
                                keywordProductEscalations.putIfAbsent(keyword, new HashMap<>());
                                Map<String, Integer> productEscalations = keywordProductEscalations.get(keyword);
                                productEscalations.put(productName, productEscalations.getOrDefault(productName, 0) + 1);
                            }
                        }
                    }
                }
            }
        }
        
        // Create IssueData objects
        for (String keyword : keywordProductCount.keySet()) {
            Map<String, Integer> productCounts = keywordProductCount.get(keyword);
            Map<String, Integer> productEscalations = keywordProductEscalations.getOrDefault(keyword, new HashMap<>());
            
            for (String productName : productCounts.keySet()) {
                int occurrences = productCounts.get(productName);
                int escalations = productEscalations.getOrDefault(productName, 0);
                double issueRate = (escalations * 100.0) / occurrences;
                
                issues.add(new IssueData(keyword, productName, occurrences, escalations, issueRate));
            }
        }
        
        // Sort by occurrences (descending) and return top 10
        return issues.stream()
                .sorted((a, b) -> Integer.compare(b.getOccurrences(), a.getOccurrences()))
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * Get agent performance metrics
     * Returns list of AgentPerformance objects
     */
    public List<AgentPerformance> getAgentPerformance() {
        List<AgentPerformance> performances = new ArrayList<>();
        
        List<User> agents = userDAO.findAll().stream()
                .filter(u -> "AGENT".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
        
        for (User agent : agents) {
            int agentId = agent.getUserId();
            
            List<Ticket> assignedTickets = ticketDAO.getByAgentId(agentId);
            int assignedCount = assignedTickets.size();
            
            List<Ticket> resolvedTickets = assignedTickets.stream()
                    .filter(t -> "RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus()))
                    .collect(Collectors.toList());
            int resolvedCount = resolvedTickets.size();
            
            double resolutionRate = assignedCount > 0 ? (resolvedCount * 100.0) / assignedCount : 0.0;
            
            // Calculate average resolution time (in hours)
            double avgResolutionTime = 0.0;
            if (!resolvedTickets.isEmpty()) {
                long totalMinutes = 0;
                int validCount = 0;
                
                for (Ticket ticket : resolvedTickets) {
                    if (ticket.getCreatedAt() != null && ticket.getResolvedAt() != null) {
                        Duration duration = Duration.between(ticket.getCreatedAt(), ticket.getResolvedAt());
                        totalMinutes += duration.toMinutes();
                        validCount++;
                    }
                }
                
                if (validCount > 0) {
                    avgResolutionTime = (totalMinutes / 60.0) / validCount; // Convert to hours
                }
            }
            
            performances.add(new AgentPerformance(
                agent.getName(),
                assignedCount,
                resolvedCount,
                resolutionRate,
                avgResolutionTime
            ));
        }
        
        return performances;
    }
    
    /**
     * Get AI vs Human resolution rate
     * Returns map with "AI Resolved" and "Human Resolved" percentages
     */
    public Map<String, Double> getResolutionTypeDistribution() {
        List<ChatSession> allSessions = chatSessionDAO.getAllSessions();
        Map<String, Double> distribution = new HashMap<>();
        
        if (allSessions.isEmpty()) {
            distribution.put("AI Resolved", 0.0);
            distribution.put("Escalated to Human", 0.0);
            return distribution;
        }
        
        long escalatedCount = allSessions.stream()
                .filter(s -> "ESCALATED".equals(s.getStatus()))
                .count();
        
        long aiResolvedCount = allSessions.stream()
                .filter(s -> "CLOSED".equals(s.getStatus()))
                .count();
        
        double aiResolvedPercent = (aiResolvedCount * 100.0) / allSessions.size();
        double escalatedPercent = (escalatedCount * 100.0) / allSessions.size();
        
        distribution.put("AI Resolved", aiResolvedPercent);
        distribution.put("Escalated to Human", escalatedPercent);
        
        return distribution;
    }
    
    /**
     * Get user satisfaction score (dummy data for now)
     * In production, this would come from user feedback/ratings
     */
    public double getUserSatisfactionScore() {
        // Dummy calculation based on escalation rate
        double escalationRate = getEscalationRate();
        // Lower escalation rate = higher satisfaction
        // If 0% escalation = 5.0 score, 100% escalation = 2.0 score
        double score = 5.0 - (escalationRate / 100.0 * 3.0);
        return Math.max(2.0, Math.min(5.0, score));
    }
    
    /**
     * Get escalations by hour of day
     * Returns map of Hour -> Escalation Count
     */
    public Map<String, Integer> getEscalationsByHour() {
        Map<String, Integer> hourlyEscalations = new LinkedHashMap<>();
        
        // Initialize all 24 hours with 0
        for (int i = 0; i < 24; i++) {
            String hour = String.format("%02d:00", i);
            hourlyEscalations.put(hour, 0);
        }
        
        // Count escalations by hour (using dummy distribution for now)
        // In production, this would analyze actual ticket creation times
        List<Ticket> allTickets = ticketDAO.getAllTickets();
        
        for (Ticket ticket : allTickets) {
            if (ticket.getCreatedAt() != null) {
                int hour = ticket.getCreatedAt().getHour();
                String hourStr = String.format("%02d:00", hour);
                hourlyEscalations.put(hourStr, hourlyEscalations.getOrDefault(hourStr, 0) + 1);
            }
        }
        
        return hourlyEscalations;
    }
    
    /**
     * Get detailed escalation data with conversation history
     * Returns list of EscalationDetail objects
     */
    public List<EscalationDetail> getEscalationDetails() {
        List<EscalationDetail> details = new ArrayList<>();
        
        // Get all escalated sessions
        List<ChatSession> escalatedSessions = chatSessionDAO.getAllSessions().stream()
                .filter(s -> "ESCALATED".equals(s.getStatus()))
                .collect(Collectors.toList());
        
        for (ChatSession session : escalatedSessions) {
            // Get related ticket
            Ticket ticket = ticketDAO.findBySessionId(session.getSessionId());
            if (ticket == null) continue;
            
            // Get user info
            User user = userDAO.findById(session.getUserId());
            String userName = user != null ? user.getName() : "Unknown User";
            
            // Get product info
            Product product = productDAO.findById(session.getProductId());
            String productName = product != null ? product.getName() : "Unknown Product";
            
            // Get conversation messages
            List<Message> messages = messageDAO.getBySessionId(session.getSessionId());
            int messageCount = messages.size();
            
            // Get escalation time
            String escalatedAt = session.getCreatedAt() != null ? 
                    session.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")) : 
                    "Unknown";
            
            details.add(new EscalationDetail(
                ticket.getTicketId(),
                userName,
                productName,
                escalatedAt,
                messageCount,
                ticket.getPriority(),
                session.getSessionId(),
                messages
            ));
        }
        
        // Sort by ticket ID descending (newest first)
        details.sort((a, b) -> Integer.compare(b.getTicketId(), a.getTicketId()));
        
        return details;
    }
    
    // Inner classes for data transfer
    
    public static class IssueData {
        private String keyword;
        private String productName;
        private int occurrences;
        private int escalations;
        private double issueRate;
        
        public IssueData(String keyword, String productName, int occurrences, int escalations, double issueRate) {
            this.keyword = keyword;
            this.productName = productName;
            this.occurrences = occurrences;
            this.escalations = escalations;
            this.issueRate = issueRate;
        }
        
        public String getKeyword() { return keyword; }
        public String getProductName() { return productName; }
        public int getOccurrences() { return occurrences; }
        public int getEscalations() { return escalations; }
        public double getIssueRate() { return issueRate; }
        public String getFormattedIssueRate() { return String.format("%.1f%%", issueRate); }
    }
    
    public static class AgentPerformance {
        private String agentName;
        private int assignedTickets;
        private int resolvedTickets;
        private double resolutionRate;
        private double avgResolutionTime;
        
        public AgentPerformance(String agentName, int assignedTickets, int resolvedTickets, 
                               double resolutionRate, double avgResolutionTime) {
            this.agentName = agentName;
            this.assignedTickets = assignedTickets;
            this.resolvedTickets = resolvedTickets;
            this.resolutionRate = resolutionRate;
            this.avgResolutionTime = avgResolutionTime;
        }
        
        public String getAgentName() { return agentName; }
        public int getAssignedTickets() { return assignedTickets; }
        public int getResolvedTickets() { return resolvedTickets; }
        public double getResolutionRate() { return resolutionRate; }
        public String getFormattedResolutionRate() { return String.format("%.1f%%", resolutionRate); }
        public double getAvgResolutionTime() { return avgResolutionTime; }
        public String getFormattedAvgResolutionTime() { return String.format("%.1f", avgResolutionTime); }
    }
    
    public static class EscalationDetail {
        private int ticketId;
        private String userName;
        private String productName;
        private String escalatedAt;
        private int messageCount;
        private String priority;
        private int sessionId;
        private List<Message> conversationHistory;
        
        public EscalationDetail(int ticketId, String userName, String productName, String escalatedAt,
                               int messageCount, String priority, int sessionId, List<Message> conversationHistory) {
            this.ticketId = ticketId;
            this.userName = userName;
            this.productName = productName;
            this.escalatedAt = escalatedAt;
            this.messageCount = messageCount;
            this.priority = priority;
            this.sessionId = sessionId;
            this.conversationHistory = conversationHistory;
        }
        
        public int getTicketId() { return ticketId; }
        public String getUserName() { return userName; }
        public String getProductName() { return productName; }
        public String getEscalatedAt() { return escalatedAt; }
        public int getMessageCount() { return messageCount; }
        public String getPriority() { return priority; }
        public int getSessionId() { return sessionId; }
        public List<Message> getConversationHistory() { return conversationHistory; }
    }
}

