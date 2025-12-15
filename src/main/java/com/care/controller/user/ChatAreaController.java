package com.care.controller.user;

import com.care.dao.ChatSessionDAO;
import com.care.dao.MessageDAO;
import com.care.model.ChatSession;
import com.care.model.Message;
import com.care.model.Product;
import com.care.model.Ticket;
import com.care.service.AIService;
import com.care.service.TicketService;
import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label; // Changed from Text to Label
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox; // Added HBox
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Chat Area with OpenAI Integration
 */
public class ChatAreaController {
    
    @FXML private Text chatTitleText;
    @FXML private Text chatSubtitleText;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInputField;
    @FXML private Button sendBtn;
    @FXML private Button escalateBtn;
    @FXML private Button endChatBtn;
    
    private ViewFactory viewFactory;
    private AIService aiService;
    private ChatSessionDAO chatSessionDAO;
    private MessageDAO messageDAO;
    private TicketService ticketService;
    
    private ChatSession currentSession;
    private Product currentProduct;
    private List<Message> conversationHistory;
    
    public ChatAreaController() {
        this.viewFactory = ViewFactory.getInstance();
        this.aiService = new AIService();
        this.chatSessionDAO = new ChatSessionDAO();
        this.messageDAO = new MessageDAO();
        this.ticketService = new TicketService();
        this.conversationHistory = new ArrayList<>();
    }
    
