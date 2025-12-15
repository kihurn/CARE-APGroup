package com.care;

import com.care.util.DatabaseDriver;
import com.care.util.ViewFactory;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main Application Entry Point for CARE
 * Customer Assistance and Resource Engine
 */
public class App extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize Database
            DatabaseDriver dbDriver = DatabaseDriver.getInstance();
            
            if (dbDriver.testConnection()) {
                System.out.println("✓ Database initialized successfully");
            } else {
                System.err.println("✗ Database connection failed!");
                return;
            }
            
            // Initialize ViewFactory with primary stage
            ViewFactory viewFactory = ViewFactory.getInstance();
            viewFactory.setPrimaryStage(primaryStage);
            
            // Show Login Window
            viewFactory.showLoginWindow();
            
            // Application shutdown hook
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Closing application...");
                dbDriver.closeConnection();
            });
            
        } catch (Exception e) {
            System.err.println("Error starting application!");
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        // Clean up resources when application stops
        DatabaseDriver.getInstance().closeConnection();
        System.out.println("Application stopped successfully");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

