# Fintech Frontend

A React TypeScript frontend application for the Fintech platform with STOMP WebSocket integration, chat functionality, and comprehensive financial assistant features.

## Features

- **Chat Interface**: Real-time chat with financial assistant
- **STOMP Integration**: WebSocket connection with auto-reconnect and exponential backoff
- **PII Protection**: Client-side input sanitization and validation
- **Disclaimer Banner**: Prominent "Not Financial Advice" warning
- **Sources Display**: Collapsible sources section for each response
- **JWT Authentication**: Secure API communication with token management
- **Responsive Design**: Mobile-friendly interface with Tailwind CSS
- **TypeScript**: Full type safety and IntelliSense support

## Technology Stack

- **React 18** - UI framework
- **TypeScript** - Type safety
- **STOMP.js** - WebSocket messaging
- **SockJS** - WebSocket fallback
- **Axios** - HTTP client
- **Tailwind CSS** - Styling framework
- **Jest** - Testing framework
- **React Testing Library** - Component testing

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   └── ChatBox.tsx          # Chat message display with disclaimer
│   ├── hooks/
│   │   ├── useStomp.ts          # STOMP WebSocket hook
│   │   └── __tests__/
│   │       └── useStomp.test.ts # STOMP hook tests
│   ├── lib/
│   │   └── api.ts               # API client with JWT support
│   ├── pages/
│   │   ├── Chat.tsx             # Main chat page
│   │   └── __tests__/
│   │       └── Chat.test.tsx    # Chat component tests
│   ├── types/
│   │   └── chat.ts              # TypeScript type definitions
│   └── App.tsx                  # Main application component
├── package.json                 # Dependencies and scripts
└── README.md                    # This file
```

## Installation

1. **Install dependencies**:
   ```bash
   npm install
   ```

2. **Configure environment variables**:
   Create a `.env` file in the frontend directory:
   ```env
   REACT_APP_API_BASE_URL=http://localhost:8084/chatbot-service
   REACT_APP_STOMP_URL=ws://localhost:8081/market-ingest/ws
   REACT_APP_AUTH_SERVICE_URL=http://localhost:8080/auth-service
   ```

3. **Start development server**:
   ```bash
   npm start
   ```

4. **Run tests**:
   ```bash
   npm test
   ```

5. **Build for production**:
   ```bash
   npm run build
   ```

## Key Components

### useStomp Hook

A custom React hook for STOMP WebSocket connections with:

- **Auto-reconnect**: Exponential backoff strategy
- **Connection Management**: Connect, disconnect, subscribe, unsubscribe
- **Error Handling**: Comprehensive error handling and logging
- **Message Sending**: Publish messages to STOMP topics
- **SockJS Fallback**: Automatic fallback for older browsers

```typescript
const { isConnected, connect, disconnect, sendMessage } = useStomp({
  url: 'ws://localhost:8081/ws',
  topic: '/topic/chat/updates',
  onMessage: (message) => console.log(message),
  autoReconnect: true,
  maxReconnectAttempts: 5,
  reconnectDelay: 1000,
  useSockJS: true
});
```

### Chat Page

Main chat interface featuring:

- **Real-time Messaging**: Send and receive messages
- **STOMP Integration**: Live connection status indicator
- **Input Validation**: Prevent empty messages
- **Loading States**: Visual feedback during message processing
- **Error Handling**: Graceful error display
- **Auto-scroll**: Automatic scrolling to latest messages

### ChatBox Component

Message display component with:

- **Disclaimer Banner**: Prominent "Not Financial Advice" warning
- **Sources Section**: Collapsible sources under each bot response
- **Message Types**: Different styling for user vs bot messages
- **Loading Animation**: Typing indicator during processing
- **Empty State**: Welcome message when no messages

### API Client

Axios-based API client with:

- **JWT Authentication**: Automatic token management
- **Request Interceptors**: Add auth headers automatically
- **Response Interceptors**: Handle 401 errors and redirects
- **Error Handling**: Comprehensive error management
- **Type Safety**: Full TypeScript support

## STOMP Configuration

### Connection Options

- **URL**: WebSocket endpoint URL
- **Topic**: STOMP topic to subscribe to
- **Auto-reconnect**: Enable/disable automatic reconnection
- **Max Attempts**: Maximum reconnection attempts
- **Reconnect Delay**: Base delay for exponential backoff
- **SockJS**: Use SockJS fallback or native WebSocket

### Reconnection Strategy

The hook implements exponential backoff for reconnections:

1. **Initial Delay**: 1 second
2. **Multiplier**: 2x for each attempt
3. **Maximum Delay**: 30 seconds
4. **Max Attempts**: Configurable (default: 10)

## API Integration

### Chat Endpoints

- `POST /api/chat/message` - Send chat message
- `GET /api/chat/history/{userId}` - Get chat history
- `GET /api/chat/stats/{userId}` - Get chat statistics
- `GET /api/chat/health` - Health check

### Authentication

- **JWT Token**: Stored in localStorage
- **Auto-refresh**: Automatic token refresh
- **Logout**: Clear token and redirect

## Styling

### Tailwind CSS Classes

The application uses Tailwind CSS for styling with:

- **Responsive Design**: Mobile-first approach
- **Color Scheme**: Blue primary, gray secondary
- **Typography**: Clean, readable fonts
- **Spacing**: Consistent spacing system
- **Animations**: Smooth transitions and loading states

### TODO: Styling Improvements

- [ ] Add dark mode support
- [ ] Implement custom color themes
- [ ] Add more animation effects
- [ ] Improve mobile responsiveness
- [ ] Add accessibility features

## Testing

### Test Coverage

- **useStomp Hook**: Reconnection logic, error handling
- **Chat Component**: Message sending, disclaimer display
- **ChatBox Component**: Sources display, empty states
- **API Client**: Request/response handling

### Running Tests

```bash
# Run all tests
npm test

# Run tests with coverage
npm run test:coverage

# Run tests in watch mode
npm test -- --watch
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REACT_APP_API_BASE_URL` | Backend API base URL | `http://localhost:8084/chatbot-service` |
| `REACT_APP_STOMP_URL` | STOMP WebSocket URL | `ws://localhost:8081/market-ingest/ws` |
| `REACT_APP_AUTH_SERVICE_URL` | Auth service URL | `http://localhost:8080/auth-service` |

## Production Considerations

### Security

- [ ] Implement CSP headers
- [ ] Add input sanitization
- [ ] Secure JWT storage
- [ ] Add rate limiting

### Performance

- [ ] Implement code splitting
- [ ] Add service worker
- [ ] Optimize bundle size
- [ ] Add caching strategies

### Monitoring

- [ ] Add error tracking
- [ ] Implement analytics
- [ ] Add performance monitoring
- [ ] Set up logging

## Development Notes

- **Hot Reload**: Automatic reload on file changes
- **TypeScript**: Strict type checking enabled
- **ESLint**: Code quality and consistency
- **Prettier**: Code formatting
- **Proxy**: Development proxy for API calls

## Browser Support

- **Chrome**: Latest 2 versions
- **Firefox**: Latest 2 versions
- **Safari**: Latest 2 versions
- **Edge**: Latest 2 versions

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License.
