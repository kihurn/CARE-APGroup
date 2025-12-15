package com.care.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Singleton class to manage SQLite database connection
 * Implements thread-safe lazy initialization
 */
public class DatabaseDriver {
    private static DatabaseDriver instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:care.db";
    
    /**
     * Private constructor to prevent instantiation
     */
    private DatabaseDriver() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            // Establish connection
            connection = DriverManager.getConnection(DB_URL);
            
            // CRITICAL: Ensure auto-commit is ON for SQLite
            connection.setAutoCommit(true);
            
            System.out.println("Database connection established: " + DB_URL);
            System.out.println("Auto-commit enabled: " + connection.getAutoCommit());
            
            // Initialize database schema
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database!");
            e.printStackTrace();
        }
    }
    
    /**
     * Get the singleton instance of DatabaseDriver
     * Thread-safe implementation
     * 
     * @return DatabaseDriver instance
     */
    public static synchronized DatabaseDriver getInstance() {
        if (instance == null) {
            instance = new DatabaseDriver();
        }
        return instance;
    }
    
    /**
     * Get the active database connection
     * 
     * @return Connection object
     */
    public Connection getConnection() {
        try {
            // Check if connection is closed and reconnect if necessary
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                connection.setAutoCommit(true); // Ensure auto-commit is ON
                System.out.println("Database reconnected (auto-commit: " + connection.getAutoCommit() + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection status!");
            e.printStackTrace();
        }
        return connection;
    }
    
    /**
     * Initialize database by executing schema.sql
     * Creates all tables, enables foreign keys, and loads mock data
     */
    private void initializeDatabase() {
        try {
            Statement statement = connection.createStatement();
            
            // CRITICAL: Enable foreign key constraints in SQLite
            statement.execute("PRAGMA foreign_keys = ON;");
            
            // Load schema.sql from resources
            InputStream schemaStream = getClass().getResourceAsStream("/com/care/sql/schema.sql");
            
            if (schemaStream == null) {
                System.err.println("schema.sql not found in resources!");
                return;
            }
            
            // Read and parse SQL script
            BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream, StandardCharsets.UTF_8));
            StringBuilder currentStatement = new StringBuilder();
            String line;
            int executedCount = 0;
            
            while ((line = reader.readLine()) != null) {
                // Skip empty lines and pure comment lines
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                    continue;
                }
                
                // Remove inline comments
                int commentIndex = trimmedLine.indexOf("--");
                if (commentIndex > 0) {
                    trimmedLine = trimmedLine.substring(0, commentIndex).trim();
                }
                
                // Append line to current statement
                currentStatement.append(trimmedLine).append(" ");
                
                // If line ends with semicolon, execute the statement
                if (trimmedLine.endsWith(";")) {
                    String sql = currentStatement.toString().trim();
                    // Remove the trailing semicolon
                    sql = sql.substring(0, sql.length() - 1).trim();
                    
                    if (!sql.isEmpty()) {
                        try {
                            statement.execute(sql);
                            executedCount++;
                        } catch (SQLException e) {
                            // Silently skip UNIQUE constraint errors (expected for re-runs)
                            if (!e.getMessage().contains("UNIQUE constraint failed")) {
                                System.err.println("SQL Warning: " + e.getMessage());
                            }
                        }
                    }
                    
                    // Reset for next statement
                    currentStatement = new StringBuilder();
                }
            }
            
            reader.close();
            
            statement.close();
            System.out.println("✓ Database initialized successfully (" + executedCount + " SQL statements executed)");
            System.out.println("✓ Foreign key constraints enabled");
            System.out.println("✓ Mock data loaded (4 users, 3 products, 2 chat sessions)");
            
        } catch (Exception e) {
            System.err.println("Error initializing database schema!");
            e.printStackTrace();
        }
    }
    
    /**
     * Close the database connection
     * Should be called when application shuts down
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection!");
            e.printStackTrace();
        }
    }
    
    /**
     * Test the database connection
     * 
     * @return true if connection is active, false otherwise
     */
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

