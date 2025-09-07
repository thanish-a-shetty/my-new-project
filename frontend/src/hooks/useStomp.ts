import { useEffect, useRef, useState, useCallback } from 'react';

// STOMP types
export interface StompConfig {
  url: string;
  topic: string;
  onMessage: (message: any) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: any) => void;
  autoReconnect?: boolean;
  maxReconnectAttempts?: number;
  reconnectDelay?: number;
  useSockJS?: boolean;
}

export interface StompState {
  isConnected: boolean;
  isConnecting: boolean;
  error: string | null;
  reconnectAttempts: number;
}

export interface StompMessage {
  body: string;
  headers: Record<string, any>;
  command: string;
}

// Reconnect configuration
const DEFAULT_RECONNECT_DELAY = 1000; // 1 second
const MAX_RECONNECT_DELAY = 30000; // 30 seconds
const RECONNECT_MULTIPLIER = 2;

export const useStomp = (config: StompConfig) => {
  const {
    url,
    topic,
    onMessage,
    onConnect,
    onDisconnect,
    onError,
    autoReconnect = true,
    maxReconnectAttempts = 10,
    reconnectDelay = DEFAULT_RECONNECT_DELAY,
    useSockJS = true
  } = config;

  const [state, setState] = useState<StompState>({
    isConnected: false,
    isConnecting: false,
    error: null,
    reconnectAttempts: 0
  });

  const stompClientRef = useRef<any>(null);
  const subscriptionRef = useRef<any>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const reconnectAttemptsRef = useRef(0);

  // Calculate exponential backoff delay
  const calculateReconnectDelay = useCallback((attempt: number): number => {
    const delay = reconnectDelay * Math.pow(RECONNECT_MULTIPLIER, attempt);
    return Math.min(delay, MAX_RECONNECT_DELAY);
  }, [reconnectDelay]);

  // Connect to STOMP broker
  const connect = useCallback(async () => {
    if (state.isConnected || state.isConnecting) {
      return;
    }

    setState(prev => ({ ...prev, isConnecting: true, error: null }));

    try {
      let client: any;

      if (useSockJS) {
        // Use SockJS + STOMP
        const SockJS = (await import('sockjs-client')).default;
        const Stomp = (await import('@stomp/stompjs')).default;
        
        const socket = new SockJS(url);
        client = new Stomp.Client({
          webSocketFactory: () => socket,
          debug: (str) => console.log('STOMP Debug:', str),
          reconnectDelay: 0, // We handle reconnection manually
        });
      } else {
        // Use native WebSocket + STOMP
        const Stomp = (await import('@stomp/stompjs')).default;
        
        client = new Stomp.Client({
          brokerURL: url,
          debug: (str) => console.log('STOMP Debug:', str),
          reconnectDelay: 0, // We handle reconnection manually
        });
      }

      // Set up event handlers
      client.onConnect = (frame: any) => {
        console.log('STOMP Connected:', frame);
        
        setState(prev => ({
          ...prev,
          isConnected: true,
          isConnecting: false,
          error: null,
          reconnectAttempts: 0
        }));

        reconnectAttemptsRef.current = 0;

        // Subscribe to topic
        if (topic) {
          subscriptionRef.current = client.subscribe(topic, (message: StompMessage) => {
            try {
              const parsedMessage = JSON.parse(message.body);
              onMessage(parsedMessage);
            } catch (error) {
              console.error('Error parsing STOMP message:', error);
              onMessage(message.body);
            }
          });
        }

        onConnect?.();
      };

      client.onStompError = (frame: any) => {
        console.error('STOMP Error:', frame);
        setState(prev => ({
          ...prev,
          isConnected: false,
          isConnecting: false,
          error: frame.headers?.message || 'STOMP connection error'
        }));
        onError?.(frame);
      };

      client.onWebSocketError = (error: any) => {
        console.error('WebSocket Error:', error);
        setState(prev => ({
          ...prev,
          isConnected: false,
          isConnecting: false,
          error: 'WebSocket connection error'
        }));
        onError?.(error);
      };

      client.onDisconnect = () => {
        console.log('STOMP Disconnected');
        setState(prev => ({
          ...prev,
          isConnected: false,
          isConnecting: false
        }));
        onDisconnect?.();
      };

      // Store client reference
      stompClientRef.current = client;

      // Connect
      client.activate();

    } catch (error) {
      console.error('Error connecting to STOMP:', error);
      setState(prev => ({
        ...prev,
        isConnecting: false,
        error: 'Failed to connect to STOMP broker'
      }));
      onError?.(error);
    }
  }, [url, topic, onMessage, onConnect, onDisconnect, onError, useSockJS, state.isConnected, state.isConnecting]);

  // Disconnect from STOMP broker
  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe();
      subscriptionRef.current = null;
    }

    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
      stompClientRef.current = null;
    }

    setState(prev => ({
      ...prev,
      isConnected: false,
      isConnecting: false,
      error: null
    }));
  }, []);

  // Reconnect with exponential backoff
  const reconnect = useCallback(() => {
    if (!autoReconnect) {
      return;
    }

    if (reconnectAttemptsRef.current >= maxReconnectAttempts) {
      setState(prev => ({
        ...prev,
        error: `Max reconnection attempts (${maxReconnectAttempts}) reached`
      }));
      return;
    }

    const delay = calculateReconnectDelay(reconnectAttemptsRef.current);
    reconnectAttemptsRef.current++;

    console.log(`Reconnecting in ${delay}ms (attempt ${reconnectAttemptsRef.current})`);

    reconnectTimeoutRef.current = setTimeout(() => {
      setState(prev => ({
        ...prev,
        reconnectAttempts: reconnectAttemptsRef.current
      }));
      connect();
    }, delay);
  }, [autoReconnect, maxReconnectAttempts, calculateReconnectDelay, connect]);

  // Send message to STOMP broker
  const sendMessage = useCallback((destination: string, message: any) => {
    if (!stompClientRef.current || !state.isConnected) {
      console.error('STOMP client not connected');
      return false;
    }

    try {
      stompClientRef.current.publish({
        destination,
        body: JSON.stringify(message)
      });
      return true;
    } catch (error) {
      console.error('Error sending STOMP message:', error);
      return false;
    }
  }, [state.isConnected]);

  // Subscribe to additional topic
  const subscribe = useCallback((newTopic: string, callback: (message: any) => void) => {
    if (!stompClientRef.current || !state.isConnected) {
      console.error('STOMP client not connected');
      return null;
    }

    try {
      return stompClientRef.current.subscribe(newTopic, (message: StompMessage) => {
        try {
          const parsedMessage = JSON.parse(message.body);
          callback(parsedMessage);
        } catch (error) {
          console.error('Error parsing STOMP message:', error);
          callback(message.body);
        }
      });
    } catch (error) {
      console.error('Error subscribing to topic:', error);
      return null;
    }
  }, [state.isConnected]);

  // Unsubscribe from topic
  const unsubscribe = useCallback((subscription: any) => {
    if (subscription) {
      subscription.unsubscribe();
    }
  }, []);

  // Auto-reconnect on connection loss
  useEffect(() => {
    if (!state.isConnected && !state.isConnecting && autoReconnect && reconnectAttemptsRef.current < maxReconnectAttempts) {
      reconnect();
    }
  }, [state.isConnected, state.isConnecting, autoReconnect, maxReconnectAttempts, reconnect]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      disconnect();
    };
  }, [disconnect]);

  return {
    ...state,
    connect,
    disconnect,
    reconnect,
    sendMessage,
    subscribe,
    unsubscribe
  };
};
