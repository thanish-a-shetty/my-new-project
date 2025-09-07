# Fintech Auth Service

A Spring Boot authentication service with email verification functionality.

## Features

- User registration with email verification
- JWT-based authentication
- Email verification using Spring Mail
- Password encryption with BCrypt
- H2 database for development
- Comprehensive test stubs

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- Spring Mail
- JWT (JSON Web Tokens)
- H2 Database
- Maven

## API Endpoints

### POST /api/auth/signup
Creates a new user account (disabled until verified).

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "message": "User created successfully. Please check your email for verification link.",
  "status": "success"
}
```

### POST /api/auth/login
Authenticates a user and returns JWT token (only for verified users).

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe"
  },
  "status": "success"
}
```

### GET /api/auth/verify?token={token}
Verifies user account using email verification token.

**Response:**
```json
{
  "message": "Account verified successfully! You can now log in.",
  "status": "success"
}
```

## Environment Variables

Configure the following environment variables:

- `SMTP_HOST` - SMTP server hostname
- `SMTP_PORT` - SMTP server port (default: 587)
- `SMTP_USER` - SMTP username
- `SMTP_PASS` - SMTP password
- `JWT_SECRET` - Secret key for JWT signing
- `SPRING_DATASOURCE_URL` - Database URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

## Running the Application

1. Clone the repository
2. Configure environment variables
3. Run with Maven:
   ```bash
   mvn spring-boot:run
   ```
4. Access H2 console at: http://localhost:8080/auth-service/h2-console

## Database Schema

### Users Table
- `id` - Primary key
- `email` - Unique email address
- `password` - Encrypted password
- `first_name` - User's first name
- `last_name` - User's last name
- `enabled` - Account verification status
- `created_at` - Account creation timestamp
- `updated_at` - Last update timestamp
- `last_login` - Last login timestamp

### Verification Tokens Table
- `id` - Primary key
- `token` - Unique verification token
- `user_id` - Foreign key to users table
- `expiry_date` - Token expiration timestamp
- `created_at` - Token creation timestamp
- `used` - Token usage status
- `used_at` - Token usage timestamp

## Testing

The project includes JUnit 5 test stubs for:
- Token expiry validation
- Verification flow testing
- Authentication flow testing

Run tests with:
```bash
mvn test
```

## Production Considerations

The following areas require production hardening (marked with `// REVIEW:` comments):

1. **Security Configuration**: Configure specific CORS origins instead of allowing all
2. **Email Configuration**: Set proper from address and SMTP credentials
3. **JWT Secret**: Ensure JWT secret has sufficient length (256+ bits)
4. **Database**: Replace H2 with production database (PostgreSQL, MySQL)
5. **Error Handling**: Implement comprehensive error handling and logging
6. **Rate Limiting**: Add rate limiting for authentication endpoints
7. **Password Policy**: Implement password strength requirements
8. **Token Cleanup**: Implement scheduled cleanup of expired tokens
9. **Monitoring**: Add health checks and metrics
10. **Test Coverage**: Implement comprehensive test suite

## Development Notes

- Uses H2 in-memory database for development
- Email service includes placeholder SMTP configuration
- JWT tokens expire in 24 hours by default
- Verification tokens expire in 24 hours
- Password encryption uses BCrypt with default strength
