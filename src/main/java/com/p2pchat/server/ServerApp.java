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
    private static boolean isRunning = false;
    
    public static void main(String[] args) {
        startServer();
    }
    
    public static void startServer() {
        if (isRunning) {
            System.out.println("⚠️  Server is already running");
            return;
        }
        
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("🚀 P2PChat Server Started on port " + PORT);
            System.out.println("💡 Running in background mode");
            
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientThread = new ClientHandler(clientSocket);
                pool.execute(clientThread);
            }
            
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("❌ Server error: " + e.getMessage());
            }
        } finally {
            shutdown();
        }
    }
    
    public static void stopServer() {
        if (!isRunning) {
            System.out.println("⚠️  Server is not running");
            return;
        }
        
        isRunning = false;
        System.out.println("🛑 Stopping server...");
        shutdown();
    }
    
    public static void registerUser(String phoneNumber, ClientHandler handler) {
        onlineUsers.put(phoneNumber, handler);
        System.out.println("👤 User online: " + phoneNumber);
        broadcastOnlineUsers();
    }
    
    public static void unregisterUser(String phoneNumber) {
        onlineUsers.remove(phoneNumber);
        System.out.println("👤 User offline: " + phoneNumber);
        broadcastOnlineUsers();
    }
    
    public static boolean sendMessageToUser(String fromPhone, String toPhone, String content) {
        ClientHandler recipient = onlineUsers.get(toPhone);
        if (recipient != null) {
            String message = "MESSAGE:" + fromPhone + ":" + content;
            recipient.sendMessage(message);
            System.out.println("📨 " + fromPhone + " → " + toPhone);
            return true;
        } else {
            System.out.println("⚠️  User offline: " + toPhone);
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
            System.out.println("✅ Server shutdown complete");
        } catch (IOException e) {
            System.err.println("❌ Error during server shutdown: " + e.getMessage());
        }
    }
    
    public static boolean isRunning() {
        return isRunning;
    }
    
    // REMOVED THE INNER ClientHandler CLASS - USING SEPARATE FILE INSTEAD
}