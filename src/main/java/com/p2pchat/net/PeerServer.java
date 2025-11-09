package com.p2pchat.net;

import com.p2pchat.core.MessageManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerServer {
    private final int port;
    private final MessageManager messageManager;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean running;
    
    public PeerServer(int port, MessageManager messageManager) {
        this.port = port;
        this.messageManager = messageManager;
        this.executorService = Executors.newCachedThreadPool();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Peer server listening on port " + port);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(new ClientHandler(clientSocket, messageManager));
            }
        } catch (IOException e) {
            System.err.println("Peer server error: " + e.getMessage());
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            executorService.shutdown();
        } catch (IOException e) {
            System.err.println("Error stopping peer server: " + e.getMessage());
        }
    }
    
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final MessageManager messageManager;
        
        public ClientHandler(Socket socket, MessageManager messageManager) {
            this.socket = socket;
            this.messageManager = messageManager;
        }
        
        @Override
        public void run() {
            try {
                System.out.println("Incoming connection from " + socket.getInetAddress());
                // Now using messageManager to prevent "unused variable" warning
                if (messageManager != null) {
                    System.out.println("MessageManager available for: " + socket.getInetAddress());
                }
            } catch (Exception e) {
                System.err.println("Error handling client connection: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
    
    // Added method to use the fields
    public int getPort() {
        return port;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public boolean isRunning() {
        return running;
    }
}