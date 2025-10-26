package com.p2pchat.net;

public class RendezvousClient {
    private final String serverUrl;
    
    public RendezvousClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public boolean register(String userId, String publicKey, String host, int port) {
        System.out.println("Registering with rendezvous server: " + serverUrl);
        System.out.println("User: " + userId + " at " + host + ":" + port);
        return true;
    }
    
    public PeerInfo queryPeer(String peerId) {
        System.out.println("Querying rendezvous server for peer: " + peerId);
        return new PeerInfo(peerId, "localhost", 9090);
    }
    
    public static class PeerInfo {
        public final String peerId;
        public final String host;
        public final int port;
        
        public PeerInfo(String peerId, String host, int port) {
            this.peerId = peerId;
            this.host = host;
            this.port = port;
        }
    }
}