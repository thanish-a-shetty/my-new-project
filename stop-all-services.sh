#!/bin/bash

# Fintech Microservices Platform - Stop All Services
# This script stops all running microservices

echo "🛑 Stopping Fintech Microservices Platform..."
echo "=============================================="

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file=".${service_name,,}.pid"
    
    if [[ -f "$pid_file" ]]; then
        local pid=$(cat "$pid_file")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo "🔄 Stopping $service_name (PID: $pid)..."
            kill "$pid" 2>/dev/null
            # Wait for graceful shutdown
            local count=0
            while ps -p "$pid" > /dev/null 2>&1 && [[ $count -lt 10 ]]; do
                sleep 1
                ((count++))
            done
            
            # Force kill if still running
            if ps -p "$pid" > /dev/null 2>&1; then
                echo "⚡ Force stopping $service_name..."
                kill -9 "$pid" 2>/dev/null
            fi
            
            echo "✅ $service_name stopped"
        else
            echo "⚠️  $service_name was not running (stale PID file)"
        fi
        rm -f "$pid_file"
    else
        echo "ℹ️  No PID file found for $service_name"
    fi
}

# Stop all services
stop_service "Auth-Service"
stop_service "Portfolio-Service"
stop_service "SIP-Service" 
stop_service "Market-Ingest"
stop_service "Chatbot-Service"
stop_service "Frontend"

echo ""
echo "🧹 Cleaning up additional processes..."

# Kill any remaining Spring Boot processes
pkill -f "spring-boot:run" 2>/dev/null || true

# Kill any remaining npm processes for our frontend
pkill -f "react-scripts start" 2>/dev/null || true

# Kill processes on known ports (be more specific)
for port in 8080 8081 8082 8083 8084 3000; do
    local pid=$(lsof -ti:$port 2>/dev/null)
    if [[ -n "$pid" ]]; then
        echo "🔄 Stopping process on port $port (PID: $pid)..."
        kill "$pid" 2>/dev/null || true
    fi
done

echo ""
echo "✅ All services stopped successfully!"
echo "📄 Logs are preserved in the 'logs/' directory"
