package com.p2pchat.storage.dao;

import com.p2pchat.storage.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {
    
    public enum ContactStatus {
        PENDING, ACCEPTED, BLOCKED
    }
    
    public static class Contact {
        private final String ownerPhone;
        private final String contactPhone;
        private final String nickname;
        private final ContactStatus status;
        
        public Contact(String ownerPhone, String contactPhone, String nickname, ContactStatus status) {
            this.ownerPhone = ownerPhone;
            this.contactPhone = contactPhone;
            this.nickname = nickname;
            this.status = status;
        }
        
        public String getOwnerPhone() { return ownerPhone; }
        public String getContactPhone() { return contactPhone; }
        public String getNickname() { return nickname; }
        public ContactStatus getStatus() { return status; }
    }
    
    public boolean save(String ownerPhone, String contactPhone, String nickname, ContactStatus status) {
        String sql = "INSERT INTO contacts (owner_phone, contact_phone, nickname, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, ownerPhone);
            stmt.setString(2, contactPhone);
            stmt.setString(3, nickname);
            stmt.setString(4, status.name());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving contact: " + e.getMessage());
            return false;
        }
    }
    
    public List<Contact> findByOwnerPhone(String ownerPhone) {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT owner_phone, contact_phone, nickname, status FROM contacts WHERE owner_phone = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, ownerPhone);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String contactPhone = rs.getString("contact_phone");
                String nickname = rs.getString("nickname");
                ContactStatus status = ContactStatus.valueOf(rs.getString("status"));
                
                contacts.add(new Contact(ownerPhone, contactPhone, nickname, status));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching contacts: " + e.getMessage());
        }
        
        return contacts;
    }
}