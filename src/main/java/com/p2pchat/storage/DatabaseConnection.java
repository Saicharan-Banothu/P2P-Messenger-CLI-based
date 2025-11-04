package com.p2pchat.storage;

import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    private static String url;
    private static String username;
    private static String password;
    private static boolean initialized = false;
    
    // Load MySQL driver at class loading time
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found");
        }
    }
    
    public static void initialize(String jdbcUrl, String dbUsername, String dbPassword) {
        url = jdbcUrl;
        username = dbUsername;
        password = dbPassword;
        
        try {
            System.out.println("🔗 Setting up database...");
            System.out.println("📝 URL: " + url);
            System.out.println("👤 User: " + username);
            
            // Test connection first
            try (Connection conn = getFreshConnection()) {
                System.out.println("✅ Database connection test successful");
                
                // Reset and recreate tables to ensure correct schema
                resetAndCreateTables(conn);
                initialized = true;
            }
            
            System.out.println("✅ Database setup completed successfully");
            
        } catch (Exception e) {
            System.out.println("❌ Database setup failed: " + e.getMessage());
            System.out.println("💡 Running in offline mode without database");
            initialized = false;
        }
    }
    
    // Rest of your existing DatabaseConnection code remains the same...
    private static Connection getFreshConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        props.setProperty("useSSL", "false");
        props.setProperty("serverTimezone", "UTC");
        props.setProperty("autoReconnect", "true");
        props.setProperty("allowPublicKeyRetrieval", "true");
        
        return DriverManager.getConnection(url, props);
    }
    
    private static void resetAndCreateTables(Connection conn) throws SQLException {
        System.out.println("🔄 Creating database tables with SIMPLIFIED schema...");
        
        // Drop tables in correct order
        String[] dropTables = {
            "DROP TABLE IF EXISTS messages",
            "DROP TABLE IF EXISTS file_chunks",
            "DROP TABLE IF EXISTS contacts", 
            "DROP TABLE IF EXISTS conversations",
            "DROP TABLE IF EXISTS p2p_connections",
            "DROP TABLE IF EXISTS key_store",
            "DROP TABLE IF EXISTS encryption_keys",
            "DROP TABLE IF EXISTS users"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String sql : dropTables) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    // Ignore drop errors
                }
            }
        }
        
        // Create tables with SIMPLIFIED file_chunks table (NO CHUNK DATA)
        String[] tables = {
            // Users table
            "CREATE TABLE users (" +
                "phone_number VARCHAR(10) PRIMARY KEY, " +
                "display_name VARCHAR(100) NOT NULL, " +
                "public_key_fingerprint VARCHAR(255), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "profile_status VARCHAR(200) DEFAULT 'Hey there! I am using P2PChat')",
            
            // Contacts table
            "CREATE TABLE contacts (" +
                "owner_phone VARCHAR(10) NOT NULL, " +
                "contact_phone VARCHAR(10) NOT NULL, " +
                "nickname VARCHAR(100), " +
                "status VARCHAR(20) DEFAULT 'PENDING', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY(owner_phone, contact_phone))",
            
            // Conversations table
            "CREATE TABLE conversations (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user1_phone VARCHAR(10) NOT NULL, " +
                "user2_phone VARCHAR(10) NOT NULL, " +
                "last_message TEXT, " +
                "last_message_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "unread_count INT DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            
            // Messages table
            "CREATE TABLE messages (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "conversation_id BIGINT, " +
                "sender_phone VARCHAR(10) NOT NULL, " +
                "receiver_phone VARCHAR(10) NOT NULL, " +
                "message_type VARCHAR(20) DEFAULT 'TEXT', " +
                "content TEXT, " +
                "file_name VARCHAR(255), " +
                "file_size BIGINT, " +
                "file_path VARCHAR(500), " +
                "file_id VARCHAR(36), " +
                "file_type VARCHAR(50), " +
                "encryption_key_id VARCHAR(36), " +
                "status VARCHAR(20) DEFAULT 'SENT', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            
            // SIMPLIFIED File metadata table - NO CHUNK DATA
            "CREATE TABLE file_chunks (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "file_id VARCHAR(36) NOT NULL, " +
                "file_name VARCHAR(255) NOT NULL, " +
                "file_path VARCHAR(500), " +
                "file_size BIGINT NOT NULL, " +
                "file_type VARCHAR(50) NOT NULL, " +
                "owner_phone VARCHAR(10) NOT NULL, " +
                "receiver_phone VARCHAR(10), " +
                "uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_delivered BOOLEAN DEFAULT FALSE)",
            
            // Encryption keys table
            "CREATE TABLE encryption_keys (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "user_phone VARCHAR(10) NOT NULL, " +
                "public_key BLOB NOT NULL, " +
                "private_key BLOB, " +
                "symmetric_key BLOB, " +
                "key_type VARCHAR(20) DEFAULT 'RSA', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_active BOOLEAN DEFAULT TRUE)",
            
            // Key store table
            "CREATE TABLE key_store (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "key_owner VARCHAR(10) NOT NULL, " +
                "key_data BLOB NOT NULL, " +
                "key_type VARCHAR(20) NOT NULL, " +
                "key_purpose VARCHAR(50), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "expires_at TIMESTAMP, " +
                "is_valid BOOLEAN DEFAULT TRUE)",
            
            // P2P connections table
            "CREATE TABLE p2p_connections (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "user1_phone VARCHAR(10) NOT NULL, " +
                "user2_phone VARCHAR(10) NOT NULL, " +
                "connection_type VARCHAR(20) DEFAULT 'DIRECT', " +
                "local_address VARCHAR(45), " +
                "remote_address VARCHAR(45), " +
                "local_port INT, " +
                "remote_port INT, " +
                "session_key BLOB, " +
                "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "total_bytes_sent BIGINT DEFAULT 0, " +
                "total_bytes_received BIGINT DEFAULT 0)"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
            System.out.println("✅ All tables created with SIMPLIFIED schema (NO CHUNK DATA)");
        }
        
        createIndexes(conn);
    }
    
    private static void createIndexes(Connection conn) throws SQLException {
        String[] indexes = {
            "CREATE INDEX idx_messages_sender ON messages(sender_phone)",
            "CREATE INDEX idx_messages_receiver ON messages(receiver_phone)",
            "CREATE INDEX idx_messages_conversation ON messages(conversation_id)",
            "CREATE INDEX idx_messages_created ON messages(created_at)",
            "CREATE INDEX idx_file_chunks_file_id ON file_chunks(file_id)",
            "CREATE INDEX idx_file_chunks_owner ON file_chunks(owner_phone)",
            "CREATE INDEX idx_encryption_keys_user ON encryption_keys(user_phone)",
            "CREATE INDEX idx_p2p_connections_users ON p2p_connections(user1_phone, user2_phone)",
            "CREATE INDEX idx_p2p_connections_status ON p2p_connections(status)",
            "CREATE INDEX idx_conversations_users ON conversations(user1_phone, user2_phone)"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String sql : indexes) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    // Ignore index creation errors
                }
            }
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return getFreshConnection();
    }
    
    public static boolean isConnected() {
        try (Connection conn = getFreshConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static void close() {
        // No need to close static connection since we create fresh ones
    }
    
    // Method for admin to reset database
    public static void resetDatabase() throws SQLException {
        try (Connection conn = getFreshConnection()) {
            resetAndCreateTables(conn);
        }
    }
}