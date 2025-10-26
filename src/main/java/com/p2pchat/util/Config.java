package com.p2pchat.util;

public class Config {
    public String getDatabaseUrl() { 
        // Make sure this matches your MySQL setup
        return "jdbc:mysql://localhost:3306/p2pchat?useSSL=false&serverTimezone=UTC&autoReconnect=true";
    }
    
    public String getDatabaseUsername() { 
        return "root"; // Change if different
    }
    
    public String getDatabasePassword() { 
        return "saiyash1234"; // Change to your actual password
    }
}