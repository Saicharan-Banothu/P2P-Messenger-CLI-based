package com.p2pchat.core.models;

import java.time.LocalDateTime;

public class Message {
    public enum MessageType {
        TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT, FILE
    }
    
    public enum MessageStatus {
        SENT, DELIVERED, READ, FAILED
    }
    
    private String id;
    private Long conversationId;
    private String senderPhone;
    private String receiverPhone;
    private MessageType messageType;
    private String content;
    private String fileName;
    private Long fileSize;
    private String filePath;
    private String thumbnailPath;
    private MessageStatus status;
    private LocalDateTime createdAt;
    
    public Message() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.status = MessageStatus.SENT;
        this.messageType = MessageType.TEXT;
    }
    
    // Text message constructor
    public static Message createTextMessage(String senderPhone, String receiverPhone, String text) {
        Message message = new Message();
        message.senderPhone = senderPhone;
        message.receiverPhone = receiverPhone;
        message.messageType = MessageType.TEXT;
        message.content = text;
        return message;
    }
    
    // File message constructor
    public static Message createFileMessage(String senderPhone, String receiverPhone, 
                                          String fileName, long fileSize, String filePath, 
                                          MessageType fileType) {
        Message message = new Message();
        message.senderPhone = senderPhone;
        message.receiverPhone = receiverPhone;
        message.messageType = fileType;
        message.fileName = fileName;
        message.fileSize = fileSize;
        message.filePath = filePath;
        message.content = "Sent a file: " + fileName;
        return message;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    
    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }
    
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }
    
    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Helper methods
    public boolean isTextMessage() {
        return messageType == MessageType.TEXT;
    }
    
    public boolean isMediaMessage() {
        return messageType == MessageType.IMAGE || 
               messageType == MessageType.VIDEO || 
               messageType == MessageType.AUDIO;
    }
    
    public boolean isFileMessage() {
        return messageType == MessageType.DOCUMENT || 
               messageType == MessageType.FILE;
    }
    
    public String getDisplayText() {
        switch (messageType) {
            case TEXT:
                return content;
            case IMAGE:
                return "📷 Photo";
            case VIDEO:
                return "🎥 Video";
            case AUDIO:
                return "🎵 Audio";
            case DOCUMENT:
                return "📄 " + (fileName != null ? fileName : "Document");
            case FILE:
                return "📁 " + (fileName != null ? fileName : "File");
            default:
                return "Unknown message";
        }
    }
    
    @Override
    public String toString() {
        return String.format("Message{from='%s', to='%s', type=%s, content='%s'}", 
                senderPhone, receiverPhone, messageType, getDisplayText());
    }
}