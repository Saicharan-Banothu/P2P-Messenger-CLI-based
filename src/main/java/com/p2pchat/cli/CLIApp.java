package com.p2pchat.cli;

import com.p2pchat.core.ConversationManager;
import com.p2pchat.core.MessageManager;
import com.p2pchat.core.FileManager;
import com.p2pchat.core.models.Message;
import com.p2pchat.core.models.User;
import com.p2pchat.crypto.KeyManager;
import com.p2pchat.net.ConnectionManager;
import com.p2pchat.storage.DatabaseConnection;
import com.p2pchat.storage.MySQLStorage;
import com.p2pchat.util.Config;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class CLIApp {
    private ConversationManager conversationManager;
    private MessageManager messageManager;
    private KeyManager keyManager;
    private ConnectionManager connectionManager;
    private MySQLStorage storage;
    private User currentUser;
    
    public static void main(String[] args) {
        try {
            new CLIApp().start();
        } catch (Exception e) {
            System.err.println("❌ Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void start() {
        System.out.println("=== 📱 P2PChat - WhatsApp-like Messenger ===");
        initializeComponents();
        
        Scanner scanner = null;
        try {
            scanner = new Scanner(System.in);
            runCommandLoop(scanner);
        } catch (Exception e) {
            System.err.println("❌ Error in command loop: " + e.getMessage());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            cleanup();
        }
    }
    
    private void initializeComponents() {
        try {
            Config config = new Config();
            
            // Initialize components first
            keyManager = new KeyManager();
            connectionManager = new ConnectionManager();
            
            // Initialize database
            try {
                System.out.println("🔗 Initializing database...");
                DatabaseConnection.initialize(
                    config.getDatabaseUrl(),
                    config.getDatabaseUsername(),
                    config.getDatabasePassword()
                );
                
                storage = new MySQLStorage();
                System.out.println("✅ Database initialized successfully");
                
                // Test database connection
                if (DatabaseConnection.isConnected()) {
                    System.out.println("✅ Database connection test successful");
                } else {
                    System.out.println("⚠️  Database connection test failed");
                }
                
            } catch (Exception e) {
                System.out.println("⚠️  Running in offline mode without database: " + e.getMessage());
                storage = null;
            }
            
            // Initialize managers
            conversationManager = new ConversationManager(storage);
            messageManager = new MessageManager(storage, keyManager, connectionManager);
            
            System.out.println("✅ All components initialized successfully");
            
        } catch (Exception e) {
            System.out.println("❌ Initialization error: " + e.getMessage());
            initializeFallbackComponents();
        }
    }
    
    private void initializeFallbackComponents() {
        try {
            keyManager = new KeyManager();
            connectionManager = new ConnectionManager();
            storage = null;
            conversationManager = new ConversationManager(null);
            messageManager = new MessageManager(null, keyManager, connectionManager);
            System.out.println("✅ Fallback components initialized (offline mode)");
        } catch (Exception e) {
            System.err.println("❌ Critical: Could not initialize fallback components: " + e.getMessage());
        }
    }
    
    private void runCommandLoop(Scanner scanner) {
        System.out.println("\nType 'help' for available commands");
        
        while (true) {
            try {
                System.out.print("\np2pchat> ");
                
                if (!scanner.hasNextLine()) {
                    System.out.println("\n👋 No input available. Exiting...");
                    break;
                }
                
                String input = scanner.nextLine().trim();
                
                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                    break;
                }
                
                if (input.isEmpty()) {
                    continue;
                }
                
                handleCommand(input, scanner);
                
            } catch (Exception e) {
                System.err.println("❌ Error in command loop: " + e.getMessage());
                if (e instanceof java.util.NoSuchElementException) {
                    System.out.println("📱 Input stream closed. Exiting...");
                    break;
                }
            }
        }
    }
    
    private void handleCommand(String input, Scanner scanner) {
        String[] parts = input.split("\\s+", 3);
        String command = parts[0].toLowerCase();
        
        try {
            switch (command) {
            	case "clear-db":
                System.out.print("⚠️  ARE YOU SURE? This will delete ALL data! Type 'YES' to confirm: ");
                String confirmation = scanner.nextLine().trim();
                if ("YES".equals(confirmation)) {
                    DatabaseConnection.clearDatabase();
                } else {
                    System.out.println("❌ Database clear cancelled");
                }
                break;
                case "register":
                    handleRegister(scanner);
                    break;
                    
                case "login":
                    if (parts.length >= 2) {
                        login(parts[1]);
                    } else {
                        System.out.println("❌ Usage: login <phoneNumber>");
                    }
                    break;
                    
                case "send":
                    if (parts.length >= 3) {
                        handleSend(parts[1], parts[2]);
                    } else {
                        System.out.println("❌ Usage: send <phoneNumber> <message>");
                    }
                    break;
                    
                case "sendfile":
                    if (parts.length >= 2) {
                        handleSendFile(parts[1], scanner);
                    } else {
                        System.out.println("❌ Usage: sendfile <phoneNumber>");
                    }
                    break;
                    
                case "add-contact":
                    if (parts.length >= 2) {
                        addContact(parts[1], scanner);
                    } else {
                        System.out.println("❌ Usage: add-contact <phoneNumber>");
                    }
                    break;
                    
                case "inbox":
                    showInbox();
                    break;
                    
                case "chats":
                    showConversations();
                    break;
                    
                case "chat":
                    if (parts.length >= 2) {
                        showConversation(parts[1]);
                    } else {
                        System.out.println("❌ Usage: chat <phoneNumber>");
                    }
                    break;
                    
                case "contacts":
                    showContacts();
                    break;
                    
                case "profile":
                    showProfile();
                    break;
                    
                case "online":
                    if (messageManager != null) {
                        messageManager.requestOnlineUsers();
                    }
                    break;
                    
                case "server-status":
                    showServerStatus();
                    break;
                    
                case "status":
                    showStatus();
                    break;
                    
                case "db-stats":
                    DatabaseConnection.printDatabaseStats();
                    break;
                    
                case "help":
                    showHelp();
                    break;
                    
                default:
                    System.out.println("❌ Unknown command: " + command);
                    showHelp();
            }
        } catch (Exception e) {
            System.err.println("❌ Error executing command '" + command + "': " + e.getMessage());
        }
    }
    
    private void handleRegister(Scanner scanner) {
        try {
            System.out.print("📝 Enter your display name: ");
            String displayName = scanner.nextLine().trim();
            
            if (!User.isValidDisplayName(displayName)) {
                System.out.println("❌ Invalid display name. Use only letters and spaces (2-100 characters)");
                return;
            }
            
            System.out.print("📱 Enter your phone number (10 digits, starting with 6-9): ");
            String phoneNumber = scanner.nextLine().trim();
            
            if (!User.isValidPhoneNumber(phoneNumber)) {
                System.out.println("❌ Invalid phone number. Must be 10 digits starting with 6,7,8,9");
                return;
            }
            
            // Check if phone number already exists
            if (storage != null && storage.getUser(phoneNumber).isPresent()) {
                System.out.println("❌ Phone number already registered");
                return;
            }
            
            // FIXED: Use key fingerprint instead of byte array
            String publicKeyFingerprint = keyManager != null ? keyManager.getPublicKeyFingerprint() : "none";
            User user = new User(phoneNumber, displayName, publicKeyFingerprint);
            
            boolean saved = false;
            if (storage != null) {
                saved = storage.saveUser(user);
            } else {
                saved = true;
                System.out.println("📴 Offline mode - user not saved to database");
            }
            
            if (saved) {
                System.out.println("✅ Registered successfully!");
                System.out.println("   📱 Phone: " + phoneNumber);
                System.out.println("   👤 Name: " + displayName);
                System.out.println("   🔑 Public key fingerprint: " + publicKeyFingerprint);
                
                // Auto-login after registration
                currentUser = user;
                if (conversationManager != null) {
                    conversationManager.setCurrentUser(user);
                }
                if (messageManager != null) {
                    messageManager.setCurrentUser(user);
                }
                
                System.out.println("🎉 Welcome to P2PChat! You're automatically logged in.");
            } else {
                System.out.println("❌ Registration failed");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Registration error: " + e.getMessage());
        }
    }
    
    private void login(String phoneNumber) {
        try {
            if (!User.isValidPhoneNumber(phoneNumber)) {
                System.out.println("❌ Invalid phone number format");
                return;
            }
            
            User user = null;
            
            if (storage != null) {
                var userOpt = storage.getUser(phoneNumber);
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                } else {
                    System.out.println("❌ User not found. Please register first.");
                    return;
                }
            } else {
                // FIXED: Use string for key fingerprint
                user = new User(phoneNumber, "Offline User", "none");
                System.out.println("📴 Offline mode - using temporary user");
            }
            
            if (user != null) {
                currentUser = user;
                if (conversationManager != null) {
                    conversationManager.setCurrentUser(currentUser);
                }
                if (messageManager != null) {
                    messageManager.setCurrentUser(currentUser);
                }
                
                // Update last seen
                if (storage != null) {
                    storage.updateUserLastSeen(phoneNumber);
                }
                
                System.out.println("✅ Logged in as: " + currentUser.getDisplayName());
                System.out.println("   📱 Phone: " + currentUser.getPhoneNumber());
                System.out.println("   📝 Status: " + currentUser.getProfileStatus());
            }
        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
        }
    }
    
    private void handleSend(String recipientPhone, String messageText) {
        if (currentUser == null) {
            System.out.println("❌ Please login first");
            return;
        }
        
        if (!User.isValidPhoneNumber(recipientPhone)) {
            System.out.println("❌ Invalid phone number format");
            return;
        }
        
        if (messageManager != null) {
            messageManager.sendMessage(recipientPhone, messageText);
        } else {
            System.out.println("❌ Message manager not available");
        }
    }
    
    private void handleSendFile(String recipientPhone, Scanner scanner) {
        if (currentUser == null) {
            System.out.println("❌ Please login first");
            return;
        }
        
        if (!User.isValidPhoneNumber(recipientPhone)) {
            System.out.println("❌ Invalid phone number format");
            return;
        }
        
        System.out.println("📁 Supported file types:");
        System.out.println(FileManager.getSupportedExtensions());
        System.out.print("📁 Enter file path: ");
        String filePath = scanner.nextLine().trim();
        
        if (filePath.isEmpty()) {
            System.out.println("❌ File path cannot be empty");
            return;
        }
        
        // For now, we'll simulate file sending since we need to update MessageManager
        System.out.println("⚠️  File sharing via server is being implemented...");
        System.out.println("💡 For now, files are saved locally only");
        
        try {
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                FileManager.FileMeta fileMeta = FileManager.saveFile(file, currentUser.getPhoneNumber());
                System.out.println("✅ File saved locally: " + fileMeta.getOriginalName());
                
                // Send a message about the file
                String fileMessage = "Sent file: " + fileMeta.getOriginalName() + " (" + 
                                   FileManager.formatFileSize(fileMeta.getFileSize()) + ")";
                messageManager.sendMessage(recipientPhone, fileMessage);
            } else {
                System.out.println("❌ File not found: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("❌ Error handling file: " + e.getMessage());
        }
    }
    
    private void addContact(String phoneNumber, Scanner scanner) {
        if (currentUser == null) {
            System.out.println("❌ Please login first");
            return;
        }
        
        if (!User.isValidPhoneNumber(phoneNumber)) {
            System.out.println("❌ Invalid phone number format");
            return;
        }
        
        // Check if user exists
        if (storage != null) {
            var userOpt = storage.getUser(phoneNumber);
            if (!userOpt.isPresent()) {
                System.out.println("❌ User with phone number " + phoneNumber + " not found");
                return;
            }
        }
        
        System.out.print("👤 Enter nickname (optional): ");
        String nickname = scanner.nextLine().trim();
        
        if (storage != null) {
            if (storage.addContact(currentUser.getPhoneNumber(), phoneNumber, nickname)) {
                System.out.println("✅ Contact added: " + phoneNumber + 
                                 (nickname.isEmpty() ? "" : " (" + nickname + ")"));
            } else {
                System.out.println("❌ Failed to add contact");
            }
        } else {
            System.out.println("📴 Offline mode - contact not saved: " + phoneNumber);
        }
    }
    
    private void showInbox() {
        if (currentUser == null) {
            System.out.println("❌ Please login first");
            return;
        }
        
        if (messageManager == null) {
            System.out.println("❌ Message manager not available");
            return;
        }
        
        List<Message> messages = messageManager.getInbox();
        if (messages.isEmpty()) {
            System.out.println("📭 No new messages");
            return;
        }
        
        System.out.println("\n📨 INBOX (" + messages.size() + " messages)");
        System.out.println("═".repeat(80));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
        
        for (Message msg : messages) {
            String time = msg.getCreatedAt().format(formatter);
            String senderInfo = msg.getSenderPhone().equals(currentUser.getPhoneNumber()) ? 
                               "You" : msg.getSenderPhone();
            
            System.out.printf("🕒 %s | 👤 %s\n", time, senderInfo);
            System.out.printf("   💬 %s\n", msg.getDisplayText());
            System.out.println("   ──────────────────────────────────────────────────────────────────");
        }
    }
    
    private void showConversations() {
        if (currentUser == null) {
            System.out.println("❌ Please login first");
            return;
        }
        
        if (storage != null) {
            try {
                List<Object[]> conversations = storage.getUserConversations(currentUser.getPhoneNumber());
                if (conversations.isEmpty()) {
                    System.out.println("💬 No conversations yet");
                    return;
                }
                
                System.out.println("\n💬 YOUR CHATS");
                System.out.println("═".repeat(80));
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
                
                for (Object[] conv : conversations) {
                    String otherUser = (String) conv[0];
                    String lastMessage = (String) conv[1];
                    LocalDateTime lastTime = (LocalDateTime) conv[2];
                    int unread = (Integer) conv[3];
                    
                    String time = lastTime.format(formatter);
                    String unreadStr = unread > 0 ? " (" + unread + ")" : "";
                    
                    System.out.printf("👤 %s%s\n", otherUser, unreadStr);
                    System.out.printf("   💬 %s\n", lastMessage != null ? lastMessage : "No messages");
                    System.out.printf("   🕒 %s\n", time);
                    System.out.println("   ──────────────────────────────────────────────────────────────────");
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading conversations: " + e.getMessage());
            }
        } else {
            System.out.println("📴 Offline mode - conversations not available");
        }
    }
    
    private void showConversation(String otherUserPhone) {
        if (currentUser == null) {
            System.out.println("❌ Please login first");
            return;
        }
        
        if (!User.isValidPhoneNumber(otherUserPhone)) {
            System.out.println("❌ Invalid phone number format");
            return;
        }
        
        if (storage != null) {
            try {
                List<Message> messages = storage.getConversationMessages(currentUser.getPhoneNumber(), otherUserPhone);
                if (messages.isEmpty()) {
                    System.out.println("💬 No messages with " + otherUserPhone);
                    return;
                }
                
                System.out.println("\n💬 CHAT WITH " + otherUserPhone);
                System.out.println("═".repeat(80));
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
                
                for (Message msg : messages) {
                    String time = msg.getCreatedAt().format(formatter);
                    boolean isSent = msg.getSenderPhone().equals(currentUser.getPhoneNumber());
                    String sender = isSent ? "You" : otherUserPhone;
                    String align = isSent ? "→" : "←";
                    
                    System.out.printf("%s 🕒 %s | 👤 %s\n", align, time, sender);
                    System.out.printf("   %s %s\n", getMessageEmoji(msg), msg.getDisplayText());
                    
                    if (msg.isFileMessage() && msg.getFileSize() != null) {
                        System.out.printf("   📊 %s\n", FileManager.formatFileSize(msg.getFileSize()));
                    }
                    
                    System.out.println();
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading conversation: " + e.getMessage());
            }
        } else {
            System.out.println("📴 Offline mode - conversation not available");
        }
    }
    
    private void showContacts() {
        if (currentUser == null) {
            System.out.println("❌ Please login first");
            return;
        }
        
        if (storage != null) {
            try {
                var contacts = storage.getContacts(currentUser.getPhoneNumber());
                if (contacts.isEmpty()) {
                    System.out.println("👥 No contacts yet");
                    return;
                }
                
                System.out.println("\n👥 YOUR CONTACTS");
                System.out.println("═".repeat(80));
                
                for (var contact : contacts) {
                    String phone = contact.getContactPhone();
                    String nickname = contact.getNickname();
                    String status = contact.getStatus().name();
                    
                    // Try to get user info
                    var userOpt = storage.getUser(phone);
                    String name = userOpt.map(User::getDisplayName).orElse("Unknown User");
                    
                    System.out.printf("📱 %s\n", phone);
                    System.out.printf("   👤 %s%s\n", name, 
                                     nickname != null && !nickname.isEmpty() ? " (" + nickname + ")" : "");
                    System.out.printf("   📍 Status: %s\n", status);
                    System.out.println("   ──────────────────────────────────────────────────────────────────");
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading contacts: " + e.getMessage());
            }
        } else {
            System.out.println("📴 Offline mode - contacts not available");
        }
    }
    
    private void showProfile() {
        if (currentUser == null) {
            System.out.println("❌ Please login first");
            return;
        }
        
        System.out.println("\n👤 YOUR PROFILE");
        System.out.println("═".repeat(80));
        System.out.println("📱 Phone: " + currentUser.getPhoneNumber());
        System.out.println("👤 Name: " + currentUser.getDisplayName());
        System.out.println("📝 Status: " + currentUser.getProfileStatus());
        System.out.println("🔑 Key fingerprint: " + currentUser.getPublicKeyFingerprint());
        System.out.println("🕒 Joined: " + currentUser.getCreatedAt().format(
            DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        System.out.println("👀 Last seen: " + currentUser.getLastSeen().format(
            DateTimeFormatter.ofPattern("MMM dd, HH:mm")));
    }
    
    private void showServerStatus() {
        System.out.println("\n🌐 SERVER STATUS");
        System.out.println("═".repeat(80));
        if (messageManager != null) {
            System.out.println("🌐 Server Connection: " + (messageManager.isConnectedToServer() ? "✅ Connected" : "❌ Disconnected"));
        } else {
            System.out.println("🌐 Server Connection: ❌ Message manager not available");
        }
        
        if (connectionManager != null) {
            boolean serverReachable = connectionManager.testServerConnection();
            System.out.println("📡 Server Reachable: " + (serverReachable ? "✅ Yes" : "❌ No"));
        }
    }
    
    private void showStatus() {
        System.out.println("\n📊 APPLICATION STATUS");
        System.out.println("═".repeat(80));
        System.out.println("💾 Database: " + (storage != null ? "✅ Connected" : "📴 Offline"));
        System.out.println("🌐 Server: " + (messageManager != null && messageManager.isConnectedToServer() ? "✅ Connected" : "❌ Disconnected"));
        System.out.println("👤 Current User: " + (currentUser != null ? 
            currentUser.getDisplayName() + " (" + currentUser.getPhoneNumber() + ")" : "❌ Not logged in"));
        System.out.println("🔑 Key Manager: " + (keyManager != null ? "✅ Ready" : "❌ Not available"));
        System.out.println("📡 Connection Manager: " + (connectionManager != null ? "✅ Ready" : "❌ Not available"));
        System.out.println("💬 Message Manager: " + (messageManager != null ? "✅ Ready" : "❌ Not available"));
        System.out.println("💼 Conversation Manager: " + (conversationManager != null ? "✅ Ready" : "❌ Not available"));
    }
    
    private String getMessageEmoji(Message msg) {
        if (msg.isTextMessage()) return "💬";
        switch (msg.getMessageType()) {
            case IMAGE: return "📷";
            case VIDEO: return "🎥";
            case AUDIO: return "🎵";
            case DOCUMENT: return "📄";
            case FILE: return "📁";
            default: return "💬";
        }
    }
    
    private void showHelp() {
        System.out.println("\n📱 P2PCHAT COMMANDS");
        System.out.println("═".repeat(80));
        System.out.println("👤 User Commands:");
        System.out.println("  register              - Create new account with phone number");
        System.out.println("  login <phone>         - Login with your phone number");
        System.out.println("  profile               - Show your profile information");
        System.out.println("  status                - Show application status");
        
        System.out.println("\n💬 Messaging Commands:");
        System.out.println("  send <phone> <msg>    - Send text message to a user");
        System.out.println("  sendfile <phone>      - Send file to a user");
        System.out.println("  inbox                 - Show all received messages");
        System.out.println("  chats                 - Show your conversations");
        System.out.println("  chat <phone>          - Show conversation with specific user");
        
        System.out.println("\n👥 Contact Commands:");
        System.out.println("  add-contact <phone>   - Add user to your contacts");
        System.out.println("  contacts              - Show your contact list");
        
        System.out.println("\n🌐 Server Commands:");
        System.out.println("  online                - Show online users");
        System.out.println("  server-status         - Check server connection");
        
        System.out.println("\n🗄️  Database Commands:");
        System.out.println("  db-stats              - Show database statistics and usage");
        System.out.println("  clear-db              - CLEAR ALL DATABASE DATA (use with caution!)");
        
        System.out.println("\n⚙️  System Commands:");
        System.out.println("  help                  - Show this help message");
        System.out.println("  exit                  - Exit the application");
        
        System.out.println("\n📁 Supported File Types:");
        System.out.println(FileManager.getSupportedExtensions());
    }
    
    private void cleanup() {
        try {
            if (messageManager != null) {
                messageManager.disconnectFromServer();
            }
            if (storage != null) {
                storage.close();
            }
            DatabaseConnection.close();
            
            // Clean up old files (older than 30 days)
            FileManager.cleanupOldFiles(30);
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        System.out.println("👋 Goodbye! Thanks for using P2PChat!");
    }
}