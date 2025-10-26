USE p2pchat;

-- Insert sample users
INSERT INTO users (id, display_name, public_key) VALUES 
('user1', 'Alice Johnson', UNHEX('1234567890ABCDEF')),
('user2', 'Bob Smith', UNHEX('FEDCBA0987654321'));

-- Insert sample contacts
INSERT INTO contacts (owner_id, contact_id, nickname, status) VALUES 
('user1', 'user2', 'Bob', 'ACCEPTED'),
('user2', 'user1', 'Alice', 'ACCEPTED');

-- Insert sample conversation
INSERT INTO conversations (conversation_key) VALUES ('user1_user2');

-- Insert sample messages
INSERT INTO messages (conversation_id, message_uuid, sender_id, recipient_id, payload, status) VALUES 
(1, UUID(), 'user1', 'user2', 'Hello Bob!', 'DELIVERED'),
(1, UUID(), 'user2', 'user1', 'Hi Alice!', 'DELIVERED');