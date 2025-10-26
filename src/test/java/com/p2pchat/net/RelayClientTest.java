package com.p2pchat.net;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RelayClientTest {
    
    @Test
    public void testRelayClientCreation() {
        RelayClient client = new RelayClient("http://localhost:8081");
        assertNotNull(client);
    }
    
    @Test
    public void testUploadChunk() {
        RelayClient client = new RelayClient("http://localhost:8081");
        boolean result = client.uploadChunk("chunk1", new byte[]{1, 2, 3});
        assertTrue(result);
    }
    
    @Test
    public void testStoreMessage() {
        RelayClient client = new RelayClient("http://localhost:8081");
        boolean result = client.storeMessage("msg1", new byte[]{1, 2, 3});
        assertTrue(result);
    }
}