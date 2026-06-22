# Security — P2P Messenger

This document explains the security considerations, encryption options, and recommendations for how to handle secrets and keys for this project.

Sanity check (repo currently contains a password)
- The repository originally included a plaintext DB password in config.properties. I replaced this with a placeholder during the documentation commit.
  - If you previously committed real secrets, rotate them immediately (change DB password, revoke keys).

Config and secrets handling
- Do NOT commit production passwords, private keys, or API tokens to the repository.
- Recommended approaches:
  - Use environment variables for sensitive values and update Config to read env vars first (e.g., DB_PASSWORD env var) and fall back to config file for non-sensitive defaults.
  - Use a secrets manager (HashiCorp Vault, AWS Secrets Manager, GitHub Encrypted Secrets for CI) for production deployments.

Transport and encryption
- The repo includes a crypto package and KeyManager. Currently the code generates fingerprints and provides encryption hooks; however the exact algorithms used must be confirmed in com.p2pchat.crypto.

Recommended crypto stack for end-to-end security
- Key agreement: ECDH (Curve25519 / X25519) to establish a shared symmetric secret between peers.
- Authenticated encryption: AES-256-GCM or ChaCha20-Poly1305 used with the derived symmetric key.
- Key authentication: sign public keys using Ed25519 and verify to avoid man-in-the-middle attacks.
- Fingerprints: use SHA-256 truncated hex (or base58) to display a short human-verifiable fingerprint.

Key management
- Private keys must be stored encrypted at rest. Options:
  - Use an encrypted key store (PKCS#12) protected with a passphrase.
  - Use OS-level key stores where available (Keychain on macOS, Windows Credential Manager, libsecret on Linux).
  - Require user to enter a passphrase at the start of the CLI session to unlock the private key.

Authentication & trust model
- For initial bootstrap, peers can rely on manual fingerprint verification (display fingerprint when peers connect, users verify via a secondary channel).
- For improved UX, implement a trust-on-first-use (TOFU) mode where the first connection stores the remote public key and warns on future changes.

Database and storage
- Use parameterized SQL queries in MySQLStorage to avoid SQL injection.
- Store only public key fingerprints in the database, not private keys.
- Files stored under storage.basePath should include integrity metadata (e.g., SHA-256 checksum) and access permissions should restrict access to the application user.

Networking
- If a central server is used as a relay, always use TLS for server-client links.
- Use certificate pinning or at least validate server certificates to prevent MITM for server-based connections.

Operational recommendations
- Rotate credentials and keys if there is any suspicion of exposure.
- Add rate-limiting and input validation to network-facing endpoints to mitigate abuse.
- Add logging with privacy in mind: mask sensitive fields and avoid logging raw message payloads in production.

Development recommendations
- Add integration tests for encryption/decryption round-trips.
- Add unit tests for fingerprint generation and verification.
- Provide a script to generate key pairs securely and instructions for safe private key import/export.

If you want, I can implement a secure key storage example and update KeyManager to use AES-GCM encrypted private key files and environment-based DB credentials.