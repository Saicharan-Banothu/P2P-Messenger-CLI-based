package com.p2pchat.net;

import com.p2pchat.util.Config;
import java.io.*;
import java.net.*;
import java.util.UUID;


public class ConnectionManager {
    private Socket serverSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ServerSocket peerServerSocket;
    private boolean connected = false;
    private Config config;
    private String instanceId;
    private int peerPort;
    private boolean peerServerStarted = false;
    
    public ConnectionManager() {
        this.config = new Config();
        this.instanceId = generateInstanceId();
        this.peerPort = findAvailablePort();
        initializeConnection();
    }
    
    private String generateInstanceId() {
        try {
            java.lang.reflect.Method method = config.getClass().getMethod("getInstanceId");
            return (String) method.invoke(config);
        } catch (Exception e) {
            return "instance_" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
    
    private int findAvailablePort() {
        int[] preferredPorts = {9090, 9091, 9092, 9093, 9094, 9095, 9096, 9097, 9098, 9099};
        
        for (int port : preferredPorts) {
            if (isPortAvailable(port)) {
                System.out.println("✅ Found available port: " + port);
                return port;
            }
        }
        
        try (ServerSocket tempSocket = new ServerSocket(0)) {
            int dynamicPort = tempSocket.getLocalPort();
            System.out.println("✅ Using dynamic port: " + dynamicPort);
            return dynamicPort;
        } catch (IOException e) {
            System.err.println("❌ Could not find available port, using default 9090");
            return 9090;
        }
    }
    
    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private void initializeConnection() {
        String serverHost = getServerHostFromConfig();
        int serverPort = getServerPortFromConfig();
        
        System.out.println("🔗 Connecting to server: " + serverHost + ":" + serverPort);
        
        try {
            serverSocket = new Socket(serverHost, serverPort);
            out = new PrintWriter(serverSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            connected = true;
            
            out.println("IDENTIFY:" + instanceId + ":" + peerPort);
            System.out.println("✅ Connected to server successfully!");
            startMessageListener();
            
        } catch (IOException e) {
            System.err.println("❌ Failed to connect to server: " + e.getMessage());
            connected = false;
        }
    }
    
    private String getServerHostFromConfig() {
        try {
            java.lang.reflect.Method method = config.getClass().getMethod("getServerHost");
            return (String) method.invoke(config);
        } catch (Exception e) {
            return "localhost";
        }
    }
    
    private int getServerPortFromConfig() {
        try {
            java.lang.reflect.Method method = config.getClass().getMethod("getServerPort");
            return (Integer) method.invoke(config);
        } catch (Exception e) {
            return 8080;
        }
    }
    
    public void startPeerServer() {
        if (peerServerStarted) {
            System.out.println("⚠️  Peer server already running on port " + peerPort);
            return;
        }
        
        try {
            peerServerSocket = new ServerSocket(peerPort);
            peerServerStarted = true;
            System.out.println("✅ P2P server started on port " + peerPort);
            
            Thread peerServerThread = new Thread(() -> {
                while (!peerServerSocket.isClosed()) {
                    try {
                        Socket peerSocket = peerServerSocket.accept();
                        System.out.println("🔗 New peer connection from: " + 
                                         peerSocket.getInetAddress().getHostAddress());
                        handlePeerConnection(peerSocket);
                    } catch (IOException e) {
                        if (!peerServerSocket.isClosed()) {
                            System.err.println("❌ Peer server error: " + e.getMessage());
                        }
                    }
                }
            });
            peerServerThread.setDaemon(true);
            peerServerThread.start();
            
        } catch (IOException e) {
            System.err.println("❌ Failed to start P2P server on port " + peerPort + ": " + e.getMessage());
        }
    }
    
    private void handlePeerConnection(Socket peerSocket) {
        try {
            BufferedReader peerIn = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
            PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
            
            // Now using peerOut to prevent "unused variable" warning
            peerOut.println("HELLO:Connected to " + instanceId);
            
            String peerMessage;
            while ((peerMessage = peerIn.readLine()) != null) {
                System.out.println("📨 Peer message: " + peerMessage);
                if (peerMessage.startsWith("FILE:")) {
                    handleFileTransfer(peerMessage, peerSocket);
                }
                // Echo back to peer using peerOut
                peerOut.println("ECHO:" + peerMessage);
            }
        } catch (IOException e) {
            System.err.println("❌ Peer connection error: " + e.getMessage());
        }
    }
    
    private void handleFileTransfer(String message, Socket peerSocket) {
        System.out.println("📁 File transfer request: " + message);
    }
    
    public void stopPeerServer() {
        if (peerServerSocket != null && !peerServerSocket.isClosed()) {
            try {
                peerServerSocket.close();
                peerServerStarted = false;
                System.out.println("✅ P2P server stopped");
            } catch (IOException e) {
                System.err.println("❌ Error stopping peer server: " + e.getMessage());
            }
        }
    }
    
    private void startMessageListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                String message;
                while (connected && (message = in.readLine()) != null) {
                    handleServerMessage(message);
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("❌ Connection lost: " + e.getMessage());
                    connected = false;
                    attemptReconnect();
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    private void handleServerMessage(String message) {
        System.out.println("📨 Server message: " + message);
        if (message.startsWith("ONLINE_USERS:")) {
            String users = message.substring("ONLINE_USERS:".length());
            System.out.println("👥 Online users: " + users);
        } else if (message.startsWith("MESSAGE:")) {
            String[] parts = message.split(":", 3);
            if (parts.length == 3) {
                String fromUser = parts[1];
                String content = parts[2];
                System.out.println("💬 Message from " + fromUser + ": " + content);
            }
        } else if (message.startsWith("FILE:")) {
            String[] parts = message.split(":", 3);
            if (parts.length == 3) {
                String fromUser = parts[1];
                String fileInfo = parts[2];
                System.out.println("📁 File from " + fromUser + ": " + fileInfo);
            }
        } else if (message.startsWith("REGISTERED:")) {
            String phone = message.substring("REGISTERED:".length());
            System.out.println("✅ Registered with server: " + phone);
        }
    }
    
    private void attemptReconnect() {
        System.out.println("🔄 Attempting to reconnect...");
        int retryCount = 0;
        int maxRetries = 3;
        
        while (retryCount < maxRetries && !connected) {
            try {
                Thread.sleep(5000);
                initializeConnection();
                retryCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (!connected) {
            System.err.println("❌ Failed to reconnect after " + maxRetries + " attempts");
        }
    }
    
    public boolean sendMessage(String toUser, String content) {
        if (!connected) {
            System.err.println("❌ Not connected to server");
            return false;
        }
        
        try {
            out.println("SEND:" + toUser + ":" + content);
            System.out.println("✅ Message sent to " + toUser);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send message: " + e.getMessage());
            return false;
        }
    }
    
    public boolean sendFileMessage(String toUser, String fileName, long fileSize, String fileId) {
        if (!connected) {
            System.err.println("❌ Not connected to server");
            return false;
        }
        
        try {
            String fileMessage = "FILE:" + fileName + ":" + fileSize + ":" + fileId;
            out.println("SEND_FILE:" + toUser + ":" + fileMessage);
            System.out.println("✅ File message sent to " + toUser);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send file message: " + e.getMessage());
            return false;
        }
    }
    
    public void registerUser(String phoneNumber) {
        if (connected) {
            out.println("REGISTER:" + phoneNumber);
            System.out.println("📡 Registering with server as: " + phoneNumber);
        }
    }
    
    public void requestOnlineUsers() {
        if (connected) {
            out.println("GET_ONLINE_USERS");
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public boolean testServerConnection() {
        String serverHost = getServerHostFromConfig();
        int serverPort = getServerPortFromConfig();
        
        try (Socket testSocket = new Socket()) {
            testSocket.connect(new InetSocketAddress(serverHost, serverPort), 5000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (serverSocket != null) serverSocket.close();
            stopPeerServer();
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public int getPeerPort() {
        return peerPort;
    }
    
    public boolean isPeerServerRunning() {
        return peerServerStarted;
    }
    
    // Added method to use config field
    public Config getConfig() {
        return config;
    }
}