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
    private final KeyDAO keyDAO;
    // private final ConnectionDAO connectionDAO; // Comment out for now
    
    public MySQLStorage() {
        this.userDAO = new UserDAO();
        this.messageDAO = new MessageDAO();
        this.contactDAO = new ContactDAO();
        this.conversationDAO = new ConversationDAO();
        this.fileDAO = new FileDAO();
        this.keyDAO = new KeyDAO();
        // this.connectionDAO = new ConnectionDAO(); // Comment out for now
    }
    
    // User methods
    public boolean saveUser(User user) {
        return userDAO.save(user);
    }
    
    public Optional<User> getUser(String phoneNumber) {
        return userDAO.findByPhoneNumber(phoneNumber);
    }
    
    public void updateUserLastSeen(String phoneNumber) {
        userDAO.updateLastSeen(phoneNumber);
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
    public List<FileDAO.FileMetadata> getFilesByOwner(String ownerPhone) {
        return fileDAO.getFilesByOwner(ownerPhone);
    }
    
    public boolean markFileAsDelivered(String fileId) {
        return fileDAO.markFileAsDelivered(fileId);
    }
   
    
    // Key management methods
 // In MySQLStorage.java, update the saveEncryptionKey call to match the DAO
    public boolean saveEncryptionKey(String keyId, String userPhone, byte[] publicKey, 
                                   byte[] privateKey, byte[] symmetricKey) {
        return keyDAO.saveEncryptionKey(keyId, userPhone, publicKey, privateKey, symmetricKey);
    }
    
    public Optional<KeyDAO.EncryptionKey> getEncryptionKey(String userPhone) {
        return keyDAO.findByUserPhone(userPhone);
    }
    
    // Connection methods (commented out for now)
    /*
    public boolean saveP2PConnection(String connectionId, String user1Phone, String user2Phone,
                                    String connectionType, String localAddress, String remoteAddress,
                                    int localPort, int remotePort, byte[] sessionKey) {
        return connectionDAO.saveConnection(connectionId, user1Phone, user2Phone, connectionType,
                                          localAddress, remoteAddress, localPort, remotePort, sessionKey);
    }
    
    public List<ConnectionDAO.P2PConnection> getActiveConnections(String userPhone) {
        return connectionDAO.findActiveConnections(userPhone);
    }
    */
    
    // Admin methods
    public int getUserCount() {
        return userDAO.getUserCount();
    }
    
    public int getMessageCount() {
        return messageDAO.getMessageCount();
    }
    
    public int getFileCount() {
        return fileDAO.getFileCount();
    }
    
    public void close() {
        // Nothing to close since we use fresh connections
    }
}