-- Make sure you're using the p2pchat database
USE p2pchat;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    public_key BLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Contacts table
CREATE TABLE IF NOT EXISTS contacts (
    owner_id VARCHAR(64) NOT NULL,
    contact_id VARCHAR(64) NOT NULL,
    nickname VARCHAR(255),
    status ENUM('PENDING','ACCEPTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(owner_id, contact_id),
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (contact_id) REFERENCES users(id)
);

-- Conversations table
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_key VARCHAR(128) UNIQUE NOT NULL,
    last_message_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT,
    message_uuid VARCHAR(64) UNIQUE NOT NULL,
    sender_id VARCHAR(64) NOT NULL,
    recipient_id VARCHAR(64) NOT NULL,
    payload BLOB NOT NULL,
    content_type VARCHAR(32) DEFAULT 'TEXT',
    status ENUM('QUEUED','SENT','DELIVERED','READ','FAILED') DEFAULT 'QUEUED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP NULL,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (recipient_id) REFERENCES users(id)
);

-- Files table
CREATE TABLE IF NOT EXISTS files (
    file_id VARCHAR(64) PRIMARY KEY,
    message_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    chunk_count INT NOT NULL,
    storage_info JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES messages(id)
);

-- Key store table
CREATE TABLE IF NOT EXISTS key_store (
    owner_id VARCHAR(64) NOT NULL,
    key_type ENUM('private','public') NOT NULL,
    key_blob BLOB NOT NULL,
    is_encrypted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(owner_id, key_type),
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Indexes for performance
CREATE INDEX idx_messages_conversation ON messages(conversation_id, created_at);
CREATE INDEX idx_messages_sender ON messages(sender_id, created_at);
CREATE INDEX idx_messages_recipient ON messages(recipient_id, created_at);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_conversations_key ON conversations(conversation_key);