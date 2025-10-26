package com.p2pchat.storage.dao;

import com.p2pchat.core.models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageDAO {
    
    public boolean save(Message message) {
        String sql = "INSERT INTO messages (id, sender_phone, receiver_phone, message_type, content, " +
                    "file_name, file_size, file_path, file_id, file_type, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, message.getId() != null ? message.getId() : UUID.randomUUID().toString());
            pstmt.setString(2, message.getSenderPhone());
            pstmt.setString(3, message.getReceiverPhone());
            pstmt.setString(4, message.getMessageType().name());
            pstmt.setString(5, message.getContent());
            pstmt.setString(6, message.getFileName());
            pstmt.setObject(7, message.getFileSize()); // Use Object to handle null
            pstmt.setString(8, message.getFilePath());
            pstmt.setString(9, message.getFileId());
            pstmt.setString(10, message.getFileType());
            pstmt.setString(11, message.getStatus().name());
            pstmt.setTimestamp(12, Timestamp.valueOf(message.getCreatedAt()));
            
            int result = pstmt.executeUpdate();
            if (result > 0) {
                System.out.println("✅ Message saved: " + message.getId());
                return true;
            } else {
                System.err.println("❌ Failed to save message: " + message.getId());
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error saving message: " + e.getMessage());
            return false;
        }
    }
    
    public List<Message> findByReceiverPhone(String receiverPhone) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender_phone, receiver_phone, message_type, content, " +
                    "file_name, file_size, file_path, file_id, file_type, status, created_at " +
                    "FROM messages WHERE receiver_phone = ? ORDER BY created_at DESC";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, receiverPhone);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = createMessageFromResultSet(rs);
                messages.add(message);
            }
            
            System.out.println("✅ Loaded " + messages.size() + " messages for receiver: " + receiverPhone);
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding messages by receiver: " + e.getMessage());
        }
        
        return messages;
    }
    
    public List<Message> findByParticipants(String user1Phone, String user2Phone) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender_phone, receiver_phone, message_type, content, " +
                    "file_name, file_size, file_path, file_id, file_type, status, created_at " +
                    "FROM messages WHERE (sender_phone = ? AND receiver_phone = ?) " +
                    "OR (sender_phone = ? AND receiver_phone = ?) " +
                    "ORDER BY created_at ASC";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user1Phone);
            pstmt.setString(2, user2Phone);
            pstmt.setString(3, user2Phone);
            pstmt.setString(4, user1Phone);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = createMessageFromResultSet(rs);
                messages.add(message);
            }
            
            System.out.println("✅ Loaded " + messages.size() + " messages between " + 
                             user1Phone + " and " + user2Phone);
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding messages by participants: " + e.getMessage());
        }
        
        return messages;
    }
    
    public List<Message> findBySenderPhone(String senderPhone) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender_phone, receiver_phone, message_type, content, " +
                    "file_name, file_size, file_path, file_id, file_type, status, created_at " +
                    "FROM messages WHERE sender_phone = ? ORDER BY created_at DESC";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, senderPhone);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = createMessageFromResultSet(rs);
                messages.add(message);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding messages by sender: " + e.getMessage());
        }
        
        return messages;
    }
    
    public boolean updateStatus(String messageId, Message.MessageStatus status) {
        String sql = "UPDATE messages SET status = ? WHERE id = ?";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setString(2, messageId);
            
            int result = pstmt.executeUpdate();
            if (result > 0) {
                System.out.println("✅ Message status updated: " + messageId + " -> " + status);
                return true;
            } else {
                System.err.println("❌ Message not found for status update: " + messageId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating message status: " + e.getMessage());
            return false;
        }
    }
    
    public int getMessageCount() {
        String sql = "SELECT COUNT(*) FROM messages";
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("❌ Error getting message count: " + e.getMessage());
            return 0;
        }
    }
    
    public int getUnreadMessageCount(String receiverPhone) {
        String sql = "SELECT COUNT(*) FROM messages WHERE receiver_phone = ? AND status = 'DELIVERED'";
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, receiverPhone);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting unread message count: " + e.getMessage());
            return 0;
        }
    }
    
    public boolean deleteMessage(String messageId) {
        String sql = "DELETE FROM messages WHERE id = ?";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, messageId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting message: " + e.getMessage());
            return false;
        }
    }
    
    public List<Message> searchMessages(String userPhone, String query) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender_phone, receiver_phone, message_type, content, " +
                    "file_name, file_size, file_path, file_id, file_type, status, created_at " +
                    "FROM messages WHERE (sender_phone = ? OR receiver_phone = ?) " +
                    "AND (content LIKE ? OR file_name LIKE ?) " +
                    "ORDER BY created_at DESC";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userPhone);
            pstmt.setString(2, userPhone);
            pstmt.setString(3, "%" + query + "%");
            pstmt.setString(4, "%" + query + "%");
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = createMessageFromResultSet(rs);
                messages.add(message);
            }
            
            System.out.println("✅ Found " + messages.size() + " messages matching: " + query);
            
        } catch (SQLException e) {
            System.err.println("❌ Error searching messages: " + e.getMessage());
        }
        
        return messages;
    }
    
    // Helper method to create Message object from ResultSet
    private Message createMessageFromResultSet(ResultSet rs) throws SQLException {
        Message message = new Message(
            rs.getString("id"),
            rs.getString("sender_phone"),
            rs.getString("receiver_phone"),
            rs.getString("content"),
            Message.MessageType.valueOf(rs.getString("message_type")),
            Message.MessageStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
        
        // Set file properties if they exist
        String fileName = rs.getString("file_name");
        if (fileName != null && !fileName.isEmpty()) {
            message.setFileName(fileName);
            message.setFileSize(rs.getLong("file_size"));
            message.setFilePath(rs.getString("file_path"));
            message.setFileId(rs.getString("file_id"));
            message.setFileType(rs.getString("file_type"));
        }
        
        return message;
    }
    
    // Method to get recent messages for a user
    public List<Message> getRecentMessages(String userPhone, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, sender_phone, receiver_phone, message_type, content, " +
                    "file_name, file_size, file_path, file_id, file_type, status, created_at " +
                    "FROM messages WHERE sender_phone = ? OR receiver_phone = ? " +
                    "ORDER BY created_at DESC LIMIT ?";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userPhone);
            pstmt.setString(2, userPhone);
            pstmt.setInt(3, limit);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = createMessageFromResultSet(rs);
                messages.add(message);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting recent messages: " + e.getMessage());
        }
        
        return messages;
    }
}