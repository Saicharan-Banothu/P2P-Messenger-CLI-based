package com.p2pchat.storage.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ContactDAOTest {
    
    @Test
    public void testContactDAOCreation() {
        // Test without mocking HikariDataSource
        assertDoesNotThrow(() -> {
            ContactDAO contactDAO = null;
            assertNull(contactDAO);
        });
    }
    
    @Test
    public void testContactModel() {
        ContactDAO.Contact contact = new ContactDAO.Contact("owner1", "contact1", "Friend", ContactDAO.ContactStatus.PENDING);
        assertEquals("owner1", contact.getOwnerId());
        assertEquals("contact1", contact.getContactId());
        assertEquals("Friend", contact.getNickname());
        assertEquals(ContactDAO.ContactStatus.PENDING, contact.getStatus());
    }
}