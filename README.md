# Fintech Microservices Platform

A comprehensive fintech platform built with microservices architecture, featuring real-time market data ingestion, portfolio management, systematic investment plans (SIP), AI-powered chatbot, and secure authentication.

## 🏗️ Architecture Overview

This platform consists of 6 microservices designed to work together to provide a complete fintech solution:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │  Auth Service   │    │ Portfolio Svc   │
│  React/TypeScript│◄──►│  JWT + Email    │◄──►│ Holdings & P&L  │
│  Port: 3000     │    │  Port: 8080     │    │ Port: 8082      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                        │
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Chatbot Service │    │   SIP Service   │    │ Market Ingest   │
│ AI-Powered Chat │    │ Scheduled SIPs  │    │ Real-time Data  │
│ Port: 8084      │    │ Port: 8083      │    │ Port: 8081      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 Services

### 1. **Auth Service** (Port 8080)
- **Purpose**: User authentication & authorization
- **Tech Stack**: Spring Boot, JWT, Spring Security, Email Verification
- **Features**:
  - User registration with email verification
  - JWT-based authentication
  - Password encryption (BCrypt)
  - Account activation flow
- **Key Endpoints**:
  - `POST /api/auth/signup` - User registration
  - `POST /api/auth/login` - User authentication  
  - `GET /api/auth/verify` - Email verification

### 2. **Portfolio Service** (Port 8082)
- **Purpose**: Portfolio management & P&L calculation
- **Tech Stack**: Spring Boot, JPA, H2 Database
- **Features**:
  - CRUD operations for holdings
  - Real-time P&L calculation (realized & unrealized)
  - Portfolio valuation with allocation percentages
  - Price cache integration
- **Key Endpoints**:
  - `GET /api/portfolio/{userId}/valuation` - Portfolio valuation
  - `GET /api/portfolio/{userId}/holdings` - User holdings
  - `POST /api/portfolio/{userId}/holdings` - Add holding
  - `GET /api/portfolio/{userId}/holdings/{holdingId}/pnl` - P&L calculation

### 3. **SIP Service** (Port 8083)
- **Purpose**: Systematic Investment Plan management
- **Tech Stack**: Spring Boot, Spring Scheduler, FCM, Email
- **Features**:
  - Scheduled SIP processing (daily at 9 AM UTC)
  - Multiple frequencies (Daily, Weekly, Monthly, Quarterly, Yearly)
  - FCM push notifications
  - Email notifications
  - Comprehensive audit logging
- **Scheduling**: Configurable cron expressions for flexible SIP execution

### 4. **Market Ingest Service** (Port 8081)
- **Purpose**: Real-time market data ingestion
- **Tech Stack**: Spring Boot, WebSocket, STOMP
- **Features**:
  - WebSocket client with auto-reconnection
  - Mock data generation for testing
  - STOMP message publishing
  - Rate limiting per symbol
  - Adapter pattern for multiple data providers
- **Topics**:
  - `/topic/price/{symbol}` - Real-time price updates
  - `/topic/error/{symbol}` - Error messages
  - `/topic/status/connection` - Connection status

### 5. **Chatbot Service** (Port 8084)
- **Purpose**: AI-powered financial assistant
- **Tech Stack**: Spring Boot, OpenAI API, Vector Database
- **Features**:
  - AI-powered financial education assistance
  - PII detection and sanitization
  - Knowledge base retrieval (keyword + vector search)
  - Rate limiting (20 requests/minute per user)
  - Comprehensive chat logging
- **Safety**: Educational disclaimer, no personalized financial advice

### 6. **Frontend** (Port 3000)
- **Purpose**: User interface
- **Tech Stack**: React, TypeScript, STOMP, Tailwind CSS
- **Features**:
  - Real-time chat interface
  - STOMP integration for live data
  - Responsive design
  - API integration with all services

## 🛠️ Prerequisites

Before running the services, ensure you have:

- **Java 17** or higher
- **Node.js 16** or higher
- **Maven 3.8** or higher
- **npm** or **yarn**

## ⚡ Quick Start

### Option 1: Run All Services (Recommended)

```bash
# Clone the repository
git clone https://github.com/thanish-a-shetty/my-new-project.git
cd my-new-project

# Start all services in parallel
./start-all-services.sh
```

### Option 2: Manual Service Startup

```bash
# Terminal 1: Auth Service
cd auth-service
mvn spring-boot:run
# Runs on http://localhost:8080

# Terminal 2: Portfolio Service  
cd portfolio-service
mvn spring-boot:run
# Runs on http://localhost:8082

# Terminal 3: SIP Service
cd sip-service
mvn spring-boot:run
# Runs on http://localhost:8083

# Terminal 4: Market Ingest Service
cd market-ingest
mvn spring-boot:run  
# Runs on http://localhost:8081

# Terminal 5: Chatbot Service
cd chatbot-service
mvn spring-boot:run
# Runs on http://localhost:8084

# Terminal 6: Frontend
cd frontend
npm install
npm start
# Runs on http://localhost:3000
```

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the root directory:

