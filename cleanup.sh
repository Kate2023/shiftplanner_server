#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "========================================="
echo "   Stopping & Removing Containers"
echo "========================================="

# 1. Remove the Java Application Container
APP_CONTAINER="shiftplanner"
if [ "$(docker ps -aq -f name=^${APP_CONTAINER}$)" ]; then
    echo "Stopping and removing container: $APP_CONTAINER..."
    docker rm -f "$APP_CONTAINER" > /dev/null
    echo "✅ Application container removed."
else
    echo "ℹ️ Container '$APP_CONTAINER' was not running."
fi

# 2. Remove the PostgreSQL Container
POSTGRES_CONTAINER="dev-postgres"
if [ "$(docker ps -aq -f name=^${POSTGRES_CONTAINER}$)" ]; then
    echo "Stopping and removing container: $POSTGRES_CONTAINER..."
    docker rm -f "$POSTGRES_CONTAINER" > /dev/null
    echo "✅ Database container removed."
else
    echo "ℹ️ Container '$POSTGRES_CONTAINER' was not running."
fi

echo "========================================="
echo "   Removing Docker Network"
echo "========================================="

# 3. Remove the App Bridge Network
APP_NETWORK="app-network"
if docker network inspect "$APP_NETWORK" &> /dev/null; then
    echo "Removing network: $APP_NETWORK..."
    docker network rm "$APP_NETWORK" > /dev/null
    echo "✅ Docker network removed."
else
    echo "ℹ️ Docker network '$APP_NETWORK' does not exist."
fi

echo "========================================="
echo "🎉 Cleanup complete! The environment is completely reset."
echo "========================================="
