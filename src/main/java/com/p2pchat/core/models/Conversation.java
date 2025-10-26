package com.p2pchat.core.models;

import java.time.LocalDateTime;

public class Conversation {
    private Long id;
    private String conversationKey;
    private LocalDateTime lastMessageTs;
    
    public Conversation() {}
    
    public Conversation(Long id, String conversationKey, LocalDateTime lastMessageTs) {
        this.id = id;
        this.conversationKey = conversationKey;
        this.lastMessageTs = lastMessageTs;
    }
    
    public Long getId() { return id; }
    public String getConversationKey() { return conversationKey; }
    public LocalDateTime getLastMessageTs() { return lastMessageTs; }
    
    public void setId(Long id) { this.id = id; }
    public void setConversationKey(String conversationKey) { this.conversationKey = conversationKey; }
    public void setLastMessageTs(LocalDateTime lastMessageTs) { this.lastMessageTs = lastMessageTs; }
}