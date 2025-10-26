package com.p2pchat.crypto;

import com.p2pchat.storage.MySQLStorage;
import java.security.*;
import java.util.Base64;
import java.util.UUID;

public class KeyManager {
    private KeyPair keyPair;
    private MySQLStorage storage;
    private String keyId;
    
    public KeyManager() {
        generateKeyPair();
    }
    
    public KeyManager(MySQLStorage storage) {
        this.storage = storage;
        generateKeyPair();
        saveKeysToDatabase();
    }
    
    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.keyPair = keyGen.generateKeyPair();
            this.keyId = UUID.randomUUID().toString();
            System.out.println("✅ RSA Key pair generated: " + keyId);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("❌ RSA algorithm not available: " + e.getMessage());
            // Fallback: create dummy key pair
            createDummyKeyPair();
        }
    }
    
    private void createDummyKeyPair() {
        // Create a simple key representation for fallback
        this.keyId = "dummy_key_" + UUID.randomUUID().toString();
        System.out.println("⚠️  Using dummy key pair: " + keyId);
    }
    
    private void saveKeysToDatabase() {
        if (storage == null || keyPair == null) {
            System.out.println("⚠️  Cannot save keys: storage not available or keys not generated");
            return;
        }
        
        try {
            // Save to encryption_keys table
            byte[] publicKey = keyPair.getPublic().getEncoded();
            byte[] privateKey = keyPair.getPrivate().getEncoded();
            
            // Use CORRECT column name: private_key (not private_key_encrypted)
            boolean saved = storage.saveEncryptionKey(
                keyId,
                "system", // This should be the user's phone number when available
                publicKey,
                privateKey, // This goes to private_key column
                null // symmetric key not used for RSA
            );
            
            if (saved) {
                System.out.println("✅ Encryption keys saved to database");
            } else {
                System.out.println("❌ Failed to save encryption keys");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error saving keys to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method that returns String fingerprint (matches your User class)
    public String getPublicKeyFingerprint() {
        if (keyPair == null) return "no_key";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
            byte[] hash = md.digest(publicKeyBytes);
            // Take first 16 chars of hex representation
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < Math.min(8, hash.length); i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "key_" + System.currentTimeMillis();
        }
    }
    
    public byte[] getPublicKey() {
        if (keyPair == null) return new byte[0];
        try {
            return keyPair.getPublic().getEncoded();
        } catch (Exception e) {
            return new byte[0];
        }
    }
    
    public byte[] getPrivateKey() {
        if (keyPair == null) return new byte[0];
        try {
            return keyPair.getPrivate().getEncoded();
        } catch (Exception e) {
            return new byte[0];
        }
    }
    
    public String getPublicKeyBase64() {
        byte[] publicKey = getPublicKey();
        if (publicKey.length == 0) return "";
        return Base64.getEncoder().encodeToString(publicKey);
    }
    
    public String getPrivateKeyBase64() {
        byte[] privateKey = getPrivateKey();
        if (privateKey.length == 0) return "";
        return Base64.getEncoder().encodeToString(privateKey);
    }
    
    public PublicKey getPublicKeyObject() {
        return keyPair != null ? keyPair.getPublic() : null;
    }
    
    public PrivateKey getPrivateKeyObject() {
        return keyPair != null ? keyPair.getPrivate() : null;
    }
    
    public void initialize() {
        System.out.println("🔑 KeyManager initialized with ID: " + keyId);
    }
    
    public boolean isInitialized() {
        return keyPair != null;
    }
    
    public String getKeyId() {
        return keyId;
    }
    
    // Method to generate a simple key for testing
    public static String generateSimpleKey() {
        return "simple_key_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    // Method to verify key integrity
    public boolean verifyKeyIntegrity() {
        if (keyPair == null) return false;
        try {
            // Simple verification by checking if we can get encoded forms
            byte[] publicKey = keyPair.getPublic().getEncoded();
            byte[] privateKey = keyPair.getPrivate().getEncoded();
            return publicKey.length > 0 && privateKey.length > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Method to get key information for debugging
    public String getKeyInfo() {
        return String.format("KeyID: %s, Type: RSA, Fingerprint: %s, Valid: %s",
                keyId, getPublicKeyFingerprint(), verifyKeyIntegrity());
    }
}