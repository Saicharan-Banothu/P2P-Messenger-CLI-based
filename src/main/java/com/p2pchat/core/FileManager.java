package com.p2pchat.core;

import com.p2pchat.storage.MySQLStorage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.*;

public class FileManager {
    private static final String BASE_STORAGE_PATH = "p2pchat_files/";
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
        "txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "jpg", "jpeg", "png", "gif", "bmp", "svg",
        "mp3", "wav", "ogg",
        "mp4", "avi", "mkv", "mov",
        "zip", "rar", "7z"
    );
    
    private MySQLStorage storage;
    
    public FileManager(MySQLStorage storage) {
        this.storage = storage;
        initializeStorage();
    }
    
    private void initializeStorage() {
        try {
            Path storagePath = Paths.get(BASE_STORAGE_PATH);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                Files.createDirectories(storagePath.resolve("sent"));
                Files.createDirectories(storagePath.resolve("received"));
                Files.createDirectories(storagePath.resolve("temp"));
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to initialize file storage: " + e.getMessage());
        }
    }
    
    public static String getSupportedExtensions() {
        return String.join(", ", SUPPORTED_EXTENSIONS);
    }
    
    public FileMeta saveFile(File file, String ownerPhone) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }
        
        String fileExtension = getFileExtension(file.getName());
        if (!SUPPORTED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: " + fileExtension);
        }
        
        String fileId = UUID.randomUUID().toString();
        String safeFileName = fileId + "_" + sanitizeFileName(file.getName());
        Path targetPath = Paths.get(BASE_STORAGE_PATH, "sent", safeFileName);
        
        // Create directories if they don't exist
        Files.createDirectories(targetPath.getParent());
        
        // Copy file to storage
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        FileMeta meta = new FileMeta(
            fileId,
            file.getName(),
            targetPath.toString(),
            file.length(),
            fileExtension,
            ownerPhone,
            LocalDateTime.now()
        );
        
        // Save file metadata to database (NO CHUNK DATA)
        if (storage != null) {
            try {
                boolean saved = storage.saveFileMetadata(fileId, file.getName(), targetPath.toString(), 
                                   file.length(), fileExtension, ownerPhone);
                if (saved) {
                    System.out.println("✅ File metadata saved to database");
                } else {
                    System.err.println("❌ Failed to save file metadata");
                }
            } catch (Exception e) {
                System.err.println("❌ Error saving file metadata: " + e.getMessage());
                // Continue even if metadata save fails - file is still saved locally
            }
        }
        
        System.out.println("✅ File saved locally: " + file.getName() + " (" + formatFileSize(file.length()) + ")");
        
        return meta;
    }
    
    public static boolean downloadFile(String fileId, String userPhone) {
        try {
            // Look for the file in sent directory
            Path sentDir = Paths.get(BASE_STORAGE_PATH, "sent");
            if (Files.exists(sentDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(sentDir, fileId + "_*")) {
                    for (Path filePath : stream) {
                        String originalName = filePath.getFileName().toString()
                            .substring(fileId.length() + 1); // Remove fileId_ prefix
                        
                        // Create user's download directory
                        Path downloadDir = Paths.get(BASE_STORAGE_PATH, "received", userPhone);
                        Files.createDirectories(downloadDir);
                        
                        Path targetPath = downloadDir.resolve(originalName);
                        Files.copy(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        
                        System.out.println("✅ File downloaded to: " + targetPath);
                        return true;
                    }
                }
            }
            
            System.out.println("❌ File not found with ID: " + fileId);
            return false;
            
        } catch (IOException e) {
            System.err.println("❌ Download failed: " + e.getMessage());
            return false;
        }
    }
    
    // Helper method to get file info by ID
    public static Optional<FileMeta> getFileInfo(String fileId) {
        try {
            Path sentDir = Paths.get(BASE_STORAGE_PATH, "sent");
            if (Files.exists(sentDir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(sentDir, fileId + "_*")) {
                    for (Path filePath : stream) {
                        String originalName = filePath.getFileName().toString()
                            .substring(fileId.length() + 1);
                        
                        // Use static helper method to get file extension
                        String fileExtension = getFileExtensionStatic(originalName);
                        
                        return Optional.of(new FileMeta(
                            fileId,
                            originalName,
                            filePath.toString(),
                            Files.size(filePath),
                            fileExtension,
                            "unknown",
                            LocalDateTime.now()
                        ));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error getting file info: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    // Instance method for non-static context
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
    
    // Static method for static context
    private static String getFileExtensionStatic(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
    
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
    
    public static String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
    
    public static void cleanupOldFiles(int days) {
        try {
            Path storagePath = Paths.get(BASE_STORAGE_PATH);
            if (!Files.exists(storagePath)) return;
            
            LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
            
            cleanupDirectory(storagePath.resolve("temp"), cutoff);
            System.out.println("✅ Cleaned up old temporary files");
            
        } catch (IOException e) {
            System.err.println("❌ Cleanup failed: " + e.getMessage());
        }
    }
    
    private static void cleanupDirectory(Path directory, LocalDateTime cutoff) throws IOException {
        if (!Files.exists(directory)) return;
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path filePath : stream) {
                if (Files.isRegularFile(filePath)) {
                    FileTime fileTime = Files.getLastModifiedTime(filePath);
                    LocalDateTime modifiedTime = LocalDateTime.ofInstant(
                        fileTime.toInstant(), 
                        java.time.ZoneId.systemDefault()
                    );
                    
                    if (modifiedTime.isBefore(cutoff)) {
                        Files.delete(filePath);
                    }
                }
            }
        }
    }
    
    public static class FileMeta {
        private final String fileId;
        private final String originalName;
        private final String filePath;
        private final long fileSize;
        private final String fileType;
        private final String ownerPhone;
        private final LocalDateTime uploadedAt;
        
        public FileMeta(String fileId, String originalName, String filePath, 
                       long fileSize, String fileType, String ownerPhone, 
                       LocalDateTime uploadedAt) {
            this.fileId = fileId;
            this.originalName = originalName;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.fileType = fileType;
            this.ownerPhone = ownerPhone;
            this.uploadedAt = uploadedAt;
        }
        
        // Getters
        public String getFileId() { return fileId; }
        public String getOriginalName() { return originalName; }
        public String getFilePath() { return filePath; }
        public long getFileSize() { return fileSize; }
        public String getFileType() { return fileType; }
        public String getOwnerPhone() { return ownerPhone; }
        public LocalDateTime getUploadedAt() { return uploadedAt; }
    }
}