package com.p2pchat.cli;

import com.p2pchat.core.ConversationManager;
import com.p2pchat.core.models.MessageManager;
import com.p2pchat.crypto.KeyManager;
import com.p2pchat.storage.MySQLStorage;

public class CommandHandler {
    private final ConversationManager conversationManager;
    private final MessageManager messageManager;
    private final KeyManager keyManager;
    private final MySQLStorage storage;
    
    public CommandHandler(ConversationManager conversationManager, 
                         MessageManager messageManager, 
                         KeyManager keyManager, 
                         MySQLStorage storage) {
        this.conversationManager = conversationManager;
        this.messageManager = messageManager;
        this.keyManager = keyManager;
        this.storage = storage;
    }
    
    // Default constructor for tests
    public CommandHandler() {
        this.conversationManager = null;
        this.messageManager = null;
        this.keyManager = null;
        this.storage = null;
    }
    
    public void handleSend(String contactId, String message) {
        if (messageManager != null) {
            messageManager.sendMessage(contactId, message);
        } else {
            System.out.println("Send: " + contactId + " - " + message);
        }
    }
    
    public void handleAddContact(String userId) {
        System.out.println("Adding contact: " + userId);
    }
    
    // Add other command handling methods as needed
}