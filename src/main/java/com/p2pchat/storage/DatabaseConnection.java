package com.p2pchat.storage;

import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    private static String url;
    private static String username;
    private static String password;
    private static boolean initialized = false;
    
    public static void clearDatabase() {
        if (!initialized) {
            System.out.println("❌ Database not initialized");
            return;
        }
        
        try (Connection conn = getFreshConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("🗑️  CLEARING ALL DATABASE DATA...");
            
            // Disable foreign key checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // Clear all tables
            String[] tables = {
                "messages", "conversations", "contacts", "file_chunks", 
                "p2p_connections", "encryption_keys", "users"
            };
            
            for (String table : tables) {
                try {
                    stmt.execute("TRUNCATE TABLE " + table);
                    System.out.println("✅ Cleared table: " + table);
                } catch (SQLException e) {
                    System.out.println("ℹ️  Table doesn't exist or already cleared: " + table);
                }
            }
            
            // Re-enable foreign key checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            
            System.out.println("✅ Database cleared successfully!");
            System.out.println("💡 All user data, messages, contacts, and files have been removed.");
            
        } catch (SQLException e) {
            System.err.println("❌ Error clearing database: " + e.getMessage());
        }
    }
    
    public static void initialize(String jdbcUrl, String dbUsername, String dbPassword) {
        url = jdbcUrl;
        username = dbUsername;
        password = dbPassword;
        
        try {
            System.out.println("🔗 Setting up database...");
            
            // Get a connection and setup tables
            try (Connection conn = getFreshConnection()) {
                // Check if tables already exist
                if (!tablesExist(conn)) {
                    createTables(conn);
                    System.out.println("✅ Database tables created successfully");
                } else {
                    System.out.println("✅ Database tables already exist - preserving data");
                    optimizeTables(conn); // Optimize existing tables
                }
                initialized = true;
            }
            
            System.out.println("✅ Database setup completed successfully");
            
        } catch (Exception e) {
            System.out.println("❌ Database setup failed: " + e.getMessage());
            initialized = false;
        }
    }
    
    private static Connection getFreshConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        props.setProperty("useSSL", "false");
        props.setProperty("serverTimezone", "UTC");
        props.setProperty("autoReconnect", "true");
        props.setProperty("characterEncoding", "UTF-8");
        
        return DriverManager.getConnection(url, props);
    }
    
    private static boolean tablesExist(Connection conn) throws SQLException {
        // Check if the main 'users' table exists
        String checkTableSQL = 
            "SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() AND table_name = 'users'";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkTableSQL)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    private static void createTables(Connection conn) throws SQLException {
        System.out.println("🔄 Creating database tables...");
        
        // Create optimized tables with key fingerprints
        String[] tables = {
            // Users table with VARCHAR public_key instead of BLOB
            "CREATE TABLE IF NOT EXISTS users (" +
                "phone_number VARCHAR(10) PRIMARY KEY, " +
                "display_name VARCHAR(100) NOT NULL, " +
                "public_key VARCHAR(64), " +  // Changed from BLOB to VARCHAR for fingerprints
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "profile_status VARCHAR(200) DEFAULT 'Hey there! I am using P2PChat', " +
                "INDEX idx_users_phone (phone_number))",
            
            // Contacts table
            "CREATE TABLE IF NOT EXISTS contacts (" +
                "owner_phone VARCHAR(10) NOT NULL, " +
                "contact_phone VARCHAR(10) NOT NULL, " +
                "nickname VARCHAR(100), " +
                "status VARCHAR(20) DEFAULT 'PENDING', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY(owner_phone, contact_phone), " +
                "INDEX idx_contacts_owner (owner_phone), " +
                "INDEX idx_contacts_contact (contact_phone))",
            
            // Conversations table
            "CREATE TABLE IF NOT EXISTS conversations (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user1_phone VARCHAR(10) NOT NULL, " +
                "user2_phone VARCHAR(10) NOT NULL, " +
                "last_message TEXT, " +
                "last_message_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "unread_count INT DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_conv_user1 (user1_phone), " +
                "INDEX idx_conv_user2 (user2_phone), " +
                "INDEX idx_conv_time (last_message_time))",
            
            // Messages table with encryption support
            "CREATE TABLE IF NOT EXISTS messages (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "conversation_id BIGINT, " +
                "sender_phone VARCHAR(10) NOT NULL, " +
                "receiver_phone VARCHAR(10) NOT NULL, " +
                "message_type VARCHAR(20) DEFAULT 'TEXT', " +
                "content TEXT, " +  // Can store encrypted content
                "file_name VARCHAR(255), " +
                "file_size BIGINT, " +
                "file_path VARCHAR(500), " +
                "encryption_key VARCHAR(64), " +  // New: Store encryption key fingerprint
                "is_encrypted BOOLEAN DEFAULT FALSE, " +  // New: Flag for encrypted messages
                "p2p_delivered BOOLEAN DEFAULT FALSE, " +  // New: Flag for P2P delivery
                "status VARCHAR(20) DEFAULT 'SENT', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_msg_conversation (conversation_id), " +
                "INDEX idx_msg_sender (sender_phone), " +
                "INDEX idx_msg_receiver (receiver_phone), " +
                "INDEX idx_msg_created (created_at))",
            
            // Enhanced file_chunks table for P2P file sharing
            "CREATE TABLE IF NOT EXISTS file_chunks (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "file_id VARCHAR(36) NOT NULL, " +
                "file_name VARCHAR(255) NOT NULL, " +
                "file_path VARCHAR(500) NOT NULL, " +
                "file_size BIGINT NOT NULL, " +
                "file_type VARCHAR(50) NOT NULL, " +
                "chunk_index INT DEFAULT 0, " +  // New: For file chunking
                "total_chunks INT DEFAULT 1, " +  // New: Total chunks for this file
                "chunk_hash VARCHAR(64), " +  // New: Hash for integrity verification
                "owner_phone VARCHAR(10) NOT NULL, " +
                "recipient_phone VARCHAR(10), " +  // New: Specific recipient for P2P
                "is_encrypted BOOLEAN DEFAULT FALSE, " +  // New: Encryption flag
                "uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_files_owner (owner_phone), " +
                "INDEX idx_files_recipient (recipient_phone), " +
                "INDEX idx_files_id (file_id))",
            
            // New table for P2P connection tracking
            "CREATE TABLE IF NOT EXISTS p2p_connections (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_phone VARCHAR(10) NOT NULL, " +
                "peer_phone VARCHAR(10) NOT NULL, " +
                "peer_host VARCHAR(255), " +
                "peer_port INT, " +
                "last_connected TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "connection_count INT DEFAULT 1, " +
                "is_active BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_p2p_user (user_phone), " +
                "INDEX idx_p2p_peer (peer_phone), " +
                "UNIQUE KEY unique_user_peer (user_phone, peer_phone))",
            
            // New table for encryption keys
            "CREATE TABLE IF NOT EXISTS encryption_keys (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_phone VARCHAR(10) NOT NULL, " +
                "key_fingerprint VARCHAR(64) NOT NULL, " +
                "key_type VARCHAR(20) DEFAULT 'RSA', " +
                "public_key TEXT, " +  // Store full public key if needed
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "INDEX idx_keys_user (user_phone), " +
                "INDEX idx_keys_fingerprint (key_fingerprint))"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String sql : tables) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    System.err.println("⚠️  Table creation warning: " + e.getMessage());
                }
            }
        }
        
        // Create optimized indexes
        createOptimizedIndexes(conn);
    }
    
    private static void optimizeTables(Connection conn) throws SQLException {
        System.out.println("🔄 Optimizing database tables...");
        
        String[] optimizations = {
            // Convert public_key from BLOB to VARCHAR if it exists as BLOB
            "ALTER TABLE users MODIFY COLUMN public_key VARCHAR(64)",
            
            // Add new columns to existing tables if they don't exist
            "ALTER TABLE messages ADD COLUMN IF NOT EXISTS encryption_key VARCHAR(64)",
            "ALTER TABLE messages ADD COLUMN IF NOT EXISTS is_encrypted BOOLEAN DEFAULT FALSE",
            "ALTER TABLE messages ADD COLUMN IF NOT EXISTS p2p_delivered BOOLEAN DEFAULT FALSE",
            
            "ALTER TABLE file_chunks ADD COLUMN IF NOT EXISTS chunk_index INT DEFAULT 0",
            "ALTER TABLE file_chunks ADD COLUMN IF NOT EXISTS total_chunks INT DEFAULT 1", 
            "ALTER TABLE file_chunks ADD COLUMN IF NOT EXISTS chunk_hash VARCHAR(64)",
            "ALTER TABLE file_chunks ADD COLUMN IF NOT EXISTS recipient_phone VARCHAR(10)",
            "ALTER TABLE file_chunks ADD COLUMN IF NOT EXISTS is_encrypted BOOLEAN DEFAULT FALSE",
            
            // Create new tables if they don't exist
            "CREATE TABLE IF NOT EXISTS p2p_connections (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_phone VARCHAR(10) NOT NULL, " +
                "peer_phone VARCHAR(10) NOT NULL, " +
                "peer_host VARCHAR(255), " +
                "peer_port INT, " +
                "last_connected TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "connection_count INT DEFAULT 1, " +
                "is_active BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_p2p_user (user_phone), " +
                "INDEX idx_p2p_peer (peer_phone), " +
                "UNIQUE KEY unique_user_peer (user_phone, peer_phone))",
                
            "CREATE TABLE IF NOT EXISTS encryption_keys (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_phone VARCHAR(10) NOT NULL, " +
                "key_fingerprint VARCHAR(64) NOT NULL, " +
                "key_type VARCHAR(20) DEFAULT 'RSA', " +
                "public_key TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "INDEX idx_keys_user (user_phone), " +
                "INDEX idx_keys_fingerprint (key_fingerprint))"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String sql : optimizations) {
                try {
                    stmt.execute(sql);
                    System.out.println("✅ Applied optimization: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                } catch (SQLException e) {
                    System.out.println("ℹ️  Optimization already applied or not needed: " + e.getMessage());
                }
            }
        }
        
        // Create optimized indexes
        createOptimizedIndexes(conn);
    }
    
    private static void createOptimizedIndexes(Connection conn) throws SQLException {
        System.out.println("📊 Creating optimized indexes...");
        
        String[] indexes = {
            // Users table indexes
            "CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number)",
            "CREATE INDEX IF NOT EXISTS idx_users_last_seen ON users(last_seen)",
            
            // Contacts table indexes  
            "CREATE INDEX IF NOT EXISTS idx_contacts_owner ON contacts(owner_phone)",
            "CREATE INDEX IF NOT EXISTS idx_contacts_contact ON contacts(contact_phone)",
            "CREATE INDEX IF NOT EXISTS idx_contacts_status ON contacts(status)",
            
            // Conversations table indexes
            "CREATE INDEX IF NOT EXISTS idx_conv_user1 ON conversations(user1_phone)",
            "CREATE INDEX IF NOT EXISTS idx_conv_user2 ON conversations(user2_phone)", 
            "CREATE INDEX IF NOT EXISTS idx_conv_time ON conversations(last_message_time)",
            "CREATE INDEX IF NOT EXISTS idx_conv_composite ON conversations(user1_phone, user2_phone)",
            
            // Messages table indexes
            "CREATE INDEX IF NOT EXISTS idx_msg_conversation ON messages(conversation_id)",
            "CREATE INDEX IF NOT EXISTS idx_msg_sender ON messages(sender_phone)",
            "CREATE INDEX IF NOT EXISTS idx_msg_receiver ON messages(receiver_phone)",
            "CREATE INDEX IF NOT EXISTS idx_msg_created ON messages(created_at)",
            "CREATE INDEX IF NOT EXISTS idx_msg_status ON messages(status)",
            "CREATE INDEX IF NOT EXISTS idx_msg_encrypted ON messages(is_encrypted)",
            
            // File chunks indexes
            "CREATE INDEX IF NOT EXISTS idx_files_owner ON file_chunks(owner_phone)",
            "CREATE INDEX IF NOT EXISTS idx_files_recipient ON file_chunks(recipient_phone)",
            "CREATE INDEX IF NOT EXISTS idx_files_id ON file_chunks(file_id)",
            "CREATE INDEX IF NOT EXISTS idx_files_type ON file_chunks(file_type)",
            
            // P2P connections indexes
            "CREATE INDEX IF NOT EXISTS idx_p2p_user ON p2p_connections(user_phone)",
            "CREATE INDEX IF NOT EXISTS idx_p2p_peer ON p2p_connections(peer_phone)",
            "CREATE INDEX IF NOT EXISTS idx_p2p_active ON p2p_connections(is_active)",
            
            // Encryption keys indexes
            "CREATE INDEX IF NOT EXISTS idx_keys_user ON encryption_keys(user_phone)",
            "CREATE INDEX IF NOT EXISTS idx_keys_fingerprint ON encryption_keys(key_fingerprint)",
            "CREATE INDEX IF NOT EXISTS idx_keys_active ON encryption_keys(is_active)"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String sql : indexes) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    System.out.println("ℹ️  Index already exists: " + e.getMessage());
                }
            }
        }
    }
    
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Database not initialized. Call initialize() first.");
        }
        return getFreshConnection();
    }
    
    public static boolean isConnected() {
        try (Connection conn = getFreshConnection()) {
            return conn.isValid(2); // 2 second timeout
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static boolean isInitialized() {
        return initialized;
    }
    
    public static void runMaintenance() {
        if (!initialized) return;
        
        try (Connection conn = getFreshConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("🧹 Running database maintenance...");
            
            // Clean up old P2P connections (older than 1 day)
            stmt.execute("DELETE FROM p2p_connections WHERE last_connected < DATE_SUB(NOW(), INTERVAL 1 DAY)");
            
            // Update user last_seen for active sessions
            stmt.execute("UPDATE users SET last_seen = CURRENT_TIMESTAMP WHERE phone_number IN " +
                        "(SELECT DISTINCT user_phone FROM p2p_connections WHERE is_active = TRUE)");
            
            // Optimize tables
            stmt.execute("OPTIMIZE TABLE users, contacts, conversations, messages, file_chunks, p2p_connections, encryption_keys");
            
            System.out.println("✅ Database maintenance completed");
            
        } catch (SQLException e) {
            System.err.println("❌ Database maintenance failed: " + e.getMessage());
        }
    }
    
    public static void close() {
        // Run maintenance before closing
        runMaintenance();
        System.out.println("🔒 Database connection pool closed");
    }
    
    // New method for database statistics
    public static void printDatabaseStats() {
        if (!initialized) {
            System.out.println("❌ Database not initialized");
            return;
        }
        
        try (Connection conn = getFreshConnection();
             Statement stmt = conn.createStatement()) {
            
            String[] statsQueries = {
                "SELECT 'Users' as table_name, COUNT(*) as count FROM users",
                "SELECT 'Contacts' as table_name, COUNT(*) as count FROM contacts", 
                "SELECT 'Conversations' as table_name, COUNT(*) as count FROM conversations",
                "SELECT 'Messages' as table_name, COUNT(*) as count FROM messages",
                "SELECT 'Files' as table_name, COUNT(*) as count FROM file_chunks",
                "SELECT 'P2P Connections' as table_name, COUNT(*) as count FROM p2p_connections WHERE is_active = TRUE",
                "SELECT 'Encrypted Messages' as table_name, COUNT(*) as count FROM messages WHERE is_encrypted = TRUE",
                "SELECT 'P2P Delivered' as table_name, COUNT(*) as count FROM messages WHERE p2p_delivered = TRUE"
            };
            
            System.out.println("\n📊 DATABASE STATISTICS");
            System.out.println("═".repeat(40));
            
            for (String query : statsQueries) {
                try (ResultSet rs = stmt.executeQuery(query)) {
                    if (rs.next()) {
                        String tableName = rs.getString("table_name");
                        long count = rs.getLong("count");
                        System.out.printf("%-20s: %d%n", tableName, count);
                    }
                }
            }
            
            // Get database size
            try (ResultSet rs = stmt.executeQuery(
                "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) as size_mb " +
                "FROM information_schema.tables " +
                "WHERE table_schema = DATABASE()")) {
                if (rs.next()) {
                    double sizeMB = rs.getDouble("size_mb");
                    System.out.printf("%-20s: %.2f MB%n", "Database Size", sizeMB);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting database stats: " + e.getMessage());
        }
    }
}