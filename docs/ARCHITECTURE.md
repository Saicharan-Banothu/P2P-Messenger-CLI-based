# Architecture — P2P Messenger (CLI)

This document describes the high-level architecture, package responsibilities, data flow and design decisions for the P2P Messenger CLI application.

Overview
- Language: Java SE
- Runtime: single-process CLI app that can operate in offline mode or connected mode with a MySQL backend and optional server component
- Primary packages: com.p2pchat.cli, com.p2pchat.core, com.p2pchat.net, com.p2pchat.crypto, com.p2pchat.storage, com.p2pchat.util

Package responsibilities
- com.p2pchat.cli
  - CLIApp.java — application bootstrap, lifecycle, CLI loop, mode selection (user/admin)
  - CommandHandler.java — parsing input and delegating to core managers

- com.p2pchat.core
  - MessageManager — sending/receiving messages, formatting, delivery and server integration
  - FileManager — saving files, chunking, downloads, storage paths
  - ConversationManager — in-memory and persisted conversation state, conversation queries
  - models (Message, User, FileMeta, Contact, Conversation)

- com.p2pchat.net
  - ConnectionManager — peer connections, server reachability tests, network IO
  - Protocol framing and peer handshake implementation belong here

- com.p2pchat.crypto
  - KeyManager — key generation, storage interface, public key fingerprinting
  - Crypto helpers: encrypt/decrypt, generate fingerprints, checksums

- com.p2pchat.storage
  - DatabaseConnection — DB lifecycle
  - MySQLStorage — concrete storage backed by MySQL (users, messages, files, contacts)

- com.p2pchat.util
  - Config — loads config.properties and exposes typed getters
  - other helpers (formatting, log wrappers)

Runtime control flow

1) Startup
- CLIApp loads Config and attempts to initialize DatabaseConnection with config.getDatabaseUrl()/username/password.
- If DB connection succeeds, MySQLStorage is used; otherwise the app runs in offline mode (storage == null) and components are initialized with fallback implementations.
- KeyManager, ConnectionManager, ConversationManager, MessageManager are initialized and wired together.

2) User interaction
- CLIApp runs an interactive read-eval-print loop and forwards user commands to CommandHandler.
- CommandHandler invokes core managers for business actions (register/login/send/sendfile/etc.).

Message send sequence (simplified)

User types: send <phone> <text>
-> CommandHandler.handleSend
-> validate phone and currentUser
-> MessageManager.sendMessage(recipientPhone, messageText)
-> MessageManager constructs Message model, persists via storage (if available), passes to ConnectionManager to deliver to peer or server
-> on delivery failure, retry based on network.retryCount (config)
-> ConversationManager updates thread view and storage

File transfer sequence (simplified)

User types: sendfile <phone>
-> CommandHandler prompts for file path
-> FileManager.saveFile(file, senderPhone) stores file in storage.basePath and creates FileMeta
-> FileManager chunks large files (storage.chunkSize) and optionally encrypts chunks via KeyManager
-> FileManager initiates transfer via ConnectionManager to peer
-> Receiver reassembles chunks into final file, verifies checksum, stores under storage.basePath, and creates a file message record

Data model (representative)
- User
  - phoneNumber (String)
  - displayName (String)
  - publicKeyFingerprint (String)
  - profileStatus (String)
  - createdAt, lastSeen (DateTime)

- Message
  - id (UUID)
  - senderPhone, receiverPhone (String)
  - createdAt (DateTime)
  - type (TEXT, FILE, IMAGE, AUDIO, VIDEO)
  - payload (text or file metadata reference)
  - status (SENT, DELIVERED, READ)

- FileMeta
  - fileId (UUID)
  - originalName, fileSize, mimeType
  - chunkCount, checksum
  - storedPath (relative to storage.basePath)

Storage layout
- storage.basePath (config.properties, default p2pchat_files/)
  - files/ — actual file blobs (chunk files during transfer / assembled files)
  - metadata/ — JSON or DB-backed metadata describing stored files
  - logs/ — optional transfer logs

Design decisions and rationale
- CLI-first design: simple text UI keeps dependencies minimal and supports automation/scripts.
- Pluggable storage: the app uses MySQLStorage if DB is available; otherwise it runs offline — this improves portability for demos and local usage.
- Chunked file transfers (storage.chunkSize) allow large-file transfers without needing huge memory.
- KeyManager and crypto package provide extension points for adding secure end-to-end encryption later.

Extensibility points
- Add a pluggable transport (WebRTC, QUIC) by implementing a new ConnectionManager
- Add group chat by expanding Conversation model to support conversationId and participant lists
- Add background daemon mode and systemd unit for running as a persistent service

Limitations
- Current server-start/stop and listing admin commands are placeholders and need concrete implementations
- No unit tests shipped in the repository — add JUnit tests for core managers
- Key exchange and authentication are basic; review docs/SECURITY.md for recommendations

If you want, I can include sequence diagrams in plantuml format and add a simple class diagram mapping the most important classes.