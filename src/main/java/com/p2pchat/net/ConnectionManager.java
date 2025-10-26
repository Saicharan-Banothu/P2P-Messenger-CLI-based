package com.p2pchat.net;

import java.net.Socket;

public class ConnectionManager {
    
    public ConnectionManager() {
        System.out.println("🌐 Connection manager initialized");
    }
    
    public boolean testServerConnection(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean testServerConnection() {
        return testServerConnection("localhost", 8080);
    }
}