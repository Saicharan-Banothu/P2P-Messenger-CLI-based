package com.p2pchat.cli;

public class CommandHandler {
    public CommandHandler() {
    }
    
    public void handleSend(String contactId, String message) {
        System.out.println("Send: " + contactId + " - " + message);
    }
}