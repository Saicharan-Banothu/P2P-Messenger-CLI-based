package com.p2pchat.core;

import com.p2pchat.core.models.User;
import com.p2pchat.storage.MySQLStorage;

public class ConversationManager {
    private final MySQLStorage storage;
    private User currentUser;
    
    public ConversationManager(MySQLStorage storage) {
        this.storage = storage;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    // Added methods to use the fields
    public MySQLStorage getStorage() {
        return storage;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isUserSet() {
        return currentUser != null;
    }
}