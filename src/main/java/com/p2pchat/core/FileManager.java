package com.p2pchat.core;

import com.p2pchat.core.models.Message;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

public class FileManager {
    private static final String BASE_STORAGE_PATH = "chat_files/";
    private static final int MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB limit
    
    static {
        // Create storage directories when class is loaded
        createStorageDirectories();
    }
    
    public static class FileMeta {
        private final String fileId;
        private final String originalName;
        private final String storedPath;
        private final long fileSize;
        private final String fileType;
        private final Message.MessageType messageType;
        
        public FileMeta(String fileId, String originalName, String storedPath, 
                       long fileSize, String fileType, Message.MessageType messageType) {
            this.fileId = fileId;
            this.originalName = originalName;
            this.storedPath = storedPath;
            this.fileSize = fileSize;
            this.fileType = fileType;
            this.messageType = messageType;
        }
        
        // Getters
        public String getFileId() { return fileId; }
        public String getOriginalName() { return originalName; }
        public String getStoredPath() { return storedPath; }
        public long getFileSize() { return fileSize; }
        public String getFileType() { return fileType; }
        public Message.MessageType getMessageType() { return messageType; }
    }
    
    private static void createStorageDirectories() {
        try {
            Files.createDirectories(Paths.get(BASE_STORAGE_PATH));
            Files.createDirectories(Paths.get(BASE_STORAGE_PATH + "images/"));
            Files.createDirectories(Paths.get(BASE_STORAGE_PATH + "videos/"));
            Files.createDirectories(Paths.get(BASE_STORAGE_PATH + "audio/"));
            Files.createDirectories(Paths.get(BASE_STORAGE_PATH + "documents/"));
            Files.createDirectories(Paths.get(BASE_STORAGE_PATH + "other/"));
            
            System.out.println("✅ File storage directories created");
        } catch (IOException e) {
            System.err.println("❌ Failed to create storage directories: " + e.getMessage());
        }
    }
    
    public static FileMeta saveFile(File file, String senderPhone) throws IOException {
        // Validate file
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }
        
        if (file.length() > MAX_FILE_SIZE) {
            throw new IOException("File too large: " + file.length() + " bytes (max: " + MAX_FILE_SIZE + ")");
        }
        
        String fileId = UUID.randomUUID().toString();
        String originalName = file.getName();
        String fileExtension = getFileExtension(originalName);
        Message.MessageType messageType = getMessageTypeFromExtension(fileExtension);
        String fileType = getFileTypeCategory(fileExtension);
        
        // Create destination path
        String subdirectory = getSubdirectory(fileType);
        String destFileName = fileId + "_" + sanitizeFileName(originalName);
        String destPath = BASE_STORAGE_PATH + subdirectory + destFileName;
        
        System.out.println("💾 Saving file: " + originalName + " -> " + destPath);
        
        // Copy file to storage
        Files.copy(file.toPath(), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("✅ File saved successfully: " + destPath);
        
        return new FileMeta(fileId, originalName, destPath, file.length(), fileType, messageType);
    }
    
    public static File getFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file;
        }
        return null;
    }
    
    public static Message.MessageType getMessageTypeFromExtension(String fileExtension) {
        String extension = fileExtension.toLowerCase();
        
        switch (extension) {
            case "jpg": case "jpeg": case "png": case "gif": case "bmp": case "webp":
                return Message.MessageType.IMAGE;
            case "mp4": case "avi": case "mov": case "wmv": case "mkv": case "webm":
                return Message.MessageType.VIDEO;
            case "mp3": case "wav": case "ogg": case "flac": case "m4a": case "aac":
                return Message.MessageType.AUDIO;
            case "pdf": case "doc": case "docx": case "txt": case "xls": case "xlsx": 
            case "ppt": case "pptx": case "zip": case "rar":
                return Message.MessageType.DOCUMENT;
            default:
                return Message.MessageType.FILE;
        }
    }
    
    public static String getSupportedExtensions() {
        return "Images: jpg, jpeg, png, gif, bmp, webp\n" +
               "Videos: mp4, avi, mov, wmv, mkv, webm\n" +
               "Audio: mp3, wav, ogg, flac, m4a, aac\n" +
               "Documents: pdf, doc, docx, txt, xls, xlsx, ppt, pptx, zip, rar\n" +
               "Others: any file type";
    }
    
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
    
    private static String getFileTypeCategory(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg": case "jpeg": case "png": case "gif": case "bmp": case "webp":
                return "image";
            case "mp4": case "avi": case "mov": case "wmv": case "mkv": case "webm":
                return "video";
            case "mp3": case "wav": case "ogg": case "flac": case "m4a": case "aac":
                return "audio";
            case "pdf": case "doc": case "docx": case "txt": case "xls": case "xlsx": 
            case "ppt": case "pptx": case "zip": case "rar":
                return "document";
            default:
                return "other";
        }
    }
    
    private static String getSubdirectory(String fileType) {
        switch (fileType) {
            case "image": return "images/";
            case "video": return "videos/";
            case "audio": return "audio/";
            case "document": return "documents/";
            default: return "other/";
        }
    }
    
    private static String sanitizeFileName(String fileName) {
        // Remove or replace problematic characters
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
    
    public static boolean deleteFile(String filePath) {
        try {
            boolean deleted = Files.deleteIfExists(Paths.get(filePath));
            if (deleted) {
                System.out.println("🗑️ Deleted file: " + filePath);
            }
            return deleted;
        } catch (IOException e) {
            System.err.println("❌ Error deleting file: " + e.getMessage());
            return false;
        }
    }
    
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    public static void cleanupOldFiles(long olderThanDays) {
        try {
            Path storagePath = Paths.get(BASE_STORAGE_PATH);
            if (!Files.exists(storagePath)) return;
            
            long cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000);
            
            Files.walk(storagePath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        System.out.println("🧹 Cleaned up old file: " + path.getFileName());
                    } catch (IOException e) {
                        System.err.println("Failed to delete old file: " + path);
                    }
                });
                    
        } catch (IOException e) {
            System.err.println("Error during file cleanup: " + e.getMessage());
        }
    }
}