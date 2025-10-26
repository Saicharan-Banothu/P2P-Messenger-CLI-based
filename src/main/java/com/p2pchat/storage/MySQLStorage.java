package com.p2pchat.storage;

import com.p2pchat.core.models.Message;
import com.p2pchat.core.models.User;
import com.p2pchat.storage.dao.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MySQLStorage {
    private final UserDAO userDAO;
    private final MessageDAO messageDAO;
    private final ContactDAO contactDAO;
    private final ConversationDAO conversationDAO;
    private final FileDAO fileDAO;
    
    public MySQLStorage() {
        this.userDAO = new UserDAO();
        this.messageDAO = new MessageDAO();
        this.contactDAO = new ContactDAO();
        this.conversationDAO = new ConversationDAO();
        this.fileDAO = new FileDAO();
    }
    
    // User methods - SIMPLIFIED (no need to set timestamps here since User constructor handles it)
    public boolean saveUser(User user) {
        // The User constructor already sets proper timestamps and default status
        // So we don't need to set them here anymore
        return userDAO.save(user);
    }
    
    public Optional<User> getUser(String phoneNumber) {
        Optional<User> user = userDAO.findByPhoneNumber(phoneNumber);
        
        // Update last seen when retrieving user (for login/active usage)
        if (user.isPresent()) {
            updateUserLastSeen(phoneNumber);
        }
        
        return user;
    }
    
    public void updateUserLastSeen(String phoneNumber) {
        userDAO.updateLastSeen(phoneNumber);
    }
    
    public boolean updateProfileStatus(String phoneNumber, String status) {
        return userDAO.updateProfileStatus(phoneNumber, status);
    }
    
    // Contact methods
    public boolean addContact(String ownerPhone, String contactPhone, String nickname) {
        return contactDAO.save(ownerPhone, contactPhone, nickname, ContactDAO.ContactStatus.ACCEPTED);
    }
    
    public List<ContactDAO.Contact> getContacts(String ownerPhone) {
        return contactDAO.findByOwnerPhone(ownerPhone);
    }
    
    // Message methods
    public boolean saveMessage(Message message) {
        return messageDAO.save(message);
    }
    
    public List<Message> getMessagesByReceiver(String receiverPhone) {
        return messageDAO.findByReceiverPhone(receiverPhone);
    }
    
    public List<Message> getConversationMessages(String user1Phone, String user2Phone) {
        return messageDAO.findByParticipants(user1Phone, user2Phone);
    }
    
    public boolean updateMessageStatus(String messageId, Message.MessageStatus status) {
        return messageDAO.updateStatus(messageId, status);
    }
    
    // Conversation methods
    public Long findOrCreateConversation(String user1Phone, String user2Phone) {
        return conversationDAO.findOrCreateConversation(user1Phone, user2Phone);
    }
    
    public void updateConversation(Long conversationId, String lastMessage, LocalDateTime lastMessageTime) {
        conversationDAO.updateConversation(conversationId, lastMessage, lastMessageTime);
    }
    
    public List<Object[]> getUserConversations(String userPhone) {
        return conversationDAO.findByUserPhone(userPhone);
    }
    
    // File methods
    public boolean saveFileMetadata(String fileId, String fileName, String filePath, 
                                   long fileSize, String fileType, String ownerPhone) {
        return fileDAO.saveFileMetadata(fileId, fileName, filePath, fileSize, fileType, ownerPhone);
    }
    
    public void close() {
        // Nothing to close since we use fresh connections
    }
}