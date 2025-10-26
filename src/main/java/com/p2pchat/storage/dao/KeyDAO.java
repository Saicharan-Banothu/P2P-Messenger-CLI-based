package com.p2pchat.storage.dao;

import java.sql.*;
import java.util.Optional;

public class KeyDAO {
    
    public boolean saveEncryptionKey(String keyId, String userPhone, byte[] publicKey, 
                                   byte[] privateKey, byte[] symmetricKey) {
        // FIXED: Using private_key column name
        String sql = "INSERT INTO encryption_keys (id, user_phone, public_key, private_key, symmetric_key) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, keyId);
            pstmt.setString(2, userPhone);
            pstmt.setBytes(3, publicKey);
            pstmt.setBytes(4, privateKey); // FIXED: private_key column
            pstmt.setBytes(5, symmetricKey);
            
            int result = pstmt.executeUpdate();
            System.out.println("✅ Encryption key saved successfully for user: " + userPhone);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error saving encryption key: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public Optional<EncryptionKey> findByUserPhone(String userPhone) {
        String sql = "SELECT * FROM encryption_keys WHERE user_phone = ? AND is_active = TRUE";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userPhone);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                EncryptionKey key = new EncryptionKey(
                    rs.getString("id"),
                    rs.getString("user_phone"),
                    rs.getBytes("public_key"),
                    rs.getBytes("private_key"), // FIXED: private_key column
                    rs.getBytes("symmetric_key"),
                    rs.getString("key_type"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getBoolean("is_active")
                );
                return Optional.of(key);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding encryption key: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    public static class EncryptionKey {
        private final String id;
        private final String userPhone;
        private final byte[] publicKey;
        private final byte[] privateKey;
        private final byte[] symmetricKey;
        private final String keyType;
        private final java.time.LocalDateTime createdAt;
        private final boolean isActive;
        
        public EncryptionKey(String id, String userPhone, byte[] publicKey, 
                           byte[] privateKey, byte[] symmetricKey, 
                           String keyType, java.time.LocalDateTime createdAt, boolean isActive) {
            this.id = id;
            this.userPhone = userPhone;
            this.publicKey = publicKey;
            this.privateKey = privateKey;
            this.symmetricKey = symmetricKey;
            this.keyType = keyType;
            this.createdAt = createdAt;
            this.isActive = isActive;
        }
        
        // Getters
        public String getId() { return id; }
        public String getUserPhone() { return userPhone; }
        public byte[] getPublicKey() { return publicKey; }
        public byte[] getPrivateKey() { return privateKey; }
        public byte[] getSymmetricKey() { return symmetricKey; }
        public String getKeyType() { return keyType; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public boolean isActive() { return isActive; }
    }
}