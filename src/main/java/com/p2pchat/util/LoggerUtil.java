package com.p2pchat.util;

/**
 * Simplified logger utility for testing
 * Avoids SLF4J dependency issues in test environment
 */
public class LoggerUtil {
    
    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }
    
    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }
    
    public static void error(String message, Throwable throwable) {
        System.err.println("[ERROR] " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
    
    public static void warn(String message) {
        System.out.println("[WARN] " + message);
    }
    
    public static void debug(String message) {
        // Only print debug in development
        if (isDebugEnabled()) {
            System.out.println("[DEBUG] " + message);
        }
    }
    
    private static boolean isDebugEnabled() {
        return "true".equals(System.getProperty("debug.enabled"));
    }
}