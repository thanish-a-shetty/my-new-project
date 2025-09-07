# Chatbot Service

A cloud-facing AI-powered financial chatbot service that provides educational financial assistance with knowledge retrieval, PII sanitization, and comprehensive chat logging.

## Features

- AI-powered financial assistance using OpenAI GPT models
- PII detection and sanitization (email, phone, SSN)
- Knowledge base retrieval with keyword and vector search
- Rate limiting per user (20 requests/minute)
- Comprehensive chat logging and audit trail
- Educational disclaimer and financial advisor recommendations

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- OpenAI GPT API
- H2 Database
- Maven

## Architecture

### Processing Pipeline
1. **Sanitization**: Remove PII and log sanitized text
2. **Retrieval**: Keyword search + optional vector search
3. **Prompt Building**: System message + context + user query
4. **LLM Call**: OpenAI API integration
5. **Response**: Return answer with sources
6. **Logging**: Store chat interaction

### PII Detection
- **Email**: `user@example.com` → `[EMAIL_REDACTED]`
- **Phone**: `(555) 123-4567` → `[PHONE_REDACTED]`
- **SSN**: `123-45-6789` → `[SSN_REDACTED]`

## API Endpoints

### Chat Interaction
- `POST /api/chat/message` - Send chat message and get AI response
- `GET /api/chat/history/{userId}` - Get chat history for user
- `GET /api/chat/stats/{userId}` - Get chat statistics for user
- `GET /api/chat/health` - Health check endpoint

## Request/Response Format

### Chat Request
```json
{
  "userId": 100,
  "message": "What is compound interest?"
}
```

### Chat Response
```json
{
  "answer": "Compound interest is interest calculated on the initial principal and the accumulated interest from previous periods...",
  "sources": [
    "Financial Education Guide - Compound Interest",
    "Investment Basics - Interest Calculations"
  ],
  "sanitizedMessage": "What is compound interest?"
}
```

## System Message

The prompt builder MUST prepend this system message:
```
System: "You are an educational finance assistant. THIS IS NOT FINANCIAL ADVICE. 
If user asks for personalised recommendations, respond with 'Consult a licensed 
financial advisor' and provide educational resources."
```

## Configuration

### Environment Variables
- `OPENAI_API_KEY` - OpenAI API key for LLM calls
- `VECTOR_DB_URL` - Vector database URL (optional)
- `VECTOR_DB_API_KEY` - Vector database API key (optional)
- `SPRING_DATASOURCE_URL` - Database URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

### Application Properties
```yaml
openai:
  api:
    key: ${OPENAI_API_KEY:}
    model: ${OPENAI_MODEL:gpt-3.5-turbo}
    max-tokens: 1000
    temperature: 0.7
    timeout: 30000

vector:
  db:
    enabled: ${VECTOR_DB_ENABLED:false}
    url: ${VECTOR_DB_URL:}
    api-key: ${VECTOR_DB_API_KEY:}

chat:
  rate-limit:
    requests-per-minute: 20
  retrieval:
    top-k: 5
  sanitization:
    enabled: true
    log-pii-detection: true
```

## Data Models

### Knowledge Document
```java
{
  "id": 1,
  "title": "Compound Interest Guide",
  "content": "Compound interest is...",
  "category": "education",
  "tags": "interest,compound,finance",
  "relevanceScore": 0.95,
  "active": true,
  "createdAt": "2023-12-21T10:00:00"
}
```

### Chat Log
```java
{
  "id": 1,
  "userId": 100,
  "query": "What is compound interest?",
  "answer": "Compound interest is...",
  "sources": "Financial Education Guide;Investment Basics",
  "timestamp": "2023-12-21T10:00:00",
  "responseTimeMs": 1500,
  "tokensUsed": 150
}
```

## PII Handling

### Detection Patterns
- **Email**: `\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b`
- **Phone**: `\\b(\\+?1[-.]?)?\\(?([0-9]{3})\\)?[-.]?([0-9]{3})[-.]?([0-9]{4})\\b`
- **SSN**: `\\b\\d{3}-?\\d{2}-?\\d{4}\\b`

### Response for PII Detection
```json
{
  "answer": "I can't process PII — please remove personal information.",
  "sources": null,
  "sanitizedMessage": "My email is [EMAIL_REDACTED] and phone is [PHONE_REDACTED]"
}
```

## Knowledge Retrieval

### Keyword Search
- Searches title, content, and tags
- Case-insensitive matching
- Ordered by relevance score

### Vector Search (Optional)
- Semantic similarity search
- Configurable via `VECTOR_DB_URL`
- Fallback to keyword search if unavailable

## Rate Limiting

- **Limit**: 20 requests per minute per user
- **Burst**: 5 requests allowed
- **Response**: "Rate limit exceeded. Please wait before sending another message."

## Running the Application

1. Configure environment variables
2. Run with Maven:
   ```bash
   mvn spring-boot:run
   ```
3. Access H2 console at: http://localhost:8084/chatbot-service/h2-console

## Testing

The project includes JUnit 5 unit test stubs for:
- PII sanitization function (email, phone, SSN detection)
- Prompt builder with system message validation
- Rate limiting functionality
- Error handling scenarios

Run tests with:
```bash
mvn test
```

## Production Considerations

The following areas require production hardening (marked with `// REVIEW:` comments):

1. **CORS Configuration**: Configure specific origins instead of allowing all
2. **OpenAI Integration**: Implement actual OpenAI API calls
3. **Vector Database**: Implement actual vector database integration
4. **Logging/Privacy**: Implement production logging and privacy rules
5. **Error Handling**: Add comprehensive error handling and recovery
6. **Security**: Implement proper authentication and authorization
7. **Monitoring**: Add metrics and health checks
8. **Performance**: Optimize for high-volume chat interactions
9. **Scalability**: Add clustering and load balancing support
10. **Data Retention**: Implement chat log retention policies

## Development Notes

- Uses H2 in-memory database for development
- OpenAI integration is stubbed for development
- Vector database integration is optional
- PII detection uses regex patterns
- Rate limiting uses in-memory tracking
- Chat logging provides comprehensive audit trail
- System message ensures educational disclaimer
- Knowledge retrieval supports both keyword and vector search
