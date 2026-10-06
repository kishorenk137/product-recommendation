#!/usr/bin/env bash
# ==============================================================================
# One-Click Linux VPS Deployment Script (Ubuntu / Debian)
# Deploys Docker, Docker Compose, and starts the containerized stack
# ==============================================================================
set -e

echo "=== 1. Updating System Packages ==="
sudo apt-get update -y && sudo apt-get upgrade -y

echo "=== 2. Installing Docker & Docker Compose Plugin ==="
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
fi

echo "=== 3. Building and Starting Application via Docker Compose ==="
sudo docker compose down --remove-orphans || true
sudo docker compose build --no-cache
sudo docker compose up -d

echo "=== 4. Checking Container Status ==="
sudo docker compose ps

echo "=============================================================================="
echo "Deployment Complete! Application running on port 8080."
echo "Access in browser: http://$(curl -s ifconfig.me):8080"
echo "Login: admin / admin123"
echo "=============================================================================="
