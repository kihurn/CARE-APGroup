package com.care.dao;

import com.care.model.ChatSession;
import com.care.util.DatabaseDriver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for ChatSession table
 */
public class ChatSessionDAO {
    
    private Connection connection;
    
    public ChatSessionDAO() {
        this.connection = DatabaseDriver.getInstance().getConnection();
    }
    
    /**
     * Create a new chat session
     */
    public int create(ChatSession session) {
        String query = "INSERT INTO chat_sessions (user_id, product_id, status) VALUES (?, ?, ?)";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, session.getUserId());
            stmt.setInt(2, session.getProductId());
            stmt.setString(3, session.getStatus());
            
            System.out.println("Creating chat session: user_id=" + session.getUserId() + 
                             ", product_id=" + session.getProductId() + ", status=" + session.getStatus());
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            if (rowsAffected > 0) {
                // Use fallback - query last inserted ID
                Statement lastIdStmt = connection.createStatement();
                ResultSet rs = lastIdStmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    int sessionId = rs.getInt(1);
                    session.setSessionId(sessionId);
                    rs.close();
                    lastIdStmt.close();
                    System.out.println("✓ Chat session created with ID: " + sessionId);
                    return sessionId;
                }
                lastIdStmt.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Error creating chat session: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Get session by ID
     */
    public ChatSession getById(int sessionId) {
        String query = "SELECT * FROM chat_sessions WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSession(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error loading chat session: " + sessionId);
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all sessions for a user
     */
    public List<ChatSession> getByUserId(int userId) {
        List<ChatSession> sessions = new ArrayList<>();
        String query = "SELECT * FROM chat_sessions WHERE user_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
            
            System.out.println("✓ Loaded " + sessions.size() + " chat sessions for user: " + userId);
        } catch (SQLException e) {
            System.err.println("Error loading chat sessions for user: " + userId);
            e.printStackTrace();
        }
        
        return sessions;
    }
    
    /**
     * Update session status
     */
    public boolean updateStatus(int sessionId, String status) {
        String query = "UPDATE chat_sessions SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, sessionId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Session " + sessionId + " status updated to: " + status);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating session status");
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Assign session to agent (for escalation)
     */
    public boolean assignToAgent(int sessionId, int agentId) {
        String query = "UPDATE chat_sessions SET assigned_agent_id = ?, status = 'ESCALATED', updated_at = CURRENT_TIMESTAMP WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, agentId);
            stmt.setInt(2, sessionId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Session " + sessionId + " assigned to agent: " + agentId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error assigning session to agent");
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get all sessions (for analytics)
     */
    public List<ChatSession> getAllSessions() {
        List<ChatSession> sessions = new ArrayList<>();
        String query = "SELECT * FROM chat_sessions ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading all sessions");
            e.printStackTrace();
        }
        
        return sessions;
    }
    
    /**
     * Get all active sessions
     */
    public List<ChatSession> getActiveSessions() {
        List<ChatSession> sessions = new ArrayList<>();
        String query = "SELECT * FROM chat_sessions WHERE status = 'ACTIVE' ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading active sessions");
            e.printStackTrace();
        }
        
        return sessions;
    }
    
    /**
     * Get sessions assigned to an agent
     */
    public List<ChatSession> getByAgentId(int agentId) {
        List<ChatSession> sessions = new ArrayList<>();
        String query = "SELECT * FROM chat_sessions WHERE assigned_agent_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, agentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading sessions for agent: " + agentId);
            e.printStackTrace();
        }
        
        return sessions;
    }
    
    /**
     * Map ResultSet to ChatSession object
     */
    private ChatSession mapResultSetToSession(ResultSet rs) throws SQLException {
        ChatSession session = new ChatSession();
        session.setSessionId(rs.getInt("session_id"));
        session.setUserId(rs.getInt("user_id"));
        session.setProductId(rs.getInt("product_id"));
        session.setStatus(rs.getString("status"));
        
        int agentId = rs.getInt("assigned_agent_id");
        if (!rs.wasNull()) {
            session.setAssignedAgentId(agentId);
        }
        
        session.setCreatedAt(rs.getString("created_at"));
        session.setUpdatedAt(rs.getString("updated_at"));
        
        String closedAt = rs.getString("closed_at");
        if (closedAt != null && !closedAt.isEmpty()) {
            session.setClosedAt(closedAt);
        }
        
        return session;
    }
}


