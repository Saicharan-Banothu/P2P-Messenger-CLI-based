package com.p2pchat.core;

import com.p2pchat.core.models.Message;
import com.p2pchat.core.models.User;
import com.p2pchat.crypto.KeyManager;
import com.p2pchat.net.ConnectionManager;
import com.p2pchat.storage.MySQLStorage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class MessageManager {
    private MySQLStorage storage;
    private KeyManager keyManager;
    private ConnectionManager connectionManager;
    private User currentUser;
    private boolean connectedToServer = false;
    
    // In-memory storage for quick access
    private Map<String, List<Message>> conversationCache = new ConcurrentHashMap<>();
    private List<Message> inboxCache = new CopyOnWriteArrayList<>();
    
    public MessageManager(MySQLStorage storage, KeyManager keyManager, ConnectionManager connectionManager) {
        this.storage = storage;
        this.keyManager = keyManager;
        this.connectionManager = connectionManager;
        
        initializeMessageManager();
    }
    
    private void initializeMessageManager() {
        System.out.println("🌐 Initializing Message Manager...");
        
        // Test database connection
        if (storage != null) {
            System.out.println("✅ Database storage available");
        } else {
            System.out.println("⚠️  Running without database storage");
        }
        
        // Initialize key manager
        if (keyManager != null) {
            keyManager.initialize();
            System.out.println("✅ Key manager initialized");
        }
        
        // Initialize connection to server and start peer server
        if (connectionManager != null) {
            if (connectionManager.isConnected()) {
                connectedToServer = true;
                System.out.println("✅ Connected to chat server");
                
                // Start peer server for P2P connections
                connectionManager.startPeerServer();
                System.out.println("✅ P2P server ready on port " + connectionManager.getPeerPort());
            } else {
                System.err.println("❌ Not connected to chat server");
            }
        }
        
        System.out.println("✅ Message Manager initialized successfully");
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && connectionManager != null) {
            // Register user with server
            connectionManager.registerUser(user.getPhoneNumber());
            System.out.println("📡 Registered user with server: " + user.getPhoneNumber());
        }
    }
    
    public boolean sendMessage(String recipientPhone, String messageText) {
        if (currentUser == null) {
            System.err.println("❌ Please login first before sending messages");
            return false;
        }
        
        if (!isValidPhoneNumber(recipientPhone)) {
            System.err.println("❌ Invalid recipient phone number: " + recipientPhone);
            return false;
        }
        
        if (messageText == null || messageText.trim().isEmpty()) {
            System.err.println("❌ Message cannot be empty");
            return false;
        }
        
        try {
            // Create message object
            Message message = new Message(
                UUID.randomUUID().toString(),
                currentUser.getPhoneNumber(),
                recipientPhone,
                messageText,
                Message.MessageType.TEXT,
                Message.MessageStatus.SENT,
                LocalDateTime.now()
            );
            
            // Save to database if available
            if (storage != null) {
                boolean saved = storage.saveMessage(message);
                if (saved) {
                    System.out.println("✅ Message saved to database");
                } else {
                    System.err.println("❌ Failed to save message to database");
                }
                
                // Update conversation
                updateConversation(currentUser.getPhoneNumber(), recipientPhone, messageText, LocalDateTime.now());
            }
            
            // Send via server connection
            if (connectionManager != null && connectionManager.isConnected()) {
                boolean sent = connectionManager.sendMessage(recipientPhone, messageText);
                if (sent) {
                    System.out.println("✅ Message sent via server");
                    message.setStatus(Message.MessageStatus.DELIVERED);
                } else {
                    System.err.println("❌ Failed to send message via server");
                    message.setStatus(Message.MessageStatus.FAILED);
                }
            } else {
                System.out.println("⚠️  Message queued for delivery (offline mode)");
                message.setStatus(Message.MessageStatus.PENDING);
            }
            
            // Update cache
            updateMessageCache(message);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean sendFileMessage(String recipientPhone, String filePath, String fileName, long fileSize) {
        if (currentUser == null) {
            System.err.println("❌ Please login first before sending files");
            return false;
        }
        
        if (!isValidPhoneNumber(recipientPhone)) {
            System.err.println("❌ Invalid recipient phone number: " + recipientPhone);
            return false;
        }
        
        try {
            // Create file message
            String fileId = UUID.randomUUID().toString();
            String fileMessage = "FILE:" + fileName + ":" + fileSize + ":" + fileId;
            
            // Create message object for file
            Message message = new Message(
                UUID.randomUUID().toString(),
                currentUser.getPhoneNumber(),
                recipientPhone,
                fileMessage,
                Message.MessageType.FILE,
                Message.MessageStatus.SENT,
                LocalDateTime.now()
            );
            
            // Set file properties
            message.setFileName(fileName);
            message.setFileSize(fileSize);
            message.setFilePath(filePath);
            message.setFileId(fileId);
            
            // Save to database if available
            if (storage != null) {
                boolean saved = storage.saveMessage(message);
                if (saved) {
                    System.out.println("✅ File message saved to database");
                } else {
                    System.err.println("❌ Failed to save file message to database");
                }
                
                // Update conversation
                updateConversation(currentUser.getPhoneNumber(), recipientPhone, 
                                 "Sent file: " + fileName, LocalDateTime.now());
            }
            
            // Send via server connection
            if (connectionManager != null && connectionManager.isConnected()) {
                boolean sent = connectionManager.sendFileMessage(recipientPhone, fileName, fileSize, fileId);
                if (sent) {
                    System.out.println("✅ File message sent via server");
                    message.setStatus(Message.MessageStatus.DELIVERED);
                } else {
                    System.err.println("❌ Failed to send file message via server");
                    message.setStatus(Message.MessageStatus.FAILED);
                }
            } else {
                System.out.println("⚠️  File message queued for delivery (offline mode)");
                message.setStatus(Message.MessageStatus.PENDING);
            }
            
            // Update cache
            updateMessageCache(message);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error sending file message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Message> getInbox() {
        // Return cached inbox if available
        if (!inboxCache.isEmpty()) {
            return new ArrayList<>(inboxCache);
        }
        
        // Load from database if storage is available
        if (storage != null && currentUser != null) {
            try {
                List<Message> messages = storage.getMessagesByReceiver(currentUser.getPhoneNumber());
                inboxCache.clear();
                inboxCache.addAll(messages);
                System.out.println("✅ Loaded " + messages.size() + " messages from database");
                return messages;
            } catch (Exception e) {
                System.err.println("❌ Error loading inbox: " + e.getMessage());
            }
        }
        
        return new ArrayList<>();
    }
    
    public List<Message> getConversation(String otherUserPhone) {
        if (currentUser == null) {
            System.err.println("❌ Please login first");
            return new ArrayList<>();
        }
        
        if (!isValidPhoneNumber(otherUserPhone)) {
            System.err.println("❌ Invalid phone number: " + otherUserPhone);
            return new ArrayList<>();
        }
        
        String cacheKey = getConversationCacheKey(currentUser.getPhoneNumber(), otherUserPhone);
        
        // Return cached conversation if available
        if (conversationCache.containsKey(cacheKey)) {
            return new ArrayList<>(conversationCache.get(cacheKey));
        }
        
        // Load from database if storage is available
        if (storage != null) {
            try {
                List<Message> messages = storage.getConversationMessages(currentUser.getPhoneNumber(), otherUserPhone);
                conversationCache.put(cacheKey, new CopyOnWriteArrayList<>(messages));
                System.out.println("✅ Loaded " + messages.size() + " messages for conversation with " + otherUserPhone);
                return messages;
            } catch (Exception e) {
                System.err.println("❌ Error loading conversation: " + e.getMessage());
            }
        }
        
        return new ArrayList<>();
    }
    
    public void requestOnlineUsers() {
        if (connectionManager != null && connectionManager.isConnected()) {
            connectionManager.requestOnlineUsers();
            System.out.println("📡 Requesting online users from server...");
        } else {
            System.err.println("❌ Not connected to server");
        }
    }
    
    public List<String> getOnlineUsers() {
        // This would typically come from server response
        // For now, return empty list - server will push updates via ConnectionManager
        return new ArrayList<>();
    }
    
    public boolean isConnectedToServer() {
        return connectedToServer && connectionManager != null && connectionManager.isConnected();
    }
    
    public void disconnectFromServer() {
        if (connectionManager != null) {
            connectionManager.disconnect();
            connectedToServer = false;
            System.out.println("✅ Disconnected from server");
        }
    }
    
    public void markMessageAsRead(String messageId) {
        if (storage != null) {
            boolean updated = storage.updateMessageStatus(messageId, Message.MessageStatus.READ);
            if (updated) {
                System.out.println("✅ Message marked as read: " + messageId);
                // Update cache
                updateMessageStatusInCache(messageId, Message.MessageStatus.READ);
            }
        }
    }
    
    public void markMessageAsDelivered(String messageId) {
        if (storage != null) {
            boolean updated = storage.updateMessageStatus(messageId, Message.MessageStatus.DELIVERED);
            if (updated) {
                System.out.println("✅ Message marked as delivered: " + messageId);
                // Update cache
                updateMessageStatusInCache(messageId, Message.MessageStatus.DELIVERED);
            }
        }
    }
    
    // Utility methods
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("[6-9][0-9]{9}") && phoneNumber.length() == 10;
    }
    
    private String getConversationCacheKey(String user1, String user2) {
        // Create consistent cache key regardless of order
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
    
    private void updateConversation(String user1, String user2, String lastMessage, LocalDateTime lastMessageTime) {
        if (storage != null) {
            try {
                Long conversationId = storage.findOrCreateConversation(user1, user2);
                storage.updateConversation(conversationId, lastMessage, lastMessageTime);
            } catch (Exception e) {
                System.err.println("❌ Error updating conversation: " + e.getMessage());
            }
        }
    }
    
    private void updateMessageCache(Message message) {
        // Update inbox cache
        if (message.getReceiverPhone().equals(currentUser.getPhoneNumber())) {
            inboxCache.add(message);
        }
        
        // Update conversation cache
        String cacheKey = getConversationCacheKey(message.getSenderPhone(), message.getReceiverPhone());
        List<Message> conversation = conversationCache.getOrDefault(cacheKey, new CopyOnWriteArrayList<>());
        conversation.add(message);
        conversationCache.put(cacheKey, conversation);
    }
    
    private void updateMessageStatusInCache(String messageId, Message.MessageStatus status) {
        // Update in inbox cache
        for (Message message : inboxCache) {
            if (message.getId().equals(messageId)) {
                message.setStatus(status);
                break;
            }
        }
        
        // Update in conversation caches
        for (List<Message> conversation : conversationCache.values()) {
            for (Message message : conversation) {
                if (message.getId().equals(messageId)) {
                    message.setStatus(status);
                    break;
                }
            }
        }
    }
    
    // Method to handle incoming messages from ConnectionManager
    public void handleIncomingMessage(String fromUser, String content) {
        if (currentUser == null) {
            System.err.println("❌ Cannot handle incoming message: no current user");
            return;
        }
        
        try {
            Message.MessageType messageType = content.startsWith("FILE:") ? 
                Message.MessageType.FILE : Message.MessageType.TEXT;
            
            Message message = new Message(
                UUID.randomUUID().toString(),
                fromUser,
                currentUser.getPhoneNumber(),
                content,
                messageType,
                Message.MessageStatus.DELIVERED,
                LocalDateTime.now()
            );
            
            // Handle file messages
            if (messageType == Message.MessageType.FILE) {
                String[] fileParts = content.split(":");
                if (fileParts.length >= 4) {
                    message.setFileName(fileParts[1]);
                    message.setFileSize(Long.parseLong(fileParts[2]));
                    message.setFileId(fileParts[3]);
                }
            }
            
            // Save to database
            if (storage != null) {
                storage.saveMessage(message);
                updateConversation(fromUser, currentUser.getPhoneNumber(), content, LocalDateTime.now());
            }
            
            // Update cache
            updateMessageCache(message);
            
            System.out.println("✅ Received message from " + fromUser + ": " + 
                             (content.length() > 50 ? content.substring(0, 50) + "..." : content));
            
        } catch (Exception e) {
            System.err.println("❌ Error handling incoming message: " + e.getMessage());
        }
    }
    
    // Method to clear cache (useful for testing or memory management)
    public void clearCache() {
        conversationCache.clear();
        inboxCache.clear();
        System.out.println("✅ Message cache cleared");
    }
    
    // Method to get message statistics
    public Map<String, Object> getMessageStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        if (storage != null && currentUser != null) {
            try {
                List<Message> allMessages = storage.getMessagesByReceiver(currentUser.getPhoneNumber());
                
                stats.put("totalMessages", allMessages.size());
                stats.put("unreadMessages", allMessages.stream()
                    .filter(m -> m.getStatus() == Message.MessageStatus.DELIVERED)
                    .count());
                stats.put("sentMessages", allMessages.stream()
                    .filter(m -> m.getSenderPhone().equals(currentUser.getPhoneNumber()))
                    .count());
                stats.put("receivedMessages", allMessages.stream()
                    .filter(m -> m.getReceiverPhone().equals(currentUser.getPhoneNumber()))
                    .count());
                    
            } catch (Exception e) {
                System.err.println("❌ Error getting message statistics: " + e.getMessage());
            }
        }
        
        return stats;
    }
    
    // Method to search messages
    public List<Message> searchMessages(String query) {
        List<Message> results = new ArrayList<>();
        
        if (query == null || query.trim().isEmpty()) {
            return results;
        }
        
        String searchQuery = query.toLowerCase().trim();
        
        // Search in cached messages
        for (Message message : inboxCache) {
            if (message.getContent().toLowerCase().contains(searchQuery) ||
                message.getSenderPhone().contains(searchQuery) ||
                (message.getFileName() != null && message.getFileName().toLowerCase().contains(searchQuery))) {
                results.add(message);
            }
        }
        
        System.out.println("✅ Found " + results.size() + " messages matching: " + query);
        return results;
    }
}