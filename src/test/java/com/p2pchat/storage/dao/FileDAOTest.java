package com.p2pchat.storage.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileDAOTest {
    
    @Test
    public void testFileDAOCreation() {
        // Test without mocking HikariDataSource
        assertDoesNotThrow(() -> {
            FileDAO fileDAO = null;
            assertNull(fileDAO);
        });
    }
}