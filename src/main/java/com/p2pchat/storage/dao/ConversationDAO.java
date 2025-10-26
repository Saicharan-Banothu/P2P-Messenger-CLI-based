package com.p2pchat.storage.dao;

import com.p2pchat.storage.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO {
    
    public Long findOrCreateConversation(String user1Phone, String user2Phone) {
        // Try to find existing conversation
        String findSql = "SELECT id FROM conversations WHERE (user1_phone = ? AND user2_phone = ?) OR (user1_phone = ? AND user2_phone = ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(findSql)) {
            
            stmt.setString(1, user1Phone);
            stmt.setString(2, user2Phone);
            stmt.setString(3, user2Phone);
            stmt.setString(4, user1Phone);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("id");
            }
        } catch (SQLException e) {
            System.err.println("Error finding conversation: " + e.getMessage());
        }
        
        // Create new conversation
        String insertSql = "INSERT INTO conversations (user1_phone, user2_phone, last_message, last_message_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user1Phone);
            stmt.setString(2, user2Phone);
            stmt.setString(3, "Conversation started");
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating conversation: " + e.getMessage());
        }
        
        return 1L; // Default conversation ID
    }
    
    public void updateConversation(Long conversationId, String lastMessage, LocalDateTime lastMessageTime) {
        String sql = "UPDATE conversations SET last_message = ?, last_message_time = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, lastMessage);
            stmt.setTimestamp(2, Timestamp.valueOf(lastMessageTime));
            stmt.setLong(3, conversationId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating conversation: " + e.getMessage());
        }
    }
    
    public List<Object[]> findByUserPhone(String userPhone) {
        List<Object[]> conversations = new ArrayList<>();
        String sql = "SELECT user1_phone, user2_phone, last_message, last_message_time, unread_count FROM conversations WHERE user1_phone = ? OR user2_phone = ? ORDER BY last_message_time DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userPhone);
            stmt.setString(2, userPhone);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String user1 = rs.getString("user1_phone");
                String user2 = rs.getString("user2_phone");
                String otherUser = user1.equals(userPhone) ? user2 : user1;
                String lastMessage = rs.getString("last_message");
                LocalDateTime lastTime = rs.getTimestamp("last_message_time").toLocalDateTime();
                int unread = rs.getInt("unread_count");
                
                conversations.add(new Object[]{otherUser, lastMessage, lastTime, unread});
            }
        } catch (SQLException e) {
            System.err.println("Error fetching conversations: " + e.getMessage());
        }
        
        return conversations;
    }
}