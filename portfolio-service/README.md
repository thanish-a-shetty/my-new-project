# Portfolio Service

A Spring Boot service for portfolio management with P&L calculation, CRUD operations for holdings, and real-time portfolio valuation.

## Features

- CRUD operations for portfolio holdings
- Real-time P&L calculation (unrealized and realized)
- Portfolio valuation with allocation percentages
- Price cache integration for current market prices
- RESTful API endpoints
- H2 database for development

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Web
- H2 Database
- Maven

## API Endpoints

### Portfolio Valuation
- `GET /api/portfolio/{userId}/valuation` - Get complete portfolio valuation

### Holdings Management
- `GET /api/portfolio/{userId}/holdings` - Get all holdings for a user
- `GET /api/portfolio/{userId}/holdings/{holdingId}` - Get specific holding
- `POST /api/portfolio/{userId}/holdings` - Create new holding
- `PUT /api/portfolio/{userId}/holdings/{holdingId}` - Update holding
- `DELETE /api/portfolio/{userId}/holdings/{holdingId}` - Delete holding

### P&L Operations
- `GET /api/portfolio/{userId}/holdings/{holdingId}/pnl` - Get P&L for specific holding
- `POST /api/portfolio/{userId}/holdings/{holdingId}/realized-pnl` - Add realized P&L

## P&L Calculation

### Unrealized P&L Formula
```
unrealizedPnl = (currentPrice - averagePrice) * quantity
```

### Realized P&L
- Stored manually for sells and other realized gains/losses
- Can be added via API endpoint

### Total P&L
```
totalPnl = unrealizedPnl + realizedPnl
```

## Data Models

### Holding Entity
```java
{
  "id": 1,
  "userId": 100,
  "symbol": "AAPL",
  "quantity": 10.0,
  "averagePrice": 150.0,
  "realizedPnl": 50.0,
  "createdAt": "2023-12-21T10:00:00",
  "updatedAt": "2023-12-21T10:00:00"
}
```

### Portfolio Valuation Response
```json
{
  "userId": 100,
  "totalValue": 15000.0,
  "totalUnrealizedPnl": 500.0,
  "totalRealizedPnl": 200.0,
  "holdings": [
    {
      "holdingId": 1,
      "symbol": "AAPL",
      "quantity": 10.0,
      "averagePrice": 150.0,
      "currentPrice": 155.0,
      "currentValue": 1550.0,
      "unrealizedPnl": 50.0,
      "realizedPnl": 0.0,
      "allocationPercentage": 10.33
    }
  ]
}
```

### P&L Calculation Response
```json
{
  "holdingId": 1,
  "symbol": "AAPL",
  "quantity": 10.0,
  "averagePrice": 150.0,
  "currentPrice": 155.0,
  "unrealizedPnl": 50.0,
  "realizedPnl": 0.0,
  "totalPnl": 50.0,
  "currentValue": 1550.0
}
```

## Configuration

### Environment Variables
- `SPRING_DATASOURCE_URL` - Database URL (default: H2 in-memory)
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

### Application Properties
```yaml
portfolio:
  cache:
    enabled: true
    type: memory # REVIEW: caching vs direct lookup - can be changed to redis or database
    ttl: 300000 # 5 minutes
  pnl:
    calculation:
      precision: 2
    validation:
      enabled: true
      max-quantity: 1000000
      max-price: 1000000

price-cache:
  enabled: true
  type: memory # REVIEW: caching vs direct lookup - implement Redis/DB cache interface
  mock-prices:
    enabled: true
    symbols:
      - AAPL
      - GOOGL
      - MSFT
      - TSLA
      - AMZN
      - BTC-USD
      - ETH-USD
```

## Running the Application

1. Configure environment variables
2. Run with Maven:
   ```bash
   mvn spring-boot:run
   ```
3. Access H2 console at: http://localhost:8082/portfolio-service/h2-console

## Price Cache Integration

The service integrates with a `PriceCacheService` interface that can be implemented with different backends:

- **Memory Cache** (default): In-memory price storage
- **Redis Cache** (TODO): Redis-based caching
- **Database Cache** (TODO): Database-based caching

### Price Cache Interface
```java
public interface PriceCacheService {
    Double getCurrentPrice(String symbol);
    void updatePrice(String symbol, Double price);
    void removePrice(String symbol);
    void clearCache();
}
```

## Testing

The project includes JUnit 5 test stubs for:
- P&L calculation edge cases (null price, zero quantity)
- Portfolio service operations
- Price cache functionality
- Error handling scenarios

Run tests with:
```bash
mvn test
```

## Production Considerations

The following areas require production hardening (marked with `// REVIEW:` comments):

1. **CORS Configuration**: Configure specific origins instead of allowing all
2. **Price Cache Implementation**: Implement Redis or database cache
3. **Error Handling**: Add comprehensive error handling and logging
4. **Validation**: Implement input validation and sanitization
5. **Security**: Add authentication and authorization
6. **Performance**: Optimize for large portfolios
7. **Monitoring**: Add metrics and health checks
8. **Data Consistency**: Implement transaction management
9. **Scalability**: Add clustering and load balancing support
10. **Test Coverage**: Implement comprehensive test suite

## Development Notes

- Uses H2 in-memory database for development
- Mock prices provided for common symbols
- P&L calculations handle edge cases gracefully
- Price cache can be swapped for different implementations
- Allocation percentages calculated dynamically
- Realized P&L stored manually for simplicity
