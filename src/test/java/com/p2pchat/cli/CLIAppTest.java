package com.p2pchat.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CLIAppTest {
    
    @Test
    public void testAppStartsWithoutError() {
        assertDoesNotThrow(() -> {
            CLIApp app = new CLIApp();
        });
    }
    
    @Test
    public void testCommandHandlerCreation() {
        // Test with default constructor
        CommandHandler handler = new CommandHandler();
        assertNotNull(handler);
        
        // Test that methods don't throw exceptions
        assertDoesNotThrow(() -> {
            handler.handleSend("test", "message");
            handler.handleAddContact("user123");
        });
    }
}