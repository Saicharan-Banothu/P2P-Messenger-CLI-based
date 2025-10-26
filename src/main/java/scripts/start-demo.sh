#!/bin/bash

# Demo script to run two clients for testing

echo "Starting P2PChat-CLI Demo..."
echo "Make sure MySQL is running and database is created"

# Terminal 1 - Client 1
echo "Starting Client 1 in new terminal..."
gnome-terminal --title="P2PChat Client 1" -- bash -c "./scripts/run-local-client.sh --profile client1; exec bash"

# Wait a bit
sleep 2

# Terminal 2 - Client 2  
echo "Starting Client 2 in new terminal..."
gnome-terminal --title="P2PChat Client 2" -- bash -c "./scripts/run-local-client.sh --profile client2; exec bash"

echo "Demo clients started. Use 'register' and 'send' commands to test messaging."