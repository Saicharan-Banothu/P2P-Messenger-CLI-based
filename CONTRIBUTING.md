# Contributing to P2P Messenger

Thanks for your interest in contributing! This document explains how to build, test and submit improvements.

Getting the code
- Fork the repository and create a feature branch: git checkout -b feat/your-feature

Build & run locally
- Compile all Java sources from the repository root:

javac -d out $(find src -name "*.java")

- Run the CLI:

java -cp out com.p2pchat.cli.CLIApp

Suggested toolchain
- JDK 11+
- Add Maven or Gradle for easier builds (recommended). If you add a build file, include a CI workflow to run mvn/gradle on PRs.

Testing
- Add unit tests under src/test/java using JUnit 5.
- Mock network behavior when testing ConnectionManager and MessageManager.

Coding style
- Use standard Java conventions (4-space indentation, UTF-8 encoding).
- Keep packages aligned with com.p2pchat.* and avoid cross-package tight coupling.

Pull requests
1. Fork the repo and create a topic branch (feat/my-feature)
2. Ensure tests pass locally
3. Create a PR against the main branch with a clear title and description
4. Reference related issues in the PR description

Commit messages
- Use conventional commits style for clarity (e.g., feat:, fix:, docs:, refactor:).

Security & sensitive data
- Do not add secrets to the repository. Use placeholders in config files and document how to supply secrets via environment variables.

Roadmap and feature requests
- Open issues for large feature requests and discuss design before implementing major changes.

Need help?
- Open an issue describing the problem and tag it with help wanted. Maintainers will help triage and guide implementation.