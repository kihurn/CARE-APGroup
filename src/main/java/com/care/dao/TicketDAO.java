package com.care.dao;

import com.care.model.Ticket;
import com.care.util.DatabaseDriver;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Ticket operations
 */
public class TicketDAO {
    
    private Connection connection;
    
    public TicketDAO() {
        this.connection = DatabaseDriver.getInstance().getConnection();
    }
    
    /**
     * Create a new ticket
     */
    public int createTicket(Ticket ticket) {
        String insertQuery = "INSERT INTO tickets (session_id, assigned_agent_id, priority, status) VALUES (?, ?, ?, ?)";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(insertQuery);
            System.out.println("Creating ticket: session_id=" + ticket.getSessionId() + 
                             ", agent_id=" + ticket.getAssignedAgentId() + 
                             ", priority=" + ticket.getPriority() + 
                             ", status=" + ticket.getStatus());
            
            stmt.setInt(1, ticket.getSessionId());
            if (ticket.getAssignedAgentId() != null && ticket.getAssignedAgentId() > 0) {
                stmt.setInt(2, ticket.getAssignedAgentId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, ticket.getPriority());
            stmt.setString(4, ticket.getStatus());
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            if (rowsAffected > 0) {
                // Use fallback - query last inserted ID
                Statement lastIdStmt = connection.createStatement();
                ResultSet rs = lastIdStmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    int ticketId = rs.getInt(1);
                    ticket.setTicketId(ticketId);
                    rs.close();
                    lastIdStmt.close();
                    System.out.println("✓ Ticket created with ID: " + ticketId);
                    return ticketId;
                }
                lastIdStmt.close();
            }
            
            System.err.println("❌ No rows affected when creating ticket");
            return -1;
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating ticket: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Get all tickets
     */
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT * FROM tickets ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
            
            System.out.println("✓ Loaded " + tickets.size() + " tickets");
        } catch (SQLException e) {
            System.err.println("Error loading tickets");
            e.printStackTrace();
        }
        
        return tickets;
    }
    
    /**
     * Get ticket by ID
     */
    public Ticket findById(int ticketId) {
        String query = "SELECT * FROM tickets WHERE ticket_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ticketId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTicket(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding ticket: " + ticketId);
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get ticket by session ID
     */
    public Ticket findBySessionId(int sessionId) {
        String query = "SELECT * FROM tickets WHERE session_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTicket(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding ticket for session: " + sessionId);
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get tickets by status
     */
    public List<Ticket> getByStatus(String status) {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT * FROM tickets WHERE status = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading tickets by status: " + status);
            e.printStackTrace();
        }
        
        return tickets;
    }
    
    /**
     * Get tickets by priority
     */
    public List<Ticket> getByPriority(String priority) {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT * FROM tickets WHERE priority = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, priority);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading tickets by priority: " + priority);
            e.printStackTrace();
        }
        
        return tickets;
    }
    
    /**
     * Get tickets assigned to an agent
     */
    public List<Ticket> getByAgentId(int agentId) {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT * FROM tickets WHERE assigned_agent_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, agentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading tickets for agent: " + agentId);
            e.printStackTrace();
        }
        
        return tickets;
    }
    
    /**
     * Get unassigned tickets
     */
    public List<Ticket> getUnassignedTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT * FROM tickets WHERE assigned_agent_id IS NULL ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading unassigned tickets");
            e.printStackTrace();
        }
        
        return tickets;
    }
    
    /**
     * Update ticket status
     */
    public boolean updateStatus(int ticketId, String status) {
        String query = "UPDATE tickets SET status = ? WHERE ticket_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, ticketId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Ticket " + ticketId + " status updated to: " + status);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating ticket status");
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Assign ticket to agent
     */
    public boolean assignAgent(int ticketId, int agentId) {
        String query = "UPDATE tickets SET assigned_agent_id = ?, status = 'IN_PROGRESS' WHERE ticket_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, agentId);
            stmt.setInt(2, ticketId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Ticket " + ticketId + " assigned to agent: " + agentId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error assigning ticket to agent");
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Update ticket priority
     */
    public boolean updatePriority(int ticketId, String priority) {
        String query = "UPDATE tickets SET priority = ? WHERE ticket_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, priority);
            stmt.setInt(2, ticketId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Ticket " + ticketId + " priority updated to: " + priority);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating ticket priority");
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete ticket
     */
    public boolean delete(int ticketId) {
        String query = "DELETE FROM tickets WHERE ticket_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ticketId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✓ Ticket deleted (ID: " + ticketId + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting ticket");
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to Ticket object
     */
    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setTicketId(rs.getInt("ticket_id"));
        ticket.setSessionId(rs.getInt("session_id"));
        
        int agentId = rs.getInt("assigned_agent_id");
        ticket.setAssignedAgentId(rs.wasNull() ? null : agentId);
        
        ticket.setPriority(rs.getString("priority"));
        ticket.setStatus(rs.getString("status"));
        
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            ticket.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T")));
        }
        
        return ticket;
    }
}


