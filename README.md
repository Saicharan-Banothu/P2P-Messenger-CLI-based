# P2P Messenger — CLI-based Peer-to-Peer Chat System

## Overview
P2P Messenger is a lightweight Java command-line peer-to-peer chat and file-transfer application. It provides a WhatsApp-like CLI experience for direct messaging, contact management, and file exchange between peers. The application supports an offline mode (file-based) and an optional MySQL-backed mode for persistent storage. It is intended for developers, power users, and educators who want a minimal, extensible P2P chat platform.

## Key features
- Text messaging between peers
- File send and download workflow with chunking support
- Contact management and conversation history
- Optional MySQL persistence (configurable via config.properties)
- Admin mode for server/database management commands
- Pluggable crypto/key management (KeyManager integration)

## Quick links
- Source: src/main/java/com/p2pchat
- CLI entrypoint: com.p2pchat.cli.CLIApp
- Core managers: com.p2pchat.core (MessageManager, FileManager, ConversationManager)
- Configuration: config.properties

## Minimum requirements
- Java SE 11 or later
- (Optional) MySQL server if you want persistent storage

## Build and run (quick)
1. Compile from repository root:

Unix / macOS:

javac -d out $(find src -name "*.java")

Windows (PowerShell):

Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName } | %{ javac -d out $_ }

2. Run the CLI:

java -cp out com.p2pchat.cli.CLIApp

3. Notes
- Ensure config.properties is available in the working directory; the app reads database and storage settings from it.
- The app detects a database and runs in online mode if a MySQL connection can be established; otherwise it falls back to an offline mode.

## Where to find documentation
- Full CLI reference: docs/USAGE.md
- Architecture and design: docs/ARCHITECTURE.md
- Security and crypto guidance: docs/SECURITY.md
- Contribution guidelines: CONTRIBUTING.md

## Contributing
We welcome contributions. See CONTRIBUTING.md for the process, coding style, and testing guidance.

## License
This repository is licensed under the MIT License — see LICENSE for details.

---

_Contributors:_ Sai Charan and Meghana
