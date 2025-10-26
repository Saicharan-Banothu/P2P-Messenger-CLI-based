package com.p2pchat.storage.dao;

import com.p2pchat.core.models.Message;
import com.p2pchat.storage.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    
    public boolean save(Message message) {
        String sql = "INSERT INTO messages (id, conversation_id, sender_phone, receiver_phone, message_type, content, file_name, file_size, file_path, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, message.getId());
            stmt.setLong(2, message.getConversationId() != null ? message.getConversationId() : 1L);
            stmt.setString(3, message.getSenderPhone());
            stmt.setString(4, message.getReceiverPhone());
            stmt.setString(5, message.getMessageType().name());
            stmt.setString(6, message.getContent());
            stmt.setString(7, message.getFileName());
            stmt.setObject(8, message.getFileSize());
            stmt.setString(9, message.getFilePath());
            stmt.setString(10, message.getStatus().name());
            stmt.setTimestamp(11, Timestamp.valueOf(message.getCreatedAt()));
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
            return false;
        }
    }
    
    public List<Message> findByReceiverPhone(String receiverPhone) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, conversation_id, sender_phone, receiver_phone, message_type, content, file_name, file_size, file_path, status, created_at FROM messages WHERE receiver_phone = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, receiverPhone);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Message message = createMessageFromResultSet(rs);
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching messages: " + e.getMessage());
        }
        
        return messages;
    }
    
    public List<Message> findByParticipants(String user1Phone, String user2Phone) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, conversation_id, sender_phone, receiver_phone, message_type, content, file_name, file_size, file_path, status, created_at FROM messages WHERE (sender_phone = ? AND receiver_phone = ?) OR (sender_phone = ? AND receiver_phone = ?) ORDER BY created_at";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user1Phone);
            stmt.setString(2, user2Phone);
            stmt.setString(3, user2Phone);
            stmt.setString(4, user1Phone);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Message message = createMessageFromResultSet(rs);
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching conversation messages: " + e.getMessage());
        }
        
        return messages;
    }
    
    public boolean updateStatus(String messageId, Message.MessageStatus status) {
        String sql = "UPDATE messages SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setString(2, messageId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating message status: " + e.getMessage());
            return false;
        }
    }
    
    private Message createMessageFromResultSet(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getString("id"));
        message.setConversationId(rs.getLong("conversation_id"));
        message.setSenderPhone(rs.getString("sender_phone"));
        message.setReceiverPhone(rs.getString("receiver_phone"));
        
        try {
            Message.MessageType messageType = Message.MessageType.valueOf(rs.getString("message_type"));
            message.setMessageType(messageType);
        } catch (IllegalArgumentException e) {
            message.setMessageType(Message.MessageType.TEXT);
        }
        
        message.setContent(rs.getString("content"));
        message.setFileName(rs.getString("file_name"));
        message.setFileSize(rs.getLong("file_size"));
        message.setFilePath(rs.getString("file_path"));
        
        try {
            Message.MessageStatus status = Message.MessageStatus.valueOf(rs.getString("status"));
            message.setStatus(status);
        } catch (IllegalArgumentException e) {
            message.setStatus(Message.MessageStatus.SENT);
        }
        
        message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        return message;
    }
}