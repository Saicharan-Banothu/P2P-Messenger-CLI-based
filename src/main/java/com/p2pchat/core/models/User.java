package com.p2pchat.core.models;

import java.time.LocalDateTime;

public class User {
    private String phoneNumber;
    private String displayName;
    private String publicKeyFingerprint;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeen;
    private String profileStatus;
    
    public User(String phoneNumber, String displayName, String publicKeyFingerprint, 
                LocalDateTime createdAt, LocalDateTime lastSeen, String profileStatus) {
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.publicKeyFingerprint = publicKeyFingerprint;
        this.createdAt = createdAt;
        this.lastSeen = lastSeen;
        this.profileStatus = profileStatus;
    }
    
    // Updated constructor with proper defaults
    public User(String phoneNumber, String displayName, String publicKeyFingerprint) {
        this(phoneNumber, displayName, publicKeyFingerprint, 
             LocalDateTime.now(), // Set createdAt to now
             LocalDateTime.now(), // Set lastSeen to now
             "Hey there! I am using P2PChat"); // Default status
    }
    
    // Getters
    public String getPhoneNumber() { return phoneNumber; }
    public String getDisplayName() { return displayName; }
    public String getPublicKeyFingerprint() { return publicKeyFingerprint; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastSeen() { return lastSeen; }
    public String getProfileStatus() { return profileStatus; }
    
    // Setters - ADD THESE MISSING SETTERS
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setPublicKeyFingerprint(String publicKeyFingerprint) { this.publicKeyFingerprint = publicKeyFingerprint; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; } // ADD THIS
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; } // ADD THIS
    public void setProfileStatus(String profileStatus) { this.profileStatus = profileStatus; }
    
    // Validation methods
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && 
               phoneNumber.matches("[6-9][0-9]{9}") && 
               phoneNumber.length() == 10;
    }
    
    public static boolean isValidDisplayName(String displayName) {
        return displayName != null && 
               displayName.matches("[a-zA-Z ]+") && 
               displayName.length() >= 2 && 
               displayName.length() <= 100;
    }
    
    @Override
    public String toString() {
        return String.format("User{phone='%s', name='%s', key='%s'}", 
                phoneNumber, displayName, 
                publicKeyFingerprint != null ? publicKeyFingerprint.substring(0, 8) + "..." : "none");
    }
}