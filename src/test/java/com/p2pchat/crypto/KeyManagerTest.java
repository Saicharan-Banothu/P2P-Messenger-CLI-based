package com.p2pchat.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KeyManagerTest {
    
    @Test
    public void testKeyGeneration() {
        KeyManager keyManager = new KeyManager();
        
        assertNotNull(keyManager.getPublicKey());
        assertNotNull(keyManager.getPrivateKey());
        assertTrue(keyManager.getPublicKey().length > 0);
        assertTrue(keyManager.getPrivateKey().length > 0);
    }
    
    @Test
    public void testEncryptionDecryption() {
        KeyManager alice = new KeyManager();
        KeyManager bob = new KeyManager();
        
        byte[] alicePublicKey = alice.getPublicKey();
        byte[] bobPublicKey = bob.getPublicKey();
        
        // Derive shared secrets
        byte[] aliceSharedSecret = alice.deriveSharedSecret(bobPublicKey);
        byte[] bobSharedSecret = bob.deriveSharedSecret(alicePublicKey);
        
        // Should be the same
        assertArrayEquals(aliceSharedSecret, bobSharedSecret);
        
        // Test encryption/decryption
        String originalText = "Hello, secure world!";
        byte[] encrypted = alice.encrypt(originalText.getBytes(), aliceSharedSecret);
        byte[] decrypted = bob.decrypt(encrypted, bobSharedSecret);
        
        assertEquals(originalText, new String(decrypted));
    }
    
    @Test
    public void testPublicKeyBase64() {
        KeyManager keyManager = new KeyManager();
        String base64Key = keyManager.getPublicKeyBase64();
        
        assertNotNull(base64Key);
        assertFalse(base64Key.isEmpty());
    }
}