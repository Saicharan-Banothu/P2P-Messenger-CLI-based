package com.p2pchat.util;

import com.p2pchat.crypto.KeyManager;
import com.p2pchat.storage.DatabaseConnection;
import com.p2pchat.storage.MySQLStorage;
import java.sql.*;
import java.util.UUID;

public class DatabasePopulator {
    
    public static void populateEncryptionTables(MySQLStorage storage) {
        System.out.println("🔄 Populating encryption tables...");
        
        try {
            // Populate encryption_keys table
            populateEncryptionKeys(storage);
            
            // Populate key_store table  
            populateKeyStore(storage);
            
            // Populate p2p_connections table
            populateP2PConnections(storage);
            
            System.out.println("✅ Encryption tables populated successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error populating encryption tables: " + e.getMessage());
        }
    }
    
    private static void populateEncryptionKeys(MySQLStorage storage) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if table is empty
            String checkSql = "SELECT COUNT(*) FROM encryption_keys";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Table is empty, insert sample data
                    String insertSql = "INSERT INTO encryption_keys (id, user_phone, public_key, key_type) " +
                                     "VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                        pstmt.setString(1, UUID.randomUUID().toString());
                        pstmt.setString(2, "system_admin");
                        pstmt.setBytes(3, "sample_public_key".getBytes());
                        pstmt.setString(4, "RSA");
                        pstmt.executeUpdate();
                        System.out.println("✅ Added sample data to encryption_keys");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error populating encryption_keys: " + e.getMessage());
        }
    }
    
    private static void populateKeyStore(MySQLStorage storage) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM key_store";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertSql = "INSERT INTO key_store (id, key_owner, key_data, key_type, key_purpose) " +
                                     "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                        pstmt.setString(1, UUID.randomUUID().toString());
                        pstmt.setString(2, "system");
                        pstmt.setBytes(3, "sample_key_data".getBytes());
                        pstmt.setString(4, "AES");
                        pstmt.setString(5, "SESSION");
                        pstmt.executeUpdate();
                        System.out.println("✅ Added sample data to key_store");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error populating key_store: " + e.getMessage());
        }
    }
    
    private static void populateP2PConnections(MySQLStorage storage) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM p2p_connections";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertSql = "INSERT INTO p2p_connections (id, user1_phone, user2_phone, connection_type) " +
                                     "VALUES (?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                        pstmt.setString(1, UUID.randomUUID().toString());
                        pstmt.setString(2, "1234567890");
                        pstmt.setString(3, "9876543210");
                        pstmt.setString(4, "DIRECT");
                        pstmt.executeUpdate();
                        System.out.println("✅ Added sample data to p2p_connections");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error populating p2p_connections: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        try {
            MySQLStorage storage = new MySQLStorage();
            populateEncryptionTables(storage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}