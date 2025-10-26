package com.p2pchat.core.models;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {
    
    @Test
    public void testMessageCreation() {
        Message message = new Message();
        message.setSenderId("user1");
        message.setRecipientId("user2");
        message.setPayload("Hello".getBytes());
        message.setContentType(Message.ContentType.TEXT);
        message.setStatus(Message.Status.QUEUED);
        message.setCreatedAt(LocalDateTime.now());
        
        assertEquals("user1", message.getSenderId());
        assertEquals("user2", message.getRecipientId());
        assertEquals(Message.ContentType.TEXT, message.getContentType());
        assertEquals(Message.Status.QUEUED, message.getStatus());
        assertNotNull(message.getCreatedAt());
    }
    
    @Test
    public void testMessageWithConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Message message = new Message(
            1L, 1L, "msg123", "user1", "user2", 
            "Hello".getBytes(), Message.ContentType.TEXT, 
            Message.Status.SENT, now, null
        );
        
        assertEquals(1L, message.getId());
        assertEquals("msg123", message.getMessageUuid());
        assertEquals("user1", message.getSenderId());
        assertEquals("Hello", new String(message.getPayload()));
    }
}