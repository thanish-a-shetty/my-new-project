#!/bin/bash

# Fintech Microservices Platform - Test All Services
# This script runs tests for all microservices

echo "🧪 Running tests for Fintech Microservices Platform..."
echo "======================================================"

# Function to run tests for a service
test_service() {
    local service_name=$1
    local service_dir=$2
    
    echo ""
    echo "🔄 Testing $service_name..."
    echo "------------------------"
    
    cd "$service_dir" || { echo "❌ Failed to cd to $service_dir"; exit 1; }
    
    if [[ "$service_name" == "Frontend" ]]; then
        # Handle Node.js frontend tests
        if [[ ! -d "node_modules" ]]; then
            echo "📦 Installing npm dependencies for $service_name..."
            npm install
        fi
        
        echo "🏃 Running npm tests for $service_name..."
        npm test -- --coverage --watchAll=false
        local exit_code=$?
    else
        # Handle Spring Boot service tests
        echo "🏃 Running Maven tests for $service_name..."
        mvn test -q
        local exit_code=$?
    fi
    
    if [[ $exit_code -eq 0 ]]; then
        echo "✅ $service_name tests passed!"
    else
        echo "❌ $service_name tests failed!"
        cd ..
        return $exit_code
    fi
    
    cd ..
    return 0
}

# Track overall success
overall_success=true

# Test all services
test_service "Auth-Service" "auth-service" || overall_success=false
test_service "Portfolio-Service" "portfolio-service" || overall_success=false
test_service "SIP-Service" "sip-service" || overall_success=false
test_service "Market-Ingest" "market-ingest" || overall_success=false
test_service "Chatbot-Service" "chatbot-service" || overall_success=false
test_service "Frontend" "frontend" || overall_success=false

echo ""
echo "======================================================"

if [[ "$overall_success" == true ]]; then
    echo "🎉 All tests passed successfully!"
    echo ""
    echo "📊 Test Summary:"
    echo "✅ Auth Service tests: PASSED"
    echo "✅ Portfolio Service tests: PASSED"
    echo "✅ SIP Service tests: PASSED"
    echo "✅ Market Ingest tests: PASSED"
    echo "✅ Chatbot Service tests: PASSED"
    echo "✅ Frontend tests: PASSED"
    echo ""
    echo "🚀 Your fintech platform is ready for deployment!"
    exit 0
else
    echo "❌ Some tests failed!"
    echo ""
    echo "🔍 Please check the test output above for details."
    echo "🛠️  Fix the failing tests and run this script again."
    exit 1
fi
