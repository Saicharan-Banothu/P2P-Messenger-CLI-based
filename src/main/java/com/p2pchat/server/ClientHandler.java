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
            if (message.startsWith("IDENTIFY:")) {
                // Match the protocol from ServerApp's inner class
                String[] parts = message.split(":");
                if (parts.length >= 2) {
                    userPhone = parts[1];
                    ServerApp.registerUser(userPhone, this);
                    sendMessage("REGISTERED:" + userPhone);
                    System.out.println("✅ User registered: " + userPhone);
                }
            } else if (message.startsWith("SEND:")) {
                if (userPhone != null) {
                    String[] parts = message.split(":", 3);
                    if (parts.length >= 3) {
                        String toPhone = parts[1];
                        String content = parts[2];
                        boolean delivered = ServerApp.sendMessageToUser(userPhone, toPhone, content);
                        if (delivered) {
                            sendMessage("DELIVERED:" + toPhone);
                        } else {
                            sendMessage("QUEUED:" + toPhone);
                        }
                    }
                }
            } else if (message.startsWith("GET_ONLINE_USERS")) {
                List<String> onlineUsers = ServerApp.getOnlineUsers();
                sendMessage("ONLINE_USERS:" + String.join(",", onlineUsers));
            } else if (message.startsWith("PING")) {
                sendMessage("PONG");
            } else {
                System.out.println("❌ Unknown command from " + userPhone + ": " + message);
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