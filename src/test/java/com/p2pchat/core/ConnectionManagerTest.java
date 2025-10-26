package com.p2pchat.core;

import com.p2pchat.net.ConnectionManager;  // CORRECT IMPORT
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConnectionManagerTest {
    private ConnectionManager connectionManager;
    
    @BeforeEach
    public void setUp() {
        connectionManager = new ConnectionManager();
    }
    
    @Test
    public void testConnectionManagerCreation() {
        assertNotNull(connectionManager);
    }
    
    @Test
    public void testGetOrCreateConnection() {
        // Test that method exists and doesn't throw exception
        assertDoesNotThrow(() -> {
            connectionManager.getOrCreateConnection("peer1", "localhost", 9090);
        });
    }
}