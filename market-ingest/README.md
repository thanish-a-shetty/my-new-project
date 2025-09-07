# Market Ingest Service

A Spring Boot service that connects to market data WebSocket feeds, parses tick data, and publishes real-time prices via STOMP messaging.

## Features

- WebSocket client with automatic reconnection
- Adapter pattern for different market data providers
- Mock data generation for testing
- STOMP message publishing
- Rate limiting per symbol
- JSON tick parsing
- Configurable endpoints and topics

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring WebSocket
- Spring STOMP
- Jackson JSON processing
- Maven

## Architecture

### Adapter Pattern
The service uses an adapter pattern to support different market data sources:

- **MockMarketAdapter**: Generates mock data for local testing
- **ProviderMarketAdapter**: Connects to live market data providers (TODO)

### WebSocket Integration
- Connects to market data WebSocket feeds
- Handles connection failures with exponential backoff
- Supports authentication via API keys
- Implements heartbeat mechanism

### STOMP Publishing
- Publishes price data to `/topic/price/{symbol}` topics
- Publishes error messages to `/topic/error/{symbol}` topics
- Publishes connection status to `/topic/status/connection` topic

## API Endpoints

### WebSocket Endpoints
- `/ws` - STOMP endpoint with SockJS fallback
- `/ws-native` - Native WebSocket endpoint

### STOMP Topics
- `/topic/price/{symbol}` - Real-time price updates
- `/topic/error/{symbol}` - Error messages
- `/topic/status/connection` - Connection status updates

## Configuration

### Environment Variables
- `MARKET_WS_URL` - Market data WebSocket URL (default: `mock://localhost`)
- `MARKET_WS_KEY` - API key for market data provider
- `MOCK_DATA_ENABLED` - Enable mock data generation (default: `true`)

### Application Properties
```yaml
market:
  ws:
    url: ${MARKET_WS_URL:mock://localhost}
    key: ${MARKET_WS_KEY:}
    reconnect:
      enabled: true
      initial-delay: 5000
      max-delay: 30000
      multiplier: 2.0
    rate-limit:
      enabled: true
      per-symbol-ms: 100
    heartbeat:
      enabled: true
      interval: 30000

mock:
  data:
    enabled: ${MOCK_DATA_ENABLED:true}
    symbols:
      - AAPL
      - GOOGL
      - MSFT
      - TSLA
      - AMZN
    interval: 1000
    price-range:
      min: 50.0
      max: 500.0
```

## Running the Application

1. Configure environment variables
2. Run with Maven:
   ```bash
   mvn spring-boot:run
   ```
3. Connect to WebSocket at: `ws://localhost:8081/market-ingest/ws`

## Mock Data Mode

When `MARKET_WS_URL` starts with `mock://`, the service generates mock tick data:

```json
{
  "symbol": "AAPL",
  "price": 150.25,
  "ts": 1703123456789
}
```

## STOMP Message Format

### Price Message
```json
{
  "symbol": "AAPL",
  "price": 150.25,
  "marketTimestamp": 1703123456789,
  "publishTimestamp": 1703123456790
}
```

### Error Message
```json
{
  "symbol": "AAPL",
  "errorMessage": "Connection lost",
  "timestamp": 1703123456789
}
```

### Status Message
```json
{
  "status": "connected",
  "message": "Successfully connected to market data feed",
  "timestamp": 1703123456789
}
```

## Testing

The project includes JUnit 5 test stubs for:
- `parseTick(String)` method validation
- Price publishing functionality
- Error handling scenarios
- Rate limiting behavior

Run tests with:
```bash
mvn test
```

## Production Considerations

The following areas require production hardening (marked with `// REVIEW:` comments):

1. **CORS Configuration**: Configure specific origins instead of allowing all
2. **Provider Authentication**: Implement provider-specific authentication
3. **Error Handling**: Add comprehensive error handling and recovery
4. **Monitoring**: Add metrics and health checks
5. **Rate Limiting**: Implement more sophisticated rate limiting
6. **Message Validation**: Add input validation and sanitization
7. **Security**: Implement proper authentication and authorization
8. **Performance**: Optimize for high-frequency data processing
9. **Scalability**: Add clustering and load balancing support
10. **Test Coverage**: Implement comprehensive test suite

## Development Notes

- Uses simple STOMP broker for development
- Mock data generation runs on configurable interval
- Rate limiting prevents message flooding
- Automatic reconnection with exponential backoff
- Heartbeat mechanism maintains connection health