```bash
# Auth Service
JWT_SECRET=your-super-secret-jwt-key-min-256-bits
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password

# Chatbot Service
OPENAI_API_KEY=sk-your-openai-api-key
VECTOR_DB_URL=https://your-vector-db-url
VECTOR_DB_API_KEY=your-vector-db-key

# SIP Service
FCM_SERVER_KEY=your-fcm-server-key

# Market Ingest Service
MARKET_WS_URL=wss://market-data-provider.com/ws
MARKET_WS_KEY=your-market-data-api-key

# Database (Production)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fintech
SPRING_DATASOURCE_USERNAME=fintech_user
SPRING_DATASOURCE_PASSWORD=secure_password
```

### Development Mode

All services use H2 in-memory databases by default for easy development:

- **Auth Service**: http://localhost:8080/auth-service/h2-console
- **Portfolio Service**: http://localhost:8082/portfolio-service/h2-console  
- **SIP Service**: http://localhost:8083/sip-service/h2-console
- **Chatbot Service**: http://localhost:8084/chatbot-service/h2-console

**Login credentials**: `sa` / (empty password)

## 📊 API Documentation

### Authentication Flow
```bash
# 1. Register user
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123", 
    "firstName": "John",
    "lastName": "Doe"
  }'

# 2. Verify email (check your inbox for verification link)
# Click the verification link or:
curl "http://localhost:8080/api/auth/verify?token=VERIFICATION_TOKEN"

# 3. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Portfolio Management
```bash
# Get portfolio valuation
curl "http://localhost:8082/api/portfolio/100/valuation"

# Add a holding
curl -X POST "http://localhost:8082/api/portfolio/100/holdings" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "quantity": 10,
    "averagePrice": 150.0
  }'

# Get P&L for holding
curl "http://localhost:8082/api/portfolio/100/holdings/1/pnl"
```

### Chat with AI Assistant
```bash
# Send message to chatbot
curl -X POST "http://localhost:8084/api/chat/message" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 100,
    "message": "What is compound interest?"
  }'
```

## 🧪 Testing

Run tests for all services:

```bash
# Test all services
./test-all-services.sh

# Or test individual services
cd auth-service && mvn test
cd portfolio-service && mvn test  
cd sip-service && mvn test
cd market-ingest && mvn test
cd chatbot-service && mvn test
cd frontend && npm test
```

## 📈 Production Deployment

### Docker Support (TODO)

```bash
# Build all services
docker-compose build

# Run with PostgreSQL
docker-compose up -d
```

### Production Checklist

**Security**:
- [ ] Replace H2 with PostgreSQL/MySQL
- [ ] Configure proper CORS origins
- [ ] Implement API rate limiting
- [ ] Add comprehensive input validation
- [ ] Set up HTTPS/TLS certificates

**Monitoring**:
- [ ] Add health checks and metrics
- [ ] Implement centralized logging
- [ ] Set up alerts and monitoring dashboards
- [ ] Add performance monitoring

**Scalability**:
- [ ] Implement Redis cache
- [ ] Add load balancing
- [ ] Set up message queues (RabbitMQ/Kafka)
- [ ] Configure auto-scaling

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/your-feature-name`
3. **Commit** your changes: `git commit -m 'Add some feature'`
4. **Push** to the branch: `git push origin feature/your-feature-name`  
5. **Submit** a Pull Request

### Development Guidelines

- Follow existing code style and conventions
- Add comprehensive tests for new features
- Update documentation for any API changes
- Ensure all tests pass before submitting PR
- Add `// REVIEW:` comments for production considerations

## 📋 Roadmap

### Phase 1 (Current)
- [x] Microservices architecture
- [x] Authentication system
- [x] Portfolio management  
- [x] SIP scheduling
- [x] Real-time market data
- [x] AI chatbot
- [x] Frontend interface

### Phase 2 (Planned)
- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] Production database migration
- [ ] API Gateway integration
- [ ] Advanced monitoring
- [ ] Mobile app development

### Phase 3 (Future)
- [ ] Machine learning models
- [ ] Advanced trading strategies
- [ ] Multi-currency support
- [ ] Regulatory compliance features
- [ ] Third-party integrations

## 🔐 Security & Compliance

- **PII Protection**: Chatbot service includes PII detection and sanitization
- **Educational Focus**: Financial advice disclaimer in AI responses
- **JWT Security**: Secure token-based authentication
- **Data Encryption**: Passwords encrypted with BCrypt
- **Rate Limiting**: Prevents abuse and ensures fair usage

## 📞 Support

For questions, issues, or contributions:

- **GitHub Issues**: [Create an issue](https://github.com/thanish-a-shetty/my-new-project/issues)
- **Discussions**: [GitHub Discussions](https://github.com/thanish-a-shetty/my-new-project/discussions)
- **Email**: [Contact the maintainer](mailto:thanish.shetty@example.com)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**⚠️ Disclaimer**: This is a demonstration fintech platform for educational purposes. Do not use in production without proper security auditing, regulatory compliance, and comprehensive testing. Always consult with licensed financial advisors for investment decisions.
