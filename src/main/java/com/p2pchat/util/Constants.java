package com.p2pchat.util;

public class Constants {
    // Application constants
    public static final String APP_NAME = "P2PChat-CLI";
    public static final String APP_VERSION = "1.0.0";
    
    // Database constants
    public static final int MAX_DB_CONNECTIONS = 10;
    public static final int DB_CONNECTION_TIMEOUT_MS = 30000;
    
    // Network constants
    public static final int DEFAULT_PEER_PORT = 9090;
    public static final int MAX_MESSAGE_SIZE = 10 * 1024 * 1024; // 10MB
    
    // Crypto constants
    public static final int KEY_SIZE = 256;
    public static final String KEY_ALGORITHM = "X25519";
    
    // File transfer constants
    public static final int CHUNK_SIZE = 64 * 1024; // 64KB chunks
    
    private Constants() {
        // Utility class
    }
}