# SIP Service

A Spring Boot service for Systematic Investment Plan (SIP) scheduling, processing, and notification management.

## Features

- Scheduled SIP processing with configurable cron expressions
- FCM push notifications for SIP reminders
- Email notifications via SMTP
- Comprehensive audit logging
- SIP management with multiple frequencies
- Error handling and retry mechanisms

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Mail
- Spring Scheduling
- H2 Database
- Maven

## Scheduling Configuration

### Default Schedule
The service runs daily at 9:00 AM UTC using the cron expression:
```java
@Scheduled(cron = "0 0 9 * * ?")
```

### Cron Expression Breakdown
- `"0 0 9 * * ?"` = Every day at 9:00 AM
- `"0 0 9 * * MON-FRI"` = Weekdays at 9:00 AM
- `"0 0 */6 * * ?"` = Every 6 hours
- `"0 30 8,12,16 * * ?"` = At 8:30, 12:30, and 4:30 PM daily
- `"0 0 0 1 * ?"` = First day of every month at midnight

### Cron Format
```
second minute hour day month day-of-week
```
- **second**: 0-59
- **minute**: 0-59  
- **hour**: 0-23
- **day**: 1-31
- **month**: 1-12 or JAN-DEC
- **day-of-week**: 0-7 or SUN-SAT (0 and 7 both represent Sunday)

## SIP Processing Flow

1. **Find Due SIPs**: Query database for SIPs with `nextDueDate <= currentDate` and `status = ACTIVE`
2. **Create Audit Record**: Log SIP processing initiation
3. **Send Notifications**: 
   - FCM push notification
   - Email notification
4. **Update SIP**: Set `lastProcessedAt` and calculate `nextDueDate`
5. **Log Completion**: Create audit record for job completion

## SIP Frequencies

- **DAILY**: Process every day
- **WEEKLY**: Process every week
- **MONTHLY**: Process every month
- **QUARTERLY**: Process every 3 months
- **YEARLY**: Process every year

## Data Models

### SIP Entity
```java
{
  "id": 1,
  "userId": 100,
  "symbol": "AAPL",
  "amount": 1000.0,
  "frequency": "MONTHLY",
  "status": "ACTIVE",
  "nextDueDate": "2023-12-22T09:00:00",
  "lastProcessedAt": "2023-12-21T09:00:00",
  "createdAt": "2023-11-21T10:00:00",
  "description": "Monthly AAPL investment"
}
```

### Audit Record
```java
{
  "id": 1,
  "sipId": 1,
  "userId": 100,
  "auditType": "SIP_PROCESSING",
  "action": "PROCESS_SIP",
  "description": "SIP processing initiated for AAPL - Amount: ₹1000.00",
  "status": "IN_PROGRESS",
  "createdAt": "2023-12-21T09:00:00"
}
```

## Configuration

### Environment Variables
- `FCM_SERVER_KEY` - Firebase Cloud Messaging server key
- `SMTP_HOST` - SMTP server hostname
- `SMTP_PORT` - SMTP server port
- `SMTP_USER` - SMTP username
- `SMTP_PASS` - SMTP password
- `SPRING_DATASOURCE_URL` - Database URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

### Application Properties
```yaml
sip:
  scheduler:
    enabled: true
    cron: "0 0 9 * * ?" # Daily at 9:00 AM
    timezone: "UTC"
  processing:
    batch-size: 100
    retry-attempts: 3
    retry-delay: 5000

fcm:
  enabled: ${FCM_ENABLED:true}
  server-key: ${FCM_SERVER_KEY:}
  api-url: "https://fcm.googleapis.com/fcm/send"

notification:
  email:
    enabled: true
    from-address: "noreply@fintech.com"
  push:
    enabled: true
  retry:
    enabled: true
    max-attempts: 3
    delay: 5000
```

## Running the Application

1. Configure environment variables
2. Run with Maven:
   ```bash
   mvn spring-boot:run
   ```
3. Access H2 console at: http://localhost:8083/sip-service/h2-console

## Notification Types

### FCM Push Notification
```json
{
  "target": "100",
  "title": "SIP Investment Reminder",
  "body": "Your AAPL SIP of ₹1000.00 is due today",
  "data": {
    "sipId": "1",
    "symbol": "AAPL",
    "amount": "1000.0",
    "frequency": "MONTHLY",
    "type": "SIP_REMINDER"
  }
}
```

### Email Notification
```
Subject: SIP Investment Reminder - AAPL

Dear Investor,

This is a reminder that your Systematic Investment Plan (SIP) is due today.

SIP Details:
- Symbol: AAPL
- Amount: ₹1000.00
- Frequency: MONTHLY
- Next Due Date: 2023-12-22T09:00:00

Please ensure you have sufficient funds in your account for the SIP investment.

Note: This is an automated reminder. Please do not reply to this email.

Best regards,
Fintech Investment Team
```

## Testing

The project includes JUnit 5 integration test stubs for:
- SIP processing with mocked repository
- Error handling scenarios
- Notification service integration
- Audit logging verification
- Concurrent access testing

Run tests with:
```bash
mvn test
```

## Production Considerations

The following areas require production hardening (marked with `// REVIEW:` comments):

1. **FCM Integration**: Implement actual FCM API integration
2. **Email Configuration**: Configure proper from address and SMTP settings
3. **Error Handling**: Add comprehensive error handling and recovery
4. **Monitoring**: Add metrics and health checks
5. **Security**: Implement proper authentication and authorization
6. **Performance**: Optimize for large-scale SIP processing
7. **Scalability**: Add clustering and load balancing support
8. **Data Consistency**: Implement transaction management
9. **Retry Logic**: Implement sophisticated retry mechanisms
10. **Test Coverage**: Implement comprehensive test suite

## Development Notes

- Uses H2 in-memory database for development
- FCM integration is stubbed for development
- Email service includes placeholder SMTP configuration
- Audit logging provides comprehensive tracking
- Scheduling can be easily modified via cron expressions
- Notifications are sent but trades are not executed
- Error handling ensures job continues despite individual failures
