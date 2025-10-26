package com.p2pchat.storage.dao;

import com.p2pchat.core.models.User;
import com.p2pchat.storage.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserDAO {
    
    public boolean save(User user) {
        System.out.println("💾 Attempting to save user: " + user.getDisplayName() + " (" + user.getPhoneNumber() + ")");
        
        String sql = "INSERT INTO users (phone_number, display_name, public_key, created_at, last_seen, profile_status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getPhoneNumber());
            stmt.setString(2, user.getDisplayName());
            stmt.setString(3, user.getPublicKeyFingerprint());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt())); // FIXED: Use actual timestamp
            stmt.setTimestamp(5, Timestamp.valueOf(user.getLastSeen()));   // FIXED: Use actual timestamp
            stmt.setString(6, user.getProfileStatus());
            
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                System.out.println("✅ User saved successfully!");
            } else {
                System.out.println("❌ User save failed - no rows affected");
            }
            return result;
            
        } catch (SQLException e) {
            System.err.println("❌ Database error saving user: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            return false;
        }
    }
    
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        String sql = "SELECT phone_number, display_name, public_key, created_at, last_seen, profile_status FROM users WHERE phone_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String phone = rs.getString("phone_number");
                String displayName = rs.getString("display_name");
                String publicKeyFingerprint = rs.getString("public_key");
                
                // Handle null timestamps - create new User with proper defaults if null
                Timestamp createdAtStamp = rs.getTimestamp("created_at");
                Timestamp lastSeenStamp = rs.getTimestamp("last_seen");
                
                LocalDateTime createdAt = (createdAtStamp != null) ? createdAtStamp.toLocalDateTime() : LocalDateTime.now();
                LocalDateTime lastSeen = (lastSeenStamp != null) ? lastSeenStamp.toLocalDateTime() : LocalDateTime.now();
                
                String profileStatus = rs.getString("profile_status");
                if (profileStatus == null) {
                    profileStatus = "Hey there! I am using P2PChat";
                }
                
                // Create user with the values from database (or defaults if null)
                User user = new User(phone, displayName, publicKeyFingerprint, createdAt, lastSeen, profileStatus);
                return Optional.of(user);
            }
        } catch (Exception e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    public boolean updateLastSeen(String phoneNumber) {
        String sql = "UPDATE users SET last_seen = ? WHERE phone_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, phoneNumber);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Updated last seen for user: " + phoneNumber);
                return true;
            } else {
                System.out.println("⚠️  No user found to update last seen: " + phoneNumber);
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating last seen: " + e.getMessage());
            return false;
        }
    }
    
    // New method to update profile status
    public boolean updateProfileStatus(String phoneNumber, String status) {
        String sql = "UPDATE users SET profile_status = ?, last_seen = ? WHERE phone_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, phoneNumber);
            
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating profile status: " + e.getMessage());
            return false;
        }
    }
}