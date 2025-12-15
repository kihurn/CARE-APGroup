package com.care.service;

import com.care.dao.TicketDAO;
import com.care.model.Ticket;

import java.util.List;

/**
 * Service layer for Ticket operations
 * Handles business logic for ticket-related operations
 */
public class TicketService {
    
    private TicketDAO ticketDAO;
    
    public TicketService() {
        this.ticketDAO = new TicketDAO();
    }
    
    /**
     * Create a new ticket
     */
    public int createTicket(Ticket ticket) {
        // Validation
        if (ticket.getSessionId() <= 0) {
            System.err.println("Invalid session ID");
            return -1;
        }
        
        if (ticket.getPriority() == null || ticket.getPriority().trim().isEmpty()) {
            ticket.setPriority("MEDIUM"); // Default priority
        }
        
        if (ticket.getStatus() == null || ticket.getStatus().trim().isEmpty()) {
            ticket.setStatus("OPEN"); // Default status
        }
        
        return ticketDAO.createTicket(ticket);
    }
    
    /**
     * Get all tickets
     */
    public List<Ticket> getAllTickets() {
        return ticketDAO.getAllTickets();
    }
    
    /**
     * Get ticket by ID
     */
    public Ticket getTicketById(int ticketId) {
        return ticketDAO.findById(ticketId);
    }
    
    /**
     * Get ticket by session ID
     */
    public Ticket getTicketBySessionId(int sessionId) {
        return ticketDAO.findBySessionId(sessionId);
    }
    
    /**
     * Get tickets by status
     */
    public List<Ticket> getTicketsByStatus(String status) {
        return ticketDAO.getByStatus(status);
    }
    
    /**
     * Get tickets by priority
     */
    public List<Ticket> getTicketsByPriority(String priority) {
        return ticketDAO.getByPriority(priority);
    }
    
    /**
     * Get tickets assigned to an agent
     */
    public List<Ticket> getTicketsByAgent(int agentId) {
        return ticketDAO.getByAgentId(agentId);
    }
    
    /**
     * Get unassigned tickets
     */
    public List<Ticket> getUnassignedTickets() {
        return ticketDAO.getUnassignedTickets();
    }
    
    /**
     * Update ticket status
     */
    public boolean updateTicketStatus(int ticketId, String status) {
        // Validate status
        if (!isValidStatus(status)) {
            System.err.println("Invalid status: " + status);
            return false;
        }
        
        return ticketDAO.updateStatus(ticketId, status);
    }
    
    /**
     * Assign ticket to agent
     */
    public boolean assignTicketToAgent(int ticketId, int agentId) {
        if (agentId <= 0) {
            System.err.println("Invalid agent ID");
            return false;
        }
        
        return ticketDAO.assignAgent(ticketId, agentId);
    }
    
    /**
     * Update ticket priority
     */
    public boolean updateTicketPriority(int ticketId, String priority) {
        // Validate priority
        if (!isValidPriority(priority)) {
            System.err.println("Invalid priority: " + priority);
            return false;
        }
        
        return ticketDAO.updatePriority(ticketId, priority);
    }
    
    /**
     * Delete ticket
     */
    public boolean deleteTicket(int ticketId) {
        return ticketDAO.delete(ticketId);
    }
    
    /**
     * Validate status value
     */
    private boolean isValidStatus(String status) {
        return status != null && 
               (status.equals("OPEN") || 
                status.equals("IN_PROGRESS") || 
                status.equals("RESOLVED") || 
                status.equals("CLOSED"));
    }
    
    /**
     * Validate priority value
     */
    private boolean isValidPriority(String priority) {
        return priority != null && 
               (priority.equals("LOW") || 
                priority.equals("MEDIUM") || 
                priority.equals("HIGH") || 
                priority.equals("CRITICAL"));
    }
    
    /**
     * Get ticket count by status
     */
    public int getCountByStatus(String status) {
        return ticketDAO.getByStatus(status).size();
    }
    
    /**
     * Get total open tickets count
     */
    public int getOpenTicketsCount() {
        return ticketDAO.getByStatus("OPEN").size();
    }
}


