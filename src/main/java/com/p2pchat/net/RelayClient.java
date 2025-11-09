package com.p2pchat.net;

public class RelayClient {
    private final String serverUrl;
    
    public RelayClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public boolean uploadChunk(String chunkId, byte[] data) {
        System.out.println("Uploading chunk " + chunkId + " to relay server: " + serverUrl);
        return true;
    }
    
    public byte[] downloadChunk(String chunkId) {
        System.out.println("Downloading chunk " + chunkId + " from relay server: " + serverUrl);
        return new byte[0];
    }
    
    public boolean storeMessage(String messageId, byte[] encryptedMessage) {
        System.out.println("Storing message " + messageId + " on relay server: " + serverUrl);
        return true;
    }
    
    public byte[] retrieveMessage(String messageId) {
        System.out.println("Retrieving message " + messageId + " from relay server: " + serverUrl);
        return new byte[0];
    }
    
    // Added method to use serverUrl field
    public String getServerUrl() {
        return serverUrl;
    }
}