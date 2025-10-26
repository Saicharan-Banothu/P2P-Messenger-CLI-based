package com.p2pchat.util;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

public class Config {
    private Properties properties;
    
    public Config() {
        properties = new Properties();
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            // Use default configuration
            setDefaultProperties();
            saveConfiguration();
        }
    }
    
    private void setDefaultProperties() {
        // Server configuration
        properties.setProperty("server.host", "localhost");
        properties.setProperty("server.port", "8080");
        
        // Database configuration
        properties.setProperty("database.url", "jdbc:mysql://localhost:3306/p2pchat");
        properties.setProperty("database.username", "root");
        properties.setProperty("database.password", "saiyash1234");
        
        // File storage
        properties.setProperty("storage.basePath", "p2pchat_files/");
        properties.setProperty("storage.chunkSize", "1048576"); // 1MB
        
        // Network settings
        properties.setProperty("network.timeout", "30000");
        properties.setProperty("network.retryCount", "3");
        
        // Multi-computer settings
        properties.setProperty("app.mode", "client");
        properties.setProperty("app.instanceId", UUID.randomUUID().toString()); // Generate unique instance ID
    }
    
    private void saveConfiguration() {
        try (OutputStream output = new FileOutputStream("config.properties")) {
            properties.store(output, "P2PChat Configuration");
        } catch (IOException e) {
            System.err.println("❌ Failed to save configuration: " + e.getMessage());
        }
    }
    
    // Existing methods
    public String getServerHost() {
        return properties.getProperty("server.host");
    }
    
    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port"));
    }
    
    public String getDatabaseUrl() {
        return properties.getProperty("database.url");
    }
    
    public String getDatabaseUsername() {
        return properties.getProperty("database.username");
    }
    
    public String getDatabasePassword() {
        return properties.getProperty("database.password");
    }
    
    // NEW METHODS - Add these
    public String getInstanceId() {
        return properties.getProperty("app.instanceId");
    }
    
    public String getAppMode() {
        return properties.getProperty("app.mode");
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public String getStorageBasePath() {
        return properties.getProperty("storage.basePath");
    }
    
    public int getStorageChunkSize() {
        return Integer.parseInt(properties.getProperty("storage.chunkSize"));
    }
    
    public int getNetworkTimeout() {
        return Integer.parseInt(properties.getProperty("network.timeout"));
    }
    
    public int getNetworkRetryCount() {
        return Integer.parseInt(properties.getProperty("network.retryCount"));
    }
    
    // Update server host for different computers
    public void updateServerHost(String newHost) {
        properties.setProperty("server.host", newHost);
        saveConfiguration();
    }
    
    // Set app mode (client/server)
    public void setAppMode(String mode) {
        properties.setProperty("app.mode", mode);
        saveConfiguration();
    }
    
    // Update instance ID if needed
    public void updateInstanceId(String newInstanceId) {
        properties.setProperty("app.instanceId", newInstanceId);
        saveConfiguration();
    }
}