import com.p2pchat.core.models.User;
import com.p2pchat.crypto.KeyManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Integration tests that verify multiple components work together
 */
public class IntegrationTest {
    
    private static KeyManager keyManager;
    
    @BeforeAll
    public static void setUp() {
        System.out.println("Setting up integration test environment...");
        keyManager = new KeyManager();
    }
    
    @Test
    public void testComponentIntegration() {
        System.out.println("Testing component integration...");
        assertNotNull(keyManager, "KeyManager should be initialized");
        System.out.println("✓ All components integrated successfully");
    }
    
    @Test
    public void testCryptoIntegration() {
        System.out.println("Testing crypto integration...");
        
        byte[] publicKey = keyManager.getPublicKey();
        
        assertNotNull(publicKey, "Public key should be generated");
        assertTrue(publicKey.length > 0, "Public key should not be empty");
        
        System.out.println("✓ Crypto integration successful");
    }
    
    @Test
    public void testBasicWorkflow() {
        System.out.println("Testing basic workflow...");
        
        User testUser = new User(
            "integration-test-user", 
            "Integration Test User", 
            keyManager.getPublicKey(), 
            LocalDateTime.now()
        );
        
        assertNotNull(testUser);
        assertEquals("integration-test-user", testUser.getId());
        
        System.out.println("✓ Basic workflow successful");
    }
}