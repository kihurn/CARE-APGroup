package com.care.service;

import com.care.model.KnowledgeBase;
import com.care.model.Product;
import com.care.dao.KnowledgeBaseDAO;
import com.care.dao.ProductDAO;
import com.care.util.Config;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * AI Service for handling chatbot interactions using OpenAI API
 */
public class AIService {
    
    private OpenAiService openAiService;
    private Config config;
    private ProductDAO productDAO;
    private KnowledgeBaseDAO knowledgeBaseDAO;
    
    public AIService() {
        this.config = Config.getInstance();
        this.productDAO = new ProductDAO();
        this.knowledgeBaseDAO = new KnowledgeBaseDAO();
        
        // Initialize OpenAI service if configured
        if (config.isOpenAIConfigured()) {
            try {
                this.openAiService = new OpenAiService(config.getOpenAIApiKey(), Duration.ofSeconds(60));
                System.out.println("✓ OpenAI service initialized with model: " + config.getOpenAIModel());
            } catch (Exception e) {
                System.err.println("Error initializing OpenAI service");
                e.printStackTrace();
            }
        } else {
            System.err.println("⚠ OpenAI not configured. Please set API key in config.properties");
        }
    }
    
    /**
     * Generate AI response for user message with product context
     * 
     * @param userMessage The user's message
     * @param productId The product being discussed
     * @param conversationHistory Previous messages in the conversation (as Message objects)
     * @return AI-generated response
     */
    public String generateResponse(String userMessage, int productId, List<com.care.model.Message> conversationHistory) {
        if (!config.isOpenAIConfigured()) {
            return "⚠ AI service not configured. Please contact administrator to set up OpenAI API key.";
        }
        
        if (openAiService == null) {
            return "⚠ AI service initialization failed. Please check configuration.";
        }
        
        try {
            // Get product context
            Product product = productDAO.getById(productId);
            String productContext = buildProductContext(product, productId);
            
            // Build message list
            List<ChatMessage> messages = new ArrayList<>();
            
            // System message with context
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
                "You are a helpful customer support assistant for CARE (Customer Assistance and Resource Engine). " +
                "You help users with technical support questions about their products. " +
                "Be professional, friendly, and concise. " +
                "If you don't know the answer, suggest escalating to a human agent.\n\n" +
                productContext));
            
            // Add conversation history (convert from Message to ChatMessage)
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (com.care.model.Message msg : conversationHistory) {
                    String role = msg.getSenderType().equals("USER") ? 
                                ChatMessageRole.USER.value() : 
                                ChatMessageRole.ASSISTANT.value();
                    messages.add(new ChatMessage(role, msg.getContent()));
                }
            }
            
            // Add current user message
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));
            
            // Create completion request
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(config.getOpenAIModel())
                    .messages(messages)
                    .maxTokens(config.getMaxTokens())
                    .temperature(config.getTemperature())
                    .build();
            
            // Get response from OpenAI
            ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
            
            String aiResponse = result.getChoices().get(0).getMessage().getContent();
            System.out.println("✓ AI response generated (" + result.getUsage().getTotalTokens() + " tokens)");
            
            return aiResponse;
            
        } catch (Exception e) {
            System.err.println("Error generating AI response");
            e.printStackTrace();
            return "⚠ Sorry, I encountered an error processing your request. Please try again or contact support.";
        }
    }
    
    /**
     * Build product context from product info and knowledge base
     */
    private String buildProductContext(Product product, int productId) {
        StringBuilder context = new StringBuilder();
        
        if (product != null) {
            context.append("PRODUCT INFORMATION:\n");
            context.append("- Name: ").append(product.getName()).append("\n");
            context.append("- Model/Version: ").append(product.getModelVersion()).append("\n");
            context.append("- Category: ").append(product.getCategory()).append("\n\n");
            
            // Get knowledge base content if available
            KnowledgeBase kb = knowledgeBaseDAO.getByProductId(productId);
            if (kb != null && kb.getContent() != null) {
                context.append("PRODUCT MANUAL/DOCUMENTATION:\n");
                context.append(kb.getContent()).append("\n\n");
            }
            
            context.append("Use this information to help answer the user's questions about this product.");
        } else {
            context.append("No specific product context available.");
        }
        
        return context.toString();
    }
    
    /**
     * Generate a simple response without product context (for general queries)
     */
    public String generateSimpleResponse(String userMessage) {
        if (!config.isOpenAIConfigured()) {
            return "⚠ AI service not configured. Please contact administrator.";
        }
        
        if (openAiService == null) {
            return "⚠ AI service initialization failed.";
        }
        
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
                "You are a helpful customer support assistant. Be professional and concise."));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));
            
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model(config.getOpenAIModel())
                    .messages(messages)
                    .maxTokens(config.getMaxTokens())
                    .temperature(config.getTemperature())
                    .build();
            
            ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
            return result.getChoices().get(0).getMessage().getContent();
            
        } catch (Exception e) {
            System.err.println("Error generating AI response");
            e.printStackTrace();
            return "⚠ Sorry, I encountered an error. Please try again.";
        }
    }
    
    /**
     * Check if AI service is ready
     */
    public boolean isReady() {
        return config.isOpenAIConfigured() && openAiService != null;
    }
}

