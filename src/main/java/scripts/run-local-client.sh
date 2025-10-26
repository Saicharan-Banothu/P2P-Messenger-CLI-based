#!/bin/bash

# P2PChat-CLI Local Client Runner for Linux/Mac

echo "Starting P2PChat-CLI Client..."

# Set Java options
export JAVA_OPTS="-Xmx256m -Dlog.level=INFO"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Run the application
mvn -q -DskipTests exec:java -Dexec.mainClass="com.p2pchat.cli.CLIApp" -Dexec.args="$@"