package com.p2pchat.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServerApp {
    private static final int PORT = 8080;
    private static ServerSocket serverSocket;
    private static final Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    
    public static void main(String[] args) {
        System.out.println("🚀 P2PChat Server Starting...");
        System.out.println("📍 Server Port: " + PORT);
        
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("✅ Server started successfully on port " + PORT);
            System.out.println("💡 Server is running. Clients can now connect!");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔗 New client connected: " + clientSocket.getInetAddress());
                ClientHandler clientThread = new ClientHandler(clientSocket);
                pool.execute(clientThread);
            }
            
        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }
    
    public static void registerUser(String phoneNumber, String keyFingerprint, ClientHandler handler) {
        onlineUsers.put(phoneNumber, handler);
        System.out.println("👤 User online: " + phoneNumber + " [Key: " + keyFingerprint + "] (Total online: " + onlineUsers.size() + ")");
        
        // Notify all users about updated online list
        broadcastOnlineUsers();
    }
    
    public static void unregisterUser(String phoneNumber) {
        onlineUsers.remove(phoneNumber);
        System.out.println("👤 User offline: " + phoneNumber + " (Total online: " + onlineUsers.size() + ")");
        
        // Notify all users about updated online list
        broadcastOnlineUsers();
    }
    
    public static boolean sendMessageToUser(String fromPhone, String toPhone, String content) {
        ClientHandler recipient = onlineUsers.get(toPhone);
        if (recipient != null) {
            String message = "MESSAGE:" + fromPhone + ":" + content;
            recipient.sendMessage(message);
            System.out.println("📨 " + fromPhone + " → " + toPhone + ": " + content);
            return true;
        } else {
            System.out.println("⚠️  User offline: " + toPhone + " - message queued for delivery");
            // In future: store in database for offline delivery
            return false;
        }
    }
    
    public static List<String> getOnlineUsers() {
        return new ArrayList<>(onlineUsers.keySet());
    }
    
    private static void broadcastOnlineUsers() {
        List<String> onlineList = getOnlineUsers();
        String message = "ONLINE_USERS:" + String.join(",", onlineList);
        
        for (ClientHandler handler : onlineUsers.values()) {
            handler.sendMessage(message);
        }
    }
    
    private static void shutdown() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            pool.shutdown();
            System.out.println("🛑 Server shutdown complete");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}