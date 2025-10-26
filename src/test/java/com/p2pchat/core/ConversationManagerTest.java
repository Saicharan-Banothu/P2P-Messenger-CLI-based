package com.p2pchat.core;

import com.p2pchat.core.models.User;
import com.p2pchat.storage.MySQLStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConversationManagerTest {
    private ConversationManager conversationManager;
    private MySQLStorage mockStorage;
    
    @BeforeEach
    public void setUp() {
        mockStorage = mock(MySQLStorage.class);
        conversationManager = new ConversationManager(mockStorage);
    }
    
    @Test
    public void testSetCurrentUser() {
        User user = new User("test123", "Test User", new byte[]{1, 2, 3}, java.time.LocalDateTime.now());
        conversationManager.setCurrentUser(user);
        
        assertEquals(user, conversationManager.getCurrentUser());
    }
}