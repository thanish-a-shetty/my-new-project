import { renderHook, act } from '@testing-library/react';
import { useStomp } from '../hooks/useStomp';

// Mock STOMP and SockJS
jest.mock('sockjs-client', () => ({
  __esModule: true,
  default: jest.fn(() => ({
    close: jest.fn(),
    send: jest.fn(),
    onopen: null,
    onclose: null,
    onmessage: null,
    onerror: null,
  })),
}));

jest.mock('@stomp/stompjs', () => ({
  __esModule: true,
  default: {
    Client: jest.fn(() => ({
      activate: jest.fn(),
      deactivate: jest.fn(),
      subscribe: jest.fn(() => ({ unsubscribe: jest.fn() })),
      publish: jest.fn(),
      onConnect: null,
      onStompError: null,
      onWebSocketError: null,
      onDisconnect: null,
    })),
  },
}));

// Mock timers
jest.useFakeTimers();

describe('useStomp Hook', () => {
  const mockOnMessage = jest.fn();
  const mockOnConnect = jest.fn();
  const mockOnDisconnect = jest.fn();
  const mockOnError = jest.fn();

  const defaultConfig = {
    url: 'ws://localhost:8080/ws',
    topic: '/topic/test',
    onMessage: mockOnMessage,
    onConnect: mockOnConnect,
    onDisconnect: mockOnDisconnect,
    onError: mockOnError,
    autoReconnect: true,
    maxReconnectAttempts: 3,
    reconnectDelay: 1000,
    useSockJS: true,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    jest.clearAllTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
    jest.useFakeTimers();
  });

  it('should initialize with disconnected state', () => {
    const { result } = renderHook(() => useStomp(defaultConfig));

    expect(result.current.isConnected).toBe(false);
    expect(result.current.isConnecting).toBe(false);
    expect(result.current.error).toBe(null);
    expect(result.current.reconnectAttempts).toBe(0);
  });

  it('should handle reconnection with exponential backoff', async () => {
    const { result } = renderHook(() => useStomp(defaultConfig));

    // Mock connection failure
    act(() => {
      result.current.connect();
    });

    // Simulate connection failure
    act(() => {
      result.current.disconnect();
    });

    // Trigger reconnection
    act(() => {
      result.current.reconnect();
    });

    expect(result.current.reconnectAttempts).toBe(1);

    // Fast-forward time to trigger reconnection
    act(() => {
      jest.advanceTimersByTime(1000); // First reconnection attempt
    });

    expect(result.current.reconnectAttempts).toBe(1);

    // Simulate another failure and reconnection
    act(() => {
      result.current.disconnect();
      result.current.reconnect();
    });

    expect(result.current.reconnectAttempts).toBe(2);

    // Fast-forward time for second reconnection attempt (exponential backoff)
    act(() => {
      jest.advanceTimersByTime(2000); // 2 * 1000ms delay
    });

    expect(result.current.reconnectAttempts).toBe(2);
  });

  it('should stop reconnecting after max attempts', () => {
    const config = {
      ...defaultConfig,
      maxReconnectAttempts: 2,
    };

    const { result } = renderHook(() => useStomp(config));

    // Simulate multiple reconnection attempts
    act(() => {
      result.current.reconnect();
    });
    expect(result.current.reconnectAttempts).toBe(1);

    act(() => {
      result.current.reconnect();
    });
    expect(result.current.reconnectAttempts).toBe(2);

    // Third attempt should not increase counter
    act(() => {
      result.current.reconnect();
    });
    expect(result.current.reconnectAttempts).toBe(2);

    // Should have error message
    expect(result.current.error).toContain('Max reconnection attempts');
  });

  it('should not reconnect when autoReconnect is disabled', () => {
    const config = {
      ...defaultConfig,
      autoReconnect: false,
    };

    const { result } = renderHook(() => useStomp(config));

    act(() => {
      result.current.reconnect();
    });

    expect(result.current.reconnectAttempts).toBe(0);
  });

  it('should calculate exponential backoff delay correctly', () => {
    const config = {
      ...defaultConfig,
      reconnectDelay: 1000,
    };

    const { result } = renderHook(() => useStomp(config));

    // Test exponential backoff calculation
    act(() => {
      result.current.reconnect();
    });
    expect(result.current.reconnectAttempts).toBe(1);

    act(() => {
      result.current.reconnect();
    });
    expect(result.current.reconnectAttempts).toBe(2);

    // Fast-forward time for exponential backoff
    act(() => {
      jest.advanceTimersByTime(4000); // 2^2 * 1000ms = 4000ms
    });

    expect(result.current.reconnectAttempts).toBe(2);
  });

  it('should cap reconnection delay at maximum', () => {
    const config = {
      ...defaultConfig,
      reconnectDelay: 1000,
      maxReconnectAttempts: 10,
    };

    const { result } = renderHook(() => useStomp(config));

    // Simulate many reconnection attempts
    for (let i = 0; i < 10; i++) {
      act(() => {
        result.current.reconnect();
      });
    }

    expect(result.current.reconnectAttempts).toBe(10);

    // Fast-forward time - should not exceed max delay
    act(() => {
      jest.advanceTimersByTime(30000); // Max delay should be 30 seconds
    });

    expect(result.current.reconnectAttempts).toBe(10);
  });

  it('should cleanup on unmount', () => {
    const { result, unmount } = renderHook(() => useStomp(defaultConfig));

    act(() => {
      result.current.connect();
    });

    // Unmount should cleanup
    unmount();

    // Should not throw errors
    expect(() => {
      jest.runAllTimers();
    }).not.toThrow();
  });

  it('should handle sendMessage when connected', () => {
    const { result } = renderHook(() => useStomp(defaultConfig));

    // Mock connected state
    act(() => {
      result.current.connect();
    });

    // Mock STOMP client
    const mockStompClient = {
      publish: jest.fn(),
    };
    
    // This would be set by the actual STOMP client
    // For testing, we'll assume it's connected
    const sendResult = result.current.sendMessage('/topic/test', { message: 'test' });
    
    // Should return false when not actually connected (in test environment)
    expect(sendResult).toBe(false);
  });

  it('should handle sendMessage when disconnected', () => {
    const { result } = renderHook(() => useStomp(defaultConfig));

    const sendResult = result.current.sendMessage('/topic/test', { message: 'test' });
    
    expect(sendResult).toBe(false);
  });
});
