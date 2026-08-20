#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "========================================="
echo "   Checking Prerequisites & Tool Versions"
echo "========================================="

# 1. Check Java 21
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed." >&2
    exit 1
fi
JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
if [ -z "$JAVA_VER" ]; then
    JAVA_VER=$(java -version 2>&1 | head -n 1 | awk '{print $3}' | tr -d '"' | cut -d. -f1)
fi

if [ "$JAVA_VER" != "21" ]; then
    echo "❌ Error: Java 21 is required. Found version: $JAVA_VER" >&2
    exit 1
fi
echo "✅ Java 21 is installed."

# 2. Check Gradle Wrapper
if [ ! -f "./gradlew" ]; then
    echo "❌ Error: Gradle wrapper (gradlew) not found in the root directory." >&2
    exit 1
fi
echo "✅ Gradle Wrapper found."

# 3. Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Error: Docker is not installed or not running." >&2
    exit 1
fi
echo "✅ Docker is installed: $(docker --version)"


echo "========================================="
echo "   Configuring Network & Environment"
echo "========================================="
# Create a dedicated docker network if it doesn't exist
APP_NETWORK="app-network"
if ! docker network inspect "$APP_NETWORK" &> /dev/null; then
    docker network create "$APP_NETWORK"
    echo "Created Docker network: $APP_NETWORK"
else
    echo "Docker network '$APP_NETWORK' already exists."
fi


echo "========================================="
echo "   Starting PostgreSQL Database Container"
echo "========================================="
POSTGRES_CONTAINER="dev-postgres"

# Stop and remove older instance if it exists to prevent conflict errors
if [ "$(docker ps -aq -f name=^${POSTGRES_CONTAINER}$)" ]; then
    echo "Removing older PostgreSQL container..."
    docker rm -f "$POSTGRES_CONTAINER"
fi

# Run PostgreSQL 18.4 container attached to the shared network
docker run -d \
  --name "$POSTGRES_CONTAINER" \
  --network "$APP_NETWORK" \
  --restart always \
  -e POSTGRES_USER=sp_user \
  -e POSTGRES_PASSWORD=secure_app_password \
  -e POSTGRES_DB=shiftplanner \
  -p 5433:5432 \
  postgres:18.4

echo "⏳ Waiting for PostgreSQL to initialize..."
sleep 5


echo "========================================="
echo "   Compiling & Building Image with Jib"
echo "========================================="
# Using the gradle wrapper to build and push straight to your local Docker daemon
./gradlew clean jibDockerBuild -x test


echo "========================================="
echo "   Starting Java Application Container"
echo "========================================="
APP_CONTAINER="shiftplanner"
APP_IMAGE="shiftplanner_server:latest"

# Stop and remove older instance if it exists
if [ "$(docker ps -aq -f name=^${APP_CONTAINER}$)" ]; then
    echo "Removing older App container..."
    docker rm -f "$APP_CONTAINER"
fi

# Run the app container using your exact production configurations
docker run -d \
  --name "$APP_CONTAINER" \
  --network "$APP_NETWORK" \
  -p 8089:8088 \
  --restart unless-stopped \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://dev-postgres:5432/shiftplanner \
  -e SPRING_DATASOURCE_USERNAME=sp_user \
  -e SPRING_DATASOURCE_PASSWORD=secure_app_password \
  "$APP_IMAGE"


echo "========================================="
echo "   Initializing Shift Planner Context"
echo "========================================="
echo "⏳ Please wait a few seconds for the Java runtime context to boot up..."

# A clean loop that prints dots every second as a countdown
for i in {15..1}; do
    echo -ne "   Booting application... ($i seconds remaining)\r"
    sleep 1
done
echo -e "\n✅ Context initialized successfully."


echo "========================================="
echo "🎉 Setup successful!"
echo "📱 App is running at: http://localhost:8089/1.0/index.html"
echo "========================================="
