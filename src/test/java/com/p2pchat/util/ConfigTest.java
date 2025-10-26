package com.p2pchat.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {
    
    @Test
    public void testConfigCreation() {
        Config config = new Config();
        assertNotNull(config);
    }
    
    @Test
    public void testDefaultProperties() {
        Config config = new Config();
        
        // Test default values
        assertEquals("jdbc:mysql://localhost:3306/p2pchat", config.getDatabaseUrl());
        assertEquals("p2puser", config.getDatabaseUsername());
        assertEquals("p2ppass", config.getDatabasePassword());
        assertEquals(9090, config.getPeerPort());
    }
    
    @Test
    public void testIntPropertyWithDefault() {
        Config config = new Config();
        int value = config.getIntProperty("nonexistent.property", 42);
        assertEquals(42, value);
    }
}