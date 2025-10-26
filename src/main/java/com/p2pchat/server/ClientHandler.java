package com.p2pchat.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String userPhone;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println("👂 Listening for messages from client...");
            
            String message;
            while ((message = input.readLine()) != null) {
                System.out.println("📩 Received from " + (userPhone != null ? userPhone : "unknown") + ": " + message);
                handleMessage(message);
            }
            
        } catch (IOException e) {
            System.err.println("❌ Client handler error for " + userPhone + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void handleMessage(String message) {
        try {
            String[] parts = message.split(":", 3);
            String command = parts[0];
            
            switch (command) {
                case "REGISTER":
                    if (parts.length >= 3) {
                        userPhone = parts[1];
                        String keyFingerprint = parts[2];
                        ServerApp.registerUser(userPhone, keyFingerprint, this);
                        sendMessage("REGISTERED:" + userPhone);
                        System.out.println("✅ User registered: " + userPhone);
                    }
                    break;
                    
                case "SEND":
                    if (parts.length >= 3 && userPhone != null) {
                        String toPhone = parts[1];
                        String content = parts[2];
                        boolean delivered = ServerApp.sendMessageToUser(userPhone, toPhone, content);
                        if (delivered) {
                            sendMessage("DELIVERED:" + toPhone);
                        } else {
                            sendMessage("QUEUED:" + toPhone);
                        }
                    }
                    break;
                    
                case "ONLINE_USERS":
                    List<String> onlineUsers = ServerApp.getOnlineUsers();
                    sendMessage("ONLINE_USERS:" + String.join(",", onlineUsers));
                    break;
                    
                case "PING":
                    sendMessage("PONG");
                    break;
                    
                default:
                    System.out.println("❌ Unknown command from " + userPhone + ": " + command);
            }
        } catch (Exception e) {
            System.err.println("❌ Error handling message from " + userPhone + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
            System.out.println("📤 Sent to " + userPhone + ": " + message);
        }
    }
    
    private void disconnect() {
        if (userPhone != null) {
            ServerApp.unregisterUser(userPhone);
        }
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            System.out.println("🔌 Client disconnected: " + userPhone);
        } catch (IOException e) {
            System.err.println("❌ Error during client disconnect: " + e.getMessage());
        }
    }
}