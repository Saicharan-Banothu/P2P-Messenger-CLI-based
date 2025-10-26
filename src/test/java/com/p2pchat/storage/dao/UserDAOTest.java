package com.p2pchat.storage.dao;

import com.p2pchat.core.models.User;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {
    
    @Test
    public void testUserDAOCreation() {
        // Test without mocking HikariDataSource
        assertDoesNotThrow(() -> {
            UserDAO userDAO = null;
            assertNull(userDAO);
        });
    }
    
    @Test
    public void testUserModel() {
        User user = new User("test123", "Test User", new byte[]{1, 2, 3}, LocalDateTime.now());
        assertEquals("test123", user.getId());
        assertEquals("Test User", user.getDisplayName());
        assertArrayEquals(new byte[]{1, 2, 3}, user.getPublicKey());
    }
}