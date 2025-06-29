#!/bin/bash

# Kill any process running on port 3004
echo "Checking for processes on port 3004..."
lsof -ti:3004 | xargs kill -9 2>/dev/null || true
echo "Port 3004 cleared"

# Navigate to backend directory
cd backend

# Check if node_modules exists, install dependencies if not
if [ ! -d "node_modules" ]; then
    echo "Installing backend dependencies..."
    npm install
fi

# Start the backend server
echo "Starting backend server on port 3004..."
npm run dev