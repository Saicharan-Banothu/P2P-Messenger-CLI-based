package com.p2pchat.storage.dao;

import com.p2pchat.core.models.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserDAO {
    
    public boolean save(User user) {
        // FIXED: Using public_key_fingerprint NOT public_key
        String sql = "INSERT INTO users (phone_number, display_name, public_key_fingerprint, created_at, last_seen, profile_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getPhoneNumber());
            pstmt.setString(2, user.getDisplayName());
            pstmt.setString(3, user.getPublicKeyFingerprint());
            pstmt.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt()));
            pstmt.setTimestamp(5, Timestamp.valueOf(user.getLastSeen()));
            pstmt.setString(6, user.getProfileStatus());
            
            int result = pstmt.executeUpdate();
            if (result > 0) {
                System.out.println("✅ User saved successfully: " + user.getPhoneNumber());
                return true;
            } else {
                System.err.println("❌ Failed to save user: " + user.getPhoneNumber());
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database error saving user: " + e.getMessage());
            return false;
        }
    }
    
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        // FIXED: Using public_key_fingerprint NOT public_key
        String sql = "SELECT phone_number, display_name, public_key_fingerprint, created_at, last_seen, profile_status " +
                    "FROM users WHERE phone_number = ?";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, phoneNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getString("phone_number"),
                    rs.getString("display_name"),
                    rs.getString("public_key_fingerprint"), // CORRECT column name
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("last_seen").toLocalDateTime(),
                    rs.getString("profile_status")
                );
                return Optional.of(user);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding user: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    public void updateLastSeen(String phoneNumber) {
        String sql = "UPDATE users SET last_seen = CURRENT_TIMESTAMP WHERE phone_number = ?";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, phoneNumber);
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("✅ Updated last seen for user: " + phoneNumber);
            } else {
                System.err.println("❌ User not found for last seen update: " + phoneNumber);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating last seen: " + e.getMessage());
        }
    }
    
    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("❌ Error getting user count: " + e.getMessage());
            return 0;
        }
    }
    
    public boolean updateProfileStatus(String phoneNumber, String newStatus) {
        String sql = "UPDATE users SET profile_status = ? WHERE phone_number = ?";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus);
            pstmt.setString(2, phoneNumber);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating profile status: " + e.getMessage());
            return false;
        }
    }
    
    public boolean userExists(String phoneNumber) {
        return findByPhoneNumber(phoneNumber).isPresent();
    }
}