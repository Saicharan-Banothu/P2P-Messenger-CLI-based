package com.p2pchat.storage.dao;

import java.sql.*;
import java.util.*;

public class FileDAO {
    
    public boolean saveFileMetadata(String fileId, String fileName, String filePath, 
                                   long fileSize, String fileType, String ownerPhone) {
        // SIMPLIFIED: Only save basic file metadata, no chunk data
        String sql = "INSERT INTO file_chunks (file_id, file_name, file_path, file_size, file_type, owner_phone) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fileId);
            pstmt.setString(2, fileName);
            pstmt.setString(3, filePath);
            pstmt.setLong(4, fileSize);
            pstmt.setString(5, fileType);
            pstmt.setString(6, ownerPhone);
            
            int result = pstmt.executeUpdate();
            if (result > 0) {
                System.out.println("✅ File metadata saved: " + fileName + " (" + fileSize + " bytes)");
                return true;
            } else {
                System.err.println("❌ Failed to save file metadata: " + fileName);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error saving file metadata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public int getFileCount() {
        String sql = "SELECT COUNT(*) FROM file_chunks";
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("❌ Error getting file count: " + e.getMessage());
            return 0;
        }
    }
    
    // Method to get file metadata by ID
    public Optional<FileMetadata> getFileMetadata(String fileId) {
        String sql = "SELECT file_id, file_name, file_path, file_size, file_type, owner_phone, uploaded_at " +
                    "FROM file_chunks WHERE file_id = ?";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fileId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                FileMetadata metadata = new FileMetadata(
                    rs.getString("file_id"),
                    rs.getString("file_name"),
                    rs.getString("file_path"),
                    rs.getLong("file_size"),
                    rs.getString("file_type"),
                    rs.getString("owner_phone"),
                    rs.getTimestamp("uploaded_at").toLocalDateTime()
                );
                return Optional.of(metadata);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting file metadata: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    // Method to get files by owner
    public List<FileMetadata> getFilesByOwner(String ownerPhone) {
        List<FileMetadata> files = new ArrayList<>();
        String sql = "SELECT file_id, file_name, file_path, file_size, file_type, owner_phone, uploaded_at " +
                    "FROM file_chunks WHERE owner_phone = ? ORDER BY uploaded_at DESC";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, ownerPhone);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                FileMetadata metadata = new FileMetadata(
                    rs.getString("file_id"),
                    rs.getString("file_name"),
                    rs.getString("file_path"),
                    rs.getLong("file_size"),
                    rs.getString("file_type"),
                    rs.getString("owner_phone"),
                    rs.getTimestamp("uploaded_at").toLocalDateTime()
                );
                files.add(metadata);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting files by owner: " + e.getMessage());
        }
        
        return files;
    }
    
    // Method to mark file as delivered
    public boolean markFileAsDelivered(String fileId) {
        String sql = "UPDATE file_chunks SET is_delivered = TRUE WHERE file_id = ?";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fileId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error marking file as delivered: " + e.getMessage());
            return false;
        }
    }
    
    // Method to delete file metadata
    public boolean deleteFileMetadata(String fileId) {
        String sql = "DELETE FROM file_chunks WHERE file_id = ?";
        
        try (Connection conn = com.p2pchat.storage.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fileId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting file metadata: " + e.getMessage());
            return false;
        }
    }
    
    public static class FileMetadata {
        private final String fileId;
        private final String fileName;
        private final String filePath;
        private final long fileSize;
        private final String fileType;
        private final String ownerPhone;
        private final java.time.LocalDateTime uploadedAt;
        
        public FileMetadata(String fileId, String fileName, String filePath, 
                           long fileSize, String fileType, String ownerPhone, 
                           java.time.LocalDateTime uploadedAt) {
            this.fileId = fileId;
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.fileType = fileType;
            this.ownerPhone = ownerPhone;
            this.uploadedAt = uploadedAt;
        }
        
        // Getters
        public String getFileId() { return fileId; }
        public String getFileName() { return fileName; }
        public String getFilePath() { return filePath; }
        public long getFileSize() { return fileSize; }
        public String getFileType() { return fileType; }
        public String getOwnerPhone() { return ownerPhone; }
        public java.time.LocalDateTime getUploadedAt() { return uploadedAt; }
        
        @Override
        public String toString() {
            return String.format("File{id='%s', name='%s', size=%d, type='%s'}", 
                               fileId, fileName, fileSize, fileType);
        }
    }
}