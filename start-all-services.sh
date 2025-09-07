#!/bin/bash

# Fintech Microservices Platform - Start All Services
# This script starts all microservices in parallel for development

echo "🚀 Starting Fintech Microservices Platform..."
echo "=================================================="

# Function to start a service in background
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    
    echo "🔄 Starting $service_name on port $port..."
    cd "$service_dir" || { echo "❌ Failed to cd to $service_dir"; exit 1; }
    
    if [[ "$service_name" == "Frontend" ]]; then
        # Handle Node.js frontend
        if [[ ! -d "node_modules" ]]; then
            echo "📦 Installing npm dependencies for $service_name..."
            npm install
        fi
        npm start > "../logs/${service_name,,}.log" 2>&1 &
    else
        # Handle Spring Boot services
        mvn spring-boot:run > "../logs/${service_name,,}.log" 2>&1 &
    fi
    
    local pid=$!
    echo "✅ $service_name started with PID $pid"
    echo "$pid" > "../.${service_name,,}.pid"
    cd ..
}

# Create logs directory
mkdir -p logs

echo "🧹 Cleaning up any existing processes..."
./stop-all-services.sh 2>/dev/null || true

echo ""
echo "🚀 Starting all services..."
echo ""

# Start all services
start_service "Auth-Service" "auth-service" "8080"
start_service "Portfolio-Service" "portfolio-service" "8082" 
start_service "SIP-Service" "sip-service" "8083"
start_service "Market-Ingest" "market-ingest" "8081"
start_service "Chatbot-Service" "chatbot-service" "8084"
start_service "Frontend" "frontend" "3000"

echo ""
echo "⏳ Waiting for services to start up..."
sleep 10

echo ""
echo "🎉 All services started successfully!"
echo "=================================================="
echo "📊 Service Status:"
echo "🔐 Auth Service:       http://localhost:8080"
echo "💼 Portfolio Service:  http://localhost:8082"  
echo "💰 SIP Service:        http://localhost:8083"
echo "📈 Market Ingest:      http://localhost:8081"
echo "🤖 Chatbot Service:    http://localhost:8084"
echo "🌐 Frontend:           http://localhost:3000"
echo ""
echo "📋 H2 Database Consoles:"
echo "🔐 Auth DB:            http://localhost:8080/auth-service/h2-console"
echo "💼 Portfolio DB:       http://localhost:8082/portfolio-service/h2-console"
echo "💰 SIP DB:             http://localhost:8083/sip-service/h2-console"
echo "🤖 Chatbot DB:         http://localhost:8084/chatbot-service/h2-console"
echo ""
echo "📄 Logs are available in the 'logs/' directory"
echo "🛑 To stop all services, run: ./stop-all-services.sh"
echo ""
echo "🎯 Open your browser to http://localhost:3000 to get started!"
