# Usage — P2P Messenger (CLI)

This document covers the CLI commands, examples and typical workflows for the P2P Messenger CLI application.

Prerequisites
- Java 11+ installed
- config.properties available in the working directory (see README and docs/SECURITY.md for storage of secrets)

Start the app

1. Compile (from repository root):

Unix/macOS:

javac -d out $(find src -name "*.java")

Windows (PowerShell):

Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName } | %{ javac -d out $_ }

2. Run the CLI:

java -cp out com.p2pchat.cli.CLIApp

Basic workflow (user mode)

1) Register
> register
- Prompts: display name, phone number
- Creates a user, optionally stores in MySQL if configured

2) Login
> login <phoneNumber>
- Example: login 9876543210

3) Sending text messages
> send <phoneNumber> <message>
- Example: send 9876543210 Hello, this is a test!

4) Sending files
> sendfile <phoneNumber>
- Prompts for a file path. The file is saved locally via FileManager and then a file notification message is sent.
- Supported extensions are shown before prompting (FileManager.getSupportedExtensions()).

5) Downloading files
> download <fileId>
- Downloads a previously received file by its ID. Files are stored under the configured storage.basePath.

6) Contacts and conversations
> add-contact <phoneNumber>
> contacts
> chats
> chat <phoneNumber>
> inbox

7) Other
> online — request list of online users
> status — show current status
> profile — show current user profile
> help — show user help
> exit / back — leave user mode

Admin mode

Start the app and choose Admin Mode. Authenticate with the admin secret key (default admin123).

Admin commands
- server-status — show server connection and reachability
- server-start — start the server (placeholder implementation)
- server-stop — stop the server (placeholder implementation)
- db-status — show database connection status
- db-tables — list database table names (assumed active)
- db-stats — show total counts (users/messages/files)
- db-reset — reset the database (requires confirmation)
- users-list — list users (placeholder)
- messages-list — list messages (placeholder)
- connections-list — list current connections (placeholder)
- system-status — show global system state
- help — show admin help

Examples (sample session)

$ java -cp out com.p2pchat.cli.CLIApp
=== 📱 P2PChat - WhatsApp-like Messenger ===

🔐 SELECT MODE
1. 👤 User Mode
2. ⚙️  Admin Mode
3. ❌ Exit
Choose mode (1-3): 1

🎯 USER MODE ACTIVATED
Type 'help' for available commands

p2pchat-user> register
📝 Enter your display name: Alice
📱 Enter your phone number (10 digits, starting with 6-9): 9876543210
✅ Registered successfully!
🎉 Welcome to P2PChat! You're automatically logged in.

p2pchat-user> send 9123456789 Hello from Alice
✅ Message queued/sent

p2pchat-user> sendfile 9123456789
📁 Supported file types:
[.png, .jpg, .pdf, ...]
📁 Enter file path: /home/alice/document.pdf
✅ File saved locally: document.pdf
✅ File notification sent

p2pchat-user> exit
👋 Exiting user mode...

Helpful notes
- If the MySQL database is not available the app runs in offline mode and persists data only to local storage via FileManager.
- File storage directory defaults to storage.basePath in config.properties (default: p2pchat_files/).
- Use the admin mode with care; db-reset will drop and re-initialize schema (if implemented).

If you want, I can add example scripts (start.sh / start.bat) to simplify running the app on your machine.