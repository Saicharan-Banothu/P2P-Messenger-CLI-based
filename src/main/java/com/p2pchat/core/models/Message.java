package com.p2pchat.core.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {
    private String id;
    private String senderPhone;
    private String receiverPhone;
    private String content;
    private MessageType messageType;
    private MessageStatus status;
    private LocalDateTime createdAt;
    private String fileName;
    private Long fileSize;
    private String filePath;
    private String fileId;
    private String fileType;
    
    // Updated constructor to match your needs
    public Message(String senderPhone, String receiverPhone, String content, 
                  MessageType messageType, LocalDateTime createdAt) {
        this.id = UUID.randomUUID().toString();
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.content = content;
        this.messageType = messageType;
        this.status = MessageStatus.SENT; // Default status
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }
    
    // Constructor with all fields
    public Message(String id, String senderPhone, String receiverPhone, String content,
                  MessageType messageType, MessageStatus status, LocalDateTime createdAt) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.content = content;
        this.messageType = messageType;
        this.status = status != null ? status : MessageStatus.SENT;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }
    
    // Getters
    public String getId() { return id; }
    public String getSenderPhone() { return senderPhone; }
    public String getReceiverPhone() { return receiverPhone; }
    public String getContent() { return content; }
    public MessageType getMessageType() { return messageType; }
    public MessageStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getFileName() { return fileName; }
    public Long getFileSize() { return fileSize; }
    public String getFilePath() { return filePath; }
    public String getFileId() { return fileId; }
    public String getFileType() { return fileType; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public void setContent(String content) { this.content = content; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public void setStatus(MessageStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    // Helper methods
    public boolean isTextMessage() {
        return messageType == MessageType.TEXT;
    }
    
    public boolean isFileMessage() {
        return messageType == MessageType.FILE || 
               messageType == MessageType.IMAGE ||
               messageType == MessageType.VIDEO ||
               messageType == MessageType.AUDIO ||
               messageType == MessageType.DOCUMENT;
    }
    
    public String getDisplayText() {
        if (isFileMessage() && fileName != null) {
            return "📎 " + fileName + (fileSize != null ? " (" + formatFileSize(fileSize) + ")" : "");
        }
        return content;
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
    
    @Override
    public String toString() {
        return String.format("Message{id='%s', from='%s', to='%s', type='%s', status='%s'}",
                id, senderPhone, receiverPhone, messageType, status);
    }
    
    // Message Type Enum
    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        DOCUMENT,
        FILE
    }
    
    // Message Status Enum - ADDED PENDING status
    public enum MessageStatus {
        PENDING,    // Message is queued for sending
        SENT,       // Message has been sent
        DELIVERED,  // Message has been delivered to recipient
        READ,       // Message has been read by recipient
        FAILED      // Message failed to send
    }
}