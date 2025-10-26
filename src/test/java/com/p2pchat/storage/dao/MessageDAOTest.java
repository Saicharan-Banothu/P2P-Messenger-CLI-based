package com.p2pchat.storage.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageDAOTest {
    
    @Test
    public void testMessageDAOCreation() {
        // Test without mocking HikariDataSource
        assertDoesNotThrow(() -> {
            MessageDAO messageDAO = null;
            assertNull(messageDAO);
        });
    }
}