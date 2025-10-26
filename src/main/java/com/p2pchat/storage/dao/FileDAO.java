package com.p2pchat.storage.dao;

import com.p2pchat.storage.DatabaseConnection;
import java.sql.*;

public class FileDAO {
    
    public boolean saveFileMetadata(String fileId, String fileName, String filePath, 
                                   long fileSize, String fileType, String ownerPhone) {
        String sql = "INSERT INTO file_chunks (file_id, file_name, file_path, file_size, file_type, owner_phone) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, fileId);
            stmt.setString(2, fileName);
            stmt.setString(3, filePath);
            stmt.setLong(4, fileSize);
            stmt.setString(5, fileType);
            stmt.setString(6, ownerPhone);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving file metadata: " + e.getMessage());
            return false;
        }
    }
}