package com.p2pchat.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CryptoUtilTest {
    
    @Test
    public void testGenerateUserId() {
        String userId = CryptoUtil.generateUserId();
        assertNotNull(userId);
        assertFalse(userId.isEmpty());
        // Should be URL-safe base64 without padding
        assertFalse(userId.contains("="));
        assertFalse(userId.contains("+"));
        assertFalse(userId.contains("/"));
    }
    
    @Test
    public void testGenerateMessageId() {
        String messageId = CryptoUtil.generateMessageId();
        assertNotNull(messageId);
        assertFalse(messageId.isEmpty());
    }
    
    @Test
    public void testGenerateNonce() {
        byte[] nonce = CryptoUtil.generateNonce(16);
        assertNotNull(nonce);
        assertEquals(16, nonce.length);
    }
}