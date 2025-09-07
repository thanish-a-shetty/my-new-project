import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

// API Configuration
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8084/chatbot-service';

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
apiClient.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token = localStorage.getItem('jwt_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('jwt_token');
      // TODO: Redirect to login page
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Chat API functions
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

// Send chat message
export const sendChatMessage = async (userId: number, message: string): Promise<ChatResponse> => {
  try {
    const response = await apiClient.post<ChatResponse>('/api/chat/message', {
      userId,
      message
    });
    return response.data;
  } catch (error) {
    console.error('Error sending chat message:', error);
    throw error;
  }
};

// Get chat history
export const getChatHistory = async (userId: number): Promise<ChatLog[]> => {
  try {
    const response = await apiClient.get<ChatLog[]>(`/api/chat/history/${userId}`);
    return response.data;
  } catch (error) {
    console.error('Error getting chat history:', error);
    throw error;
  }
};

// Get chat statistics
export const getChatStats = async (userId: number): Promise<ChatStats> => {
  try {
    const response = await apiClient.get<ChatStats>(`/api/chat/stats/${userId}`);
    return response.data;
  } catch (error) {
    console.error('Error getting chat stats:', error);
    throw error;
  }
};

// Health check
export const healthCheck = async (): Promise<{ status: string; service: string; timestamp: string }> => {
  try {
    const response = await apiClient.get('/api/chat/health');
    return response.data;
  } catch (error) {
    console.error('Error checking health:', error);
    throw error;
  }
};

// Auth API functions (if needed)
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
  };
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

// Login
export const login = async (email: string, password: string): Promise<LoginResponse> => {
  try {
    const response = await apiClient.post<LoginResponse>('/api/auth/login', {
      email,
      password
    });
    
    // Store JWT token
    if (response.data.token) {
      localStorage.setItem('jwt_token', response.data.token);
    }
    
    return response.data;
  } catch (error) {
    console.error('Error logging in:', error);
    throw error;
  }
};

// Signup
export const signup = async (userData: SignupRequest): Promise<SignupResponse> => {
  try {
    const response = await apiClient.post<SignupResponse>('/api/auth/signup', userData);
    return response.data;
  } catch (error) {
    console.error('Error signing up:', error);
    throw error;
  }
};

// Logout
export const logout = (): void => {
  localStorage.removeItem('jwt_token');
  // TODO: Redirect to login page
  window.location.href = '/login';
};

// Utility functions
export const isAuthenticated = (): boolean => {
  const token = localStorage.getItem('jwt_token');
  return !!token;
};

export const getToken = (): string | null => {
  return localStorage.getItem('jwt_token');
};

export const setToken = (token: string): void => {
  localStorage.setItem('jwt_token', token);
};

export const removeToken = (): void => {
  localStorage.removeItem('jwt_token');
};

// Export the configured axios instance for custom requests
export default apiClient;
