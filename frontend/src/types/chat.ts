// Chat types
export interface ChatMessage {
  id: string;
  userId: number;
  query: string;
  answer: string;
  sources: string[];
  timestamp: Date;
  isUser: boolean;
}

export interface ChatRequest {
  userId: number;
  message: string;
}

export interface ChatResponse {
  answer: string;
  sources: string[] | null;
  sanitizedMessage: string | null;
}

export interface ChatLog {
  id: number;
  userId: number;
  query: string;
  answer: string;
  sources: string;
  timestamp: string;
  responseTimeMs?: number;
  tokensUsed?: number;
}

export interface ChatStats {
  userId: number;
  totalMessages: number;
  totalQueryLength: number;
  totalAnswerLength: number;
  lastUpdated: string;
}

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

// Auth types
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
  status: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface SignupResponse {
  message: string;
  status: string;
}

// API types
export interface ApiError {
  message: string;
  status: number;
  code?: string;
}

export interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
}

// Component props types
export interface ChatBoxProps {
  messages: ChatMessage[];
  isLoading: boolean;
}

export interface SourcesProps {
  sources: string[];
}

export interface DisclaimerBannerProps {
  className?: string;
}

// Hook return types
export interface UseStompReturn {
  isConnected: boolean;
  isConnecting: boolean;
  error: string | null;
  reconnectAttempts: number;
  connect: () => Promise<void>;
  disconnect: () => void;
  reconnect: () => void;
  sendMessage: (destination: string, message: any) => boolean;
  subscribe: (topic: string, callback: (message: any) => void) => any;
  unsubscribe: (subscription: any) => void;
}

// Environment types
export interface EnvironmentConfig {
  REACT_APP_API_BASE_URL?: string;
  REACT_APP_STOMP_URL?: string;
  REACT_APP_AUTH_SERVICE_URL?: string;
  REACT_APP_CHATBOT_SERVICE_URL?: string;
  REACT_APP_MARKET_INGEST_URL?: string;
  REACT_APP_PORTFOLIO_SERVICE_URL?: string;
  REACT_APP_SIP_SERVICE_URL?: string;
}

// Utility types
export type LoadingState = 'idle' | 'loading' | 'success' | 'error';

export interface LoadingStateData<T> {
  state: LoadingState;
  data?: T;
  error?: string;
}

// Form types
export interface ChatFormData {
  message: string;
}

export interface LoginFormData {
  email: string;
  password: string;
}

export interface SignupFormData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

// Event types
export interface ChatEvent {
  type: 'message' | 'typing' | 'user_joined' | 'user_left';
  data: any;
  timestamp: Date;
}

export interface StompEvent {
  type: 'connect' | 'disconnect' | 'error' | 'message';
  data: any;
  timestamp: Date;
}
