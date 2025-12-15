package com.care.dao;

import com.care.model.Message;
import com.care.util.DatabaseDriver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Message table
 */
public class MessageDAO {
    
    private Connection connection;
    
    public MessageDAO() {
        this.connection = DatabaseDriver.getInstance().getConnection();
    }
    
    /**
     * Create a new message
     */
    public int create(Message message) {
        String query = "INSERT INTO messages (session_id, sender_type, content) VALUES (?, ?, ?)";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, message.getSessionId());
            stmt.setString(2, message.getSenderType());
            stmt.setString(3, message.getContent());
            
            System.out.println("Saving message: session=" + message.getSessionId() + 
                             ", sender=" + message.getSenderType());
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            if (rowsAffected > 0) {
                // Use fallback - query last inserted ID
                Statement lastIdStmt = connection.createStatement();
                ResultSet rs = lastIdStmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    int messageId = rs.getInt(1);
                    message.setMessageId(messageId);
                    rs.close();
                    lastIdStmt.close();
                    System.out.println("✓ Message created with ID: " + messageId);
                    return messageId;
                }
                lastIdStmt.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Error creating message: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Get all messages for a session
     */
    public List<Message> getBySessionId(int sessionId) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM messages WHERE session_id = ? ORDER BY timestamp ASC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
            
            System.out.println("✓ Loaded " + messages.size() + " messages for session: " + sessionId);
        } catch (SQLException e) {
            System.err.println("Error loading messages for session: " + sessionId);
            e.printStackTrace();
        }
        
        return messages;
    }
    
    /**
     * Get message by ID
     */
    public Message getById(int messageId) {
        String query = "SELECT * FROM messages WHERE message_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, messageId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToMessage(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error loading message: " + messageId);
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Delete all messages for a session
     */
    public boolean deleteBySessionId(int sessionId) {
        String query = "DELETE FROM messages WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("✓ Deleted " + rowsAffected + " messages for session: " + sessionId);
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting messages for session: " + sessionId);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get message count for a session
     */
    public int getMessageCount(int sessionId) {
        String query = "SELECT COUNT(*) FROM messages WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting messages for session: " + sessionId);
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Map ResultSet to Message object
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageId(rs.getInt("message_id"));
        message.setSessionId(rs.getInt("session_id"));
        message.setSenderType(rs.getString("sender_type"));
        message.setContent(rs.getString("content"));
        message.setCreatedAt(rs.getString("timestamp"));
        return message;
    }
}


