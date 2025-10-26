package com.p2pchat.core;

import com.p2pchat.core.models.MessageManager;
import com.p2pchat.core.models.User;
import com.p2pchat.crypto.KeyManager;
import com.p2pchat.net.ConnectionManager;
import com.p2pchat.storage.MySQLStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MessageManagerTest {
    private MessageManager messageManager;
    private MySQLStorage mockStorage;
    private KeyManager mockKeyManager;
    private ConnectionManager mockConnectionManager;
    
    @BeforeEach
    public void setUp() {
        mockStorage = mock(MySQLStorage.class);
        mockKeyManager = mock(KeyManager.class);
        mockConnectionManager = mock(ConnectionManager.class);
        messageManager = new MessageManager(mockStorage, mockKeyManager, mockConnectionManager);
    }
    
    @Test
    public void testSetCurrentUser() {
        User user = new User("test123", "Test User", new byte[]{1, 2, 3}, java.time.LocalDateTime.now());
        messageManager.setCurrentUser(user);
        // Verify no exception thrown
    }
}