    @FXML
    private void initialize() {
        System.out.println("Initializing ChatAreaController...");
        
        // Auto-scroll to bottom when new messages added
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            messagesScrollPane.setVvalue(1.0);
        });
        
        // Focus on input field
        Platform.runLater(() -> messageInputField.requestFocus());
        
        // Check if AI service is ready
        if (!aiService.isReady()) {
            addMessage("SYSTEM", "⚠ AI service not configured. Please contact administrator to set up OpenAI API key.");
            sendBtn.setDisable(true);
        } else {
            // Start chat session
            startChatSession();
        }
    }
    
    /**
     * Start a new chat session OR continue existing one
     */
    private void startChatSession() {
        try {
            SessionManager sessionManager = SessionManager.getInstance();
            int userId = sessionManager.getCurrentUserId();
            
            // Check if we're continuing an existing session
            ChatSession existingSession = sessionManager.getCurrentChatSession();
            
            if (existingSession != null) {
                // Continue existing session
                System.out.println("✓ Continuing existing session: " + existingSession.getSessionId());
                currentSession = existingSession;
                
                // Get product for this session
                currentProduct = new com.care.dao.ProductDAO().getById(currentSession.getProductId());
                
                if (currentProduct == null) {
                    System.err.println("❌ Product not found for session: " + currentSession.getProductId());
                    addMessage("SYSTEM", "⚠ Product not found. This session may be corrupted.");
                    sendBtn.setDisable(true);
                    return;
                }
                
                // Update chat title
                chatTitleText.setText("Chat Support - " + currentProduct.getName());
                chatSubtitleText.setText("Continuing Previous Conversation");
                
                // Load previous messages
                List<Message> previousMessages = messageDAO.getBySessionId(currentSession.getSessionId());
                System.out.println("✓ Loaded " + previousMessages.size() + " previous messages");
                
                for (Message msg : previousMessages) {
                    addMessage(msg.getSenderType(), msg.getContent());
                }
                
                // Add continuation message if there were previous messages
                if (previousMessages.size() > 0) {
                    addMessage("SYSTEM", "💬 Continuing your previous conversation. You can keep chatting!");
                } else {
                    // Empty session - just show welcome
                    String welcomeMsg = "👋 Hello! I'm your AI support assistant. I'm here to help you with " + 
                                      currentProduct.getName() + ". How can I assist you today?";
                    addMessage("BOT", welcomeMsg);
                    saveMessage(currentSession.getSessionId(), "BOT", welcomeMsg);
                }
                
                // Clear the session from SessionManager so it doesn't reload next time
                sessionManager.clearCurrentChatSession();
                
            } else {
                // Start new session
                currentProduct = sessionManager.getSelectedProduct();
            
            if (currentProduct == null) {
                addMessage("SYSTEM", "⚠ No product selected. Please select a product first.");
                sendBtn.setDisable(true);
                return;
            }
            
            // Update chat title
            chatTitleText.setText("Chat Support - " + currentProduct.getName());
            chatSubtitleText.setText("AI-Powered Support Assistant");
            
            // Create new chat session in database
            currentSession = new ChatSession();
            currentSession.setUserId(userId);
            currentSession.setProductId(currentProduct.getProductId());
            currentSession.setStatus("ACTIVE");
            
            int sessionId = chatSessionDAO.create(currentSession);
            
            if (sessionId > 0) {
                currentSession.setSessionId(sessionId);
                    System.out.println("✓ Chat session started successfully with ID: " + sessionId);
                
                // Welcome message
                String welcomeMsg = "👋 Hello! I'm your AI support assistant. I'm here to help you with " + 
                                  currentProduct.getName() + ". How can I assist you today?";
                addMessage("BOT", welcomeMsg);
                
                // Save welcome message
                saveMessage(sessionId, "BOT", welcomeMsg);
            } else {
                    System.err.println("❌ Failed to create chat session - sessionId: " + sessionId);
                addMessage("SYSTEM", "⚠ Failed to start chat session. Please try again.");
                sendBtn.setDisable(true);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error starting chat session");
            e.printStackTrace();
            addMessage("SYSTEM", "⚠ Error starting chat session: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSendMessage() {
        String userMessage = messageInputField.getText().trim();
        
        if (userMessage.isEmpty()) {
            return;
        }
        
        if (currentSession == null || currentProduct == null) {
            addMessage("SYSTEM", "⚠ No active chat session.");
            return;
        }
        
        // Add user message to UI
        addMessage("USER", userMessage);
        messageInputField.clear();
        
        // Save user message to database
        saveMessage(currentSession.getSessionId(), "USER", userMessage);
        
        // Add to conversation history
        Message userMsg = new Message();
        userMsg.setSenderType("USER");
        userMsg.setContent(userMessage);
        conversationHistory.add(userMsg);
        
        // Disable send button while processing
        sendBtn.setDisable(true);
        
        // Show typing indicator
        addMessage("BOT", "⏳ Typing...");
        
        // Get AI response in background thread
        new Thread(() -> {
            try {
                String aiResponse = aiService.generateResponse(
                    userMessage, 
                    currentProduct.getProductId(), 
                    conversationHistory
                );
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    // Remove typing indicator (last item in list)
                    if (!messagesContainer.getChildren().isEmpty()) {
                        messagesContainer.getChildren().remove(messagesContainer.getChildren().size() - 1);
                    }
                    
                    // Add AI response
                    addMessage("BOT", aiResponse);
                    
                    // Save AI response to database
                    saveMessage(currentSession.getSessionId(), "BOT", aiResponse);
                    
                    // Add to conversation history
                    Message botMsg = new Message();
                    botMsg.setSenderType("BOT");
                    botMsg.setContent(aiResponse);
                    conversationHistory.add(botMsg);
                    
                    // Re-enable send button
                    sendBtn.setDisable(false);
                    messageInputField.requestFocus();
                });
                
            } catch (Exception e) {
                System.err.println("Error getting AI response");
                e.printStackTrace();
                
                Platform.runLater(() -> {
                    // Remove typing indicator
                    if (!messagesContainer.getChildren().isEmpty()) {
                        messagesContainer.getChildren().remove(messagesContainer.getChildren().size() - 1);
                    }
                    
                    addMessage("SYSTEM", "⚠ Error getting response. Please try again.");
                    sendBtn.setDisable(false);
                });
            }
        }).start();
    }
    
    /**
     * Save message to database
     */
    private void saveMessage(int sessionId, String senderType, String content) {
        try {
            Message message = new Message();
            message.setSessionId(sessionId);
            message.setSenderType(senderType);
            message.setContent(content);
            messageDAO.create(message);
        } catch (Exception e) {
            System.err.println("Error saving message");
            e.printStackTrace();
        }
    }
    
    // =========================================================================
    // UPDATED METHOD: Uses HBox and Label to make bubbles fit content properly
    // =========================================================================
    private void addMessage(String senderType, String content) {
        // 1. Create the Bubble (Label)
        // Label is better than Text because it supports background CSS and padding natively
        Label messageLabel = new Label(content);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("message-bubble"); // Base style

        // 2. Apply Specific Styles based on Sender
        if (senderType.equals("USER")) {
            messageLabel.getStyleClass().add("message-user");
        } else if (senderType.equals("BOT")) {
            messageLabel.getStyleClass().add("message-bot");
        } else if (senderType.equals("SYSTEM")) {
            // Optional: You might want a specific style for system alerts
            messageLabel.setStyle("-fx-background-color: #ffeeba; -fx-text-fill: #856404; -fx-background-radius: 10;");
        } else {
            messageLabel.getStyleClass().add("message-agent");
        }

        // 3. Create a Row Container (HBox)
        // This HBox holds the bubble and allows it to align Left or Right
        HBox rowContainer = new HBox();
        rowContainer.setFillHeight(true);

        // 4. Alignment Logic
        if (senderType.equals("USER")) {
            // User floats Right
            rowContainer.setAlignment(Pos.CENTER_RIGHT);
            // Padding: Top, Right, Bottom, Left
            // We give 50px padding on the LEFT to ensure the User bubble doesn't stretch to the left edge
            rowContainer.setPadding(new Insets(5, 5, 5, 50));
        } else {
            // Bot/Agent floats Left
            rowContainer.setAlignment(Pos.CENTER_LEFT);
            // We give 50px padding on the RIGHT to ensure the Bot bubble doesn't stretch to the right edge
            rowContainer.setPadding(new Insets(5, 50, 5, 5));
        }
        
        // 5. Add Bubble to Row
        rowContainer.getChildren().add(messageLabel);

        // 6. Add Row to the main VBox
        messagesContainer.getChildren().add(rowContainer);
    }
    
    @FXML
    private void handleEscalate() {
        System.out.println("Escalating to live support agent...");
        
        if (currentSession != null) {
            // Check if ticket already exists for this session
            Ticket existingTicket = ticketService.getTicketBySessionId(currentSession.getSessionId());
            if (existingTicket != null) {
                addMessage("SYSTEM", "⚠️ This chat has already been escalated. Ticket #" + existingTicket.getTicketId());
                return;
            }
            
            // Show "Requesting Live Support Agent" message
            addMessage("SYSTEM", "📞 Requesting Live Support Agent...");
            
            // Disable buttons immediately
            sendBtn.setDisable(true);
            escalateBtn.setDisable(true);
            
            // Simulate brief delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500); 
                    
                    javafx.application.Platform.runLater(() -> {
                        try {
                            System.out.println("Updating session status to ESCALATED...");
            chatSessionDAO.updateStatus(currentSession.getSessionId(), "ESCALATED");
            
                            System.out.println("Assigning to agent Steve (ID: 2)...");
                            chatSessionDAO.assignToAgent(currentSession.getSessionId(), 2);
                            
                            System.out.println("Creating ticket...");
            Ticket ticket = new Ticket();
            ticket.setSessionId(currentSession.getSessionId());
                            ticket.setAssignedAgentId(2); 
            ticket.setStatus("OPEN");
            
            String priority = determinePriority();
            ticket.setPriority(priority);
            
                            System.out.println("Calling ticketService.createTicket()...");
            int ticketId = ticketService.createTicket(ticket);
                            System.out.println("Ticket creation returned ID: " + ticketId);
            
            if (ticketId > 0) {
                System.out.println("✓ Ticket created successfully: " + ticketId);
                                addMessage("SYSTEM", "✅ Connected to Live Support Agent: Steve\n" +
                                    "Ticket #" + ticketId + " (Priority: " + priority + ")\n" +
                                                    "Steve will assist you shortly.");
            } else {
                                System.err.println("❌ Ticket creation failed - returned ID: " + ticketId);
                                addMessage("SYSTEM", "⚠️ Failed to connect to agent. Please try again.");
                                sendBtn.setDisable(false);
                                escalateBtn.setDisable(false);
            }
                        } catch (Exception ex) {
                            System.err.println("❌ Exception during escalation: " + ex.getMessage());
                            ex.printStackTrace();
                            addMessage("SYSTEM", "⚠️ Error connecting to agent: " + ex.getMessage());
                            sendBtn.setDisable(false);
                            escalateBtn.setDisable(false);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    private String determinePriority() {
        String[] urgentKeywords = {"urgent", "critical", "emergency", "broken", "not working", "error", "crash"};
        String[] highKeywords = {"important", "asap", "quickly", "problem", "issue", "bug"};
        
        int checkCount = Math.min(5, conversationHistory.size());
        for (int i = conversationHistory.size() - checkCount; i < conversationHistory.size(); i++) {
            String content = conversationHistory.get(i).getContent().toLowerCase();
            for (String keyword : urgentKeywords) {
                if (content.contains(keyword)) return "HIGH";
            }
            for (String keyword : highKeywords) {
                if (content.contains(keyword)) return "MEDIUM";
            }
        }
            return "MEDIUM";
    }
    
    @FXML
    private void handleEndChat() {
        System.out.println("Ending chat session");
        if (currentSession != null) {
            chatSessionDAO.updateStatus(currentSession.getSessionId(), "CLOSED");
            addMessage("SYSTEM", "👋 Chat session ended. Thank you for using CARE support!");
        }
        Platform.runLater(() -> {
            try {
                Thread.sleep(1500);
                viewFactory.setUserSelectedMenuItem("UserHistory");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}