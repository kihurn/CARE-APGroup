package com.care.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for application settings
 * Loads API keys and other sensitive data from config.properties
 */
public class Config {
    
    private static Config instance;
    private Properties properties;
    
    private Config() {
        properties = new Properties();
        loadProperties();
    }
    
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }
    
    /**
     * Load properties from config.properties file
     */
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
                System.out.println("✓ Configuration loaded successfully");
            } else {
                System.err.println("⚠ config.properties not found, using defaults");
                setDefaults();
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration");
            e.printStackTrace();
            setDefaults();
        }
    }
    
    /**
     * Set default values if config file not found
     */
    private void setDefaults() {
        properties.setProperty("openai.api.key", "YOUR_API_KEY_HERE");
        properties.setProperty("openai.model", "gpt-3.5-turbo");
        properties.setProperty("openai.max.tokens", "500");
        properties.setProperty("openai.temperature", "0.7");
    }
    
    /**
     * Get OpenAI API Key
     */
    public String getOpenAIApiKey() {
        return properties.getProperty("openai.api.key", "");
    }
    
    /**
     * Get OpenAI Model (e.g., gpt-3.5-turbo, gpt-4)
     */
    public String getOpenAIModel() {
        return properties.getProperty("openai.model", "gpt-3.5-turbo");
    }
    
    /**
     * Get max tokens for OpenAI response
     */
    public int getMaxTokens() {
        return Integer.parseInt(properties.getProperty("openai.max.tokens", "500"));
    }
    
    /**
     * Get temperature for OpenAI (0.0 - 2.0)
     */
    public double getTemperature() {
        return Double.parseDouble(properties.getProperty("openai.temperature", "0.7"));
    }
    
    /**
     * Check if OpenAI is configured
     */
    public boolean isOpenAIConfigured() {
        String apiKey = getOpenAIApiKey();
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_API_KEY_HERE");
    }
}


