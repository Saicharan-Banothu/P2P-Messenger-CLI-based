package com.p2pchat.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SerializerTest {
    
    @Test
    public void testJsonSerialization() {
        TestObject obj = new TestObject("test", 123);
        
        String json = Serializer.toJson(obj);
        assertNotNull(json);
        assertTrue(json.contains("test"));
        assertTrue(json.contains("123"));
        
        TestObject deserialized = Serializer.fromJson(json, TestObject.class);
        assertEquals(obj.name, deserialized.name);
        assertEquals(obj.value, deserialized.value);
    }
    
    @Test
    public void testJsonBytesSerialization() {
        TestObject obj = new TestObject("test", 123);
        
        byte[] jsonBytes = Serializer.toJsonBytes(obj);
        assertNotNull(jsonBytes);
        assertTrue(jsonBytes.length > 0);
    }
    
    // Helper class for testing
    public static class TestObject {
        public String name;
        public int value;
        
        public TestObject() {} // Default constructor for Jackson
        
        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}