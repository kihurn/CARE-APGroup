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
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
     * Generate AI response for image analysis with Vision API
     * Uses direct HTTP requests to OpenAI's Vision API
     * 
     * @param userMessage The user's question about the image
     * @param imageBase64 Base64-encoded image data
     * @param productId The product being discussed
     * @param conversationHistory Previous messages in the conversation
     * @return AI-generated response analyzing the image
     */
    public String generateVisionResponse(String userMessage, String imageBase64, int productId, List<com.care.model.Message> conversationHistory) {
        if (!config.isOpenAIConfigured()) {
            return "⚠ AI service not configured. Please contact administrator to set up OpenAI API key.";
        }
        
        try {
            // Get product context
            Product product = productDAO.getById(productId);
            String productContext = buildProductContext(product, productId);
            
            // Build JSON request for Vision API
            JSONObject requestBody = new JSONObject();
            // Use the most stable, current multimodal model
            // gpt-4o supports both text and image inputs
            requestBody.put("model", "gpt-4o");
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);
            
            // Build messages array
            JSONArray messages = new JSONArray();
            
            // System message with product context
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", 
                "You are a helpful technical support assistant with vision capabilities for CARE (Customer Assistance and Resource Engine). " +
                "You can analyze images of products, error messages, setup configurations, and hardware issues. " +
                "Provide detailed, step-by-step solutions based on what you see in the image. " +
                "Be professional, thorough, and helpful. " +
                "If the issue requires physical inspection or parts replacement, recommend escalating to a human agent.\n\n" +
                productContext);
            messages.put(systemMessage);
            
            // Add conversation history (text only)
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (com.care.model.Message msg : conversationHistory) {
                    JSONObject historyMsg = new JSONObject();
                    historyMsg.put("role", msg.getSenderType().equals("USER") ? "user" : "assistant");
                    historyMsg.put("content", msg.getContent());
                    messages.put(historyMsg);
                }
            }
            
            // User message with image
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            
            // Content is an array with text and image parts
            JSONArray content = new JSONArray();
            
            // Text part
            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            String prompt = userMessage.isEmpty() ? 
                "Please analyze this image carefully. Identify any visible issues, errors, or problems. Provide a detailed description and step-by-step troubleshooting instructions." : 
                userMessage;
            textPart.put("text", prompt);
            content.put(textPart);
            
            // Image part with base64 data
            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", "data:image/jpeg;base64," + imageBase64);
            imagePart.put("image_url", imageUrl);
            content.put(imagePart);
            
            userMsg.put("content", content);
            messages.put(userMsg);
            
            requestBody.put("messages", messages);
            
            // Make HTTP request to OpenAI API
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getOpenAIApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .timeout(Duration.ofSeconds(60))
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parse response
                JSONObject responseJson = new JSONObject(response.body());
                String aiResponse = responseJson.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
                
                System.out.println("✓ AI vision response generated successfully");
                return aiResponse;
            } else {
                System.err.println("OpenAI API error: " + response.statusCode());
                System.err.println("Response: " + response.body());
                
                return "⚠ I'm having trouble analyzing the image (API Error " + response.statusCode() + "). " +
                       "Please try again or describe the issue in text.";
            }
            
        } catch (Exception e) {
            System.err.println("Error generating AI vision response");
            e.printStackTrace();
            
            // Provide helpful error message
            return "⚠ I'm having trouble analyzing the image right now. " +
                   "This could be due to:\n" +
                   "- Image format not supported\n" +
                   "- API limitations\n" +
                   "- Network connectivity issues\n\n" +
                   "Please try:\n" +
                   "1. Uploading a different image format (PNG or JPEG)\n" +
                   "2. Describing the issue in text\n" +
                   "3. Escalating to a human agent for immediate help";
        }
    }
    
    /**
     * Check if AI service is ready
     */
    public boolean isReady() {
        return config.isOpenAIConfigured() && openAiService != null;
    }
}

