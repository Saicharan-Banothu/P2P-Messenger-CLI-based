package com.p2pchat.core;

import com.p2pchat.core.models.Message;
import com.p2pchat.core.models.User;
import com.p2pchat.crypto.KeyManager;
import com.p2pchat.net.ConnectionManager;
import com.p2pchat.net.PeerServer;
import com.p2pchat.storage.MySQLStorage;
import com.p2pchat.util.Serializer;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManager {
    private final MySQLStorage storage;
    private final KeyManager keyManager;
    private final ConnectionManager connectionManager;
    private User currentUser;
    private Socket serverSocket;
    private BufferedReader serverInput;
    private PrintWriter serverOutput;
    private boolean connectedToServer = false;
    private PeerServer peerServer;
    private final String SERVER_HOST = "localhost";
    private final int SERVER_PORT = 8080;
    private final int PEER_PORT = 9090;
    
    // P2P connections cache
    private final ConcurrentHashMap<String, Socket> peerConnections = new ConcurrentHashMap<>();
    
    public MessageManager(MySQLStorage storage, KeyManager keyManager, ConnectionManager connectionManager) {
        this.storage = storage;
        this.keyManager = keyManager;
        this.connectionManager = connectionManager;
        connectToServer();
        startPeerServer();
    }
    
    private void connectToServer() {
        try {
            serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
            serverInput = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            serverOutput = new PrintWriter(serverSocket.getOutputStream(), true);
            connectedToServer = true;
            
            startServerListener();
            System.out.println("✅ Connected to chat server at " + SERVER_HOST + ":" + SERVER_PORT);
            
        } catch (IOException e) {
            System.err.println("❌ Failed to connect to server: " + e.getMessage());
            System.err.println("💡 Make sure the server is running on " + SERVER_HOST + ":" + SERVER_PORT);
            connectedToServer = false;
        }
    }
    
    private void startServerListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                String serverMessage;
                while (connectedToServer && (serverMessage = serverInput.readLine()) != null) {
                    handleServerMessage(serverMessage);
                }
            } catch (IOException e) {
                if (connectedToServer) {
                    System.err.println("❌ Server connection lost: " + e.getMessage());
                    connectedToServer = false;
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    private void startPeerServer() {
        try {
            this.peerServer = new PeerServer(PEER_PORT, this);
            new Thread(() -> peerServer.start()).start();
            System.out.println("✅ P2P server started on port " + PEER_PORT);
        } catch (Exception e) {
            System.err.println("❌ Failed to start P2P server: " + e.getMessage());
        }
    }
    
    private void handleServerMessage(String message) {
        System.out.println("📡 Server: " + message);
        
        String[] parts = message.split(":", 2);
        String command = parts[0];
        
        switch (command) {
            case "MESSAGE":
                if (parts.length >= 2) {
                    String[] messageParts = parts[1].split(":", 2);
                    if (messageParts.length >= 2) {
                        String fromPhone = messageParts[0];
                        String content = messageParts[1];
                        handleIncomingMessage(fromPhone, content);
                    }
                }
                break;
                
            case "REGISTERED":
                System.out.println("✅ Registered with server successfully");
                break;
                
            case "DELIVERED":
                if (parts.length >= 2) {
                    System.out.println("✅ Message delivered to: " + parts[1]);
                }
                break;
                
            case "QUEUED":
                if (parts.length >= 2) {
                    System.out.println("⚠️  User offline, message queued for: " + parts[1]);
                }
                break;
                
            case "ONLINE_USERS":
                if (parts.length >= 2) {
                    String[] users = parts[1].split(",");
                    System.out.println("👥 Online users (" + users.length + "):");
                    for (String user : users) {
                        if (!user.isEmpty() && !user.equals("null")) {
                            System.out.println("   📱 " + user);
                        }
                    }
                }
                break;
                
            case "PONG":
                // Server ping response
                break;
        }
    }
    
    private void handleIncomingMessage(String fromPhone, String content) {
        System.out.println("\n💬 NEW MESSAGE from " + fromPhone + ": " + content);
        
        // Save to local database
        if (storage != null && currentUser != null) {
            try {
                Message message = Message.createTextMessage(fromPhone, currentUser.getPhoneNumber(), content);
                Long conversationId = storage.findOrCreateConversation(fromPhone, currentUser.getPhoneNumber());
                message.setConversationId(conversationId);
                storage.saveMessage(message);
                storage.updateConversation(conversationId, content, LocalDateTime.now());
            } catch (Exception e) {
                System.err.println("❌ Error saving incoming message: " + e.getMessage());
            }
        }
    }
    
    // Enhanced sendMessage with P2P fallback
    public boolean sendMessage(String recipientPhone, String text) {
        if (currentUser == null) {
            System.out.println("❌ Please login first");
            return false;
        }
        
        try {
            // Try P2P first if recipient is online
            if (tryP2PMessage(recipientPhone, text)) {
                System.out.println("✅ Message sent via P2P");
                saveMessageLocally(currentUser.getPhoneNumber(), recipientPhone, text, true);
                return true;
            }
            
            // Fallback to server
            if (!connectedToServer) {
                System.out.println("❌ Not connected to server and P2P failed");
                return false;
            }
            
            serverOutput.println("SEND:" + recipientPhone + ":" + text);
            
            saveMessageLocally(currentUser.getPhoneNumber(), recipientPhone, text, false);
            System.out.println("📤 Message sent via server");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
            return false;
        }
    }
    
    private boolean tryP2PMessage(String recipientPhone, String text) {
        try {
            // In real implementation, you'd use rendezvous server or known peer addresses
            // For now, we'll simulate P2P with local connections
            Socket peerSocket = getPeerConnection(recipientPhone);
            if (peerSocket != null) {
                PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
                
                // Create JSON message
                P2PMessage p2pMessage = new P2PMessage(
                    currentUser.getPhoneNumber(),
                    recipientPhone,
                    text,
                    System.currentTimeMillis(),
                    keyManager.getPublicKeyFingerprint()
                );
                
                String jsonMessage = Serializer.toJson(p2pMessage);
                peerOut.println("P2P_MSG:" + jsonMessage);
                return true;
            }
        } catch (Exception e) {
            System.err.println("❌ P2P message failed: " + e.getMessage());
        }
        return false;
    }
    
    private Socket getPeerConnection(String peerPhone) {
        return peerConnections.computeIfAbsent(peerPhone, phone -> {
            try {
                // Try to connect to peer's P2P server
                Socket socket = new Socket("localhost", PEER_PORT);
                new Thread(new PeerMessageHandler(socket)).start();
                return socket;
            } catch (Exception e) {
                return null;
            }
        });
    }
    
    private void saveMessageLocally(String sender, String receiver, String text, boolean p2p) {
        if (storage != null) {
            try {
                Message message = Message.createTextMessage(sender, receiver, text);
                Long conversationId = storage.findOrCreateConversation(sender, receiver);
                message.setConversationId(conversationId);
                if (p2p) {
                    message.setStatus(Message.MessageStatus.DELIVERED);
                }
                storage.saveMessage(message);
                storage.updateConversation(conversationId, text, LocalDateTime.now());
            } catch (Exception e) {
                System.err.println("❌ Error saving message locally: " + e.getMessage());
            }
        }
    }
    
    public void handleIncomingP2PMessage(P2PMessage p2pMessage) {
        System.out.println("\n💬 P2P MESSAGE from " + p2pMessage.sender + ": " + p2pMessage.content);
        saveMessageLocally(p2pMessage.sender, currentUser.getPhoneNumber(), p2pMessage.content, true);
    }
    
    public void registerWithServer() {
        if (currentUser != null && connectedToServer && serverOutput != null) {
            try {
                serverOutput.println("REGISTER:" + currentUser.getPhoneNumber() + ":" + keyManager.getPublicKeyFingerprint());
                System.out.println("📡 Registering with server as: " + currentUser.getPhoneNumber());
                
                // Wait a bit for server response
                Thread.sleep(500);
            } catch (Exception e) {
                System.err.println("❌ Error registering with server: " + e.getMessage());
            }
        }
    }
    
    public void requestOnlineUsers() {
        if (connectedToServer) {
            serverOutput.println("ONLINE_USERS");
            System.out.println("🔍 Requesting online users list...");
        } else {
            System.out.println("❌ Not connected to server");
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (connectedToServer && user != null) {
            // Small delay to ensure server is ready
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    registerWithServer();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    public List<Message> getInbox() {
        if (currentUser == null) return new ArrayList<>();
        try {
            return storage != null ? storage.getMessagesByReceiver(currentUser.getPhoneNumber()) : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ Error getting inbox: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public boolean isConnectedToServer() {
        return connectedToServer;
    }
    
    public void disconnectFromServer() {
        connectedToServer = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            // Ignore during disconnect
        }
    }
    
    // P2P Message class for JSON serialization
    public static class P2PMessage {
        public String sender;
        public String receiver;
        public String content;
        public long timestamp;
        public String keyFingerprint;
        
        public P2PMessage(String sender, String receiver, String content, long timestamp, String keyFingerprint) {
            this.sender = sender;
            this.receiver = receiver;
            this.content = content;
            this.timestamp = timestamp;
            this.keyFingerprint = keyFingerprint;
        }
        
        // Default constructor for JSON deserialization
        public P2PMessage() {}
    }
    
    private class PeerMessageHandler implements Runnable {
        private final Socket socket;
        private BufferedReader input;
        
        public PeerMessageHandler(Socket socket) {
            this.socket = socket;
        }
        private void saveP2PConnection(String peerPhone, String host, int port) {
            try {
                // Save P2P connection to database
                String sql = "INSERT INTO p2p_connections (user_phone, peer_phone, peer_host, peer_port, is_active) " +
                            "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                            "last_connected = CURRENT_TIMESTAMP, connection_count = connection_count + 1, is_active = TRUE";
                
                // This would require database access
                System.out.println("🔗 Saving P2P connection: " + currentUser.getPhoneNumber() + " → " + peerPhone);
                System.out.println("📍 Peer address: " + host + ":" + port);
                
            } catch (Exception e) {
                System.err.println("❌ Error saving P2P connection: " + e.getMessage());
            }
        }
        
        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = input.readLine()) != null) {
                    if (message.startsWith("P2P_MSG:")) {
                        String json = message.substring(8);
                        P2PMessage p2pMessage = Serializer.fromJson(json, P2PMessage.class);
                        handleIncomingP2PMessage(p2pMessage);
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Peer message handler error: " + e.getMessage());
            }
        }
    }
}