package com.p2pchat.crypto;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class KeyManager {
    private final KeyPair keyPair;
    private final String keyFingerprint;
    
    public KeyManager() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.keyPair = keyGen.generateKeyPair();
            this.keyFingerprint = generateKeyFingerprint(keyPair.getPublic());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize KeyManager", e);
        }
    }
    
    private String generateKeyFingerprint(PublicKey publicKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(publicKey.getEncoded());
            // Take first 6 bytes and convert to hex string (12 characters)
            StringBuilder fingerprint = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                fingerprint.append(String.format("%02x", hash[i]));
            }
            return fingerprint.toString();
        } catch (Exception e) {
            return "default";
        }
    }
    
    public String getPublicKeyFingerprint() {
        return keyFingerprint;
    }
    
    public byte[] getPublicKeyBytes() {
        return keyPair.getPublic().getEncoded();
    }
    
    public byte[] getPrivateKeyBytes() {
        return keyPair.getPrivate().getEncoded();
    }
    
    // Enhanced encryption methods
    public byte[] encryptMessage(String message, byte[] peerPublicKeyBytes) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(peerPublicKeyBytes);
            PublicKey peerPublicKey = keyFactory.generatePublic(keySpec);
            
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, peerPublicKey);
            return cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            System.err.println("❌ Encryption failed: " + e.getMessage());
            return message.getBytes(); // Fallback to plaintext
        }
    }
    
    public String decryptMessage(byte[] encryptedMessage) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decrypted = cipher.doFinal(encryptedMessage);
            return new String(decrypted);
        } catch (Exception e) {
            System.err.println("❌ Decryption failed: " + e.getMessage());
            return new String(encryptedMessage); // Fallback to plaintext
        }
    }
    
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(getPublicKeyBytes());
    }
    public void saveKeysToDatabase(String userPhone) {
        try {
            // Save to encryption_keys table
            String sql = "INSERT INTO encryption_keys (user_phone, key_fingerprint, key_type, public_key, is_active) VALUES (?, ?, ?, ?, ?)";
            
            // This would require access to MySQLStorage
            System.out.println("💾 Saving encryption keys for user: " + userPhone);
            System.out.println("🔑 Key fingerprint: " + keyFingerprint);
            
            // In full implementation, you'd save the actual public key
            // For now, we'll just log it
        } catch (Exception e) {
            System.err.println("❌ Error saving keys to database: " + e.getMessage());
        }
    }
}