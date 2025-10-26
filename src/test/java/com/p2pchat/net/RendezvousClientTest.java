package com.p2pchat.net;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RendezvousClientTest {
    
    @Test
    public void testRendezvousClientCreation() {
        RendezvousClient client = new RendezvousClient("http://localhost:8080");
        assertNotNull(client);
    }
    
    @Test
    public void testRegister() {
        RendezvousClient client = new RendezvousClient("http://localhost:8080");
        boolean result = client.register("user1", "publickey", "localhost", 9090);
        assertTrue(result);
    }
    
    @Test
    public void testQueryPeer() {
        RendezvousClient client = new RendezvousClient("http://localhost:8080");
        RendezvousClient.PeerInfo peerInfo = client.queryPeer("peer1");
        
        assertNotNull(peerInfo);
        assertEquals("peer1", peerInfo.peerId);
        assertEquals("localhost", peerInfo.host);
        assertEquals(9090, peerInfo.port);
    }
}