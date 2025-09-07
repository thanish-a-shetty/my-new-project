import React, { useState, useRef, useEffect } from 'react';
import { useStomp } from '../hooks/useStomp';
import { ChatBox } from '../components/ChatBox';
import { sendChatMessage, getChatHistory } from '../lib/api';
import { ChatMessage, ChatResponse } from '../types/chat';

const Chat: React.FC = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [userId] = useState(1); // TODO: Get from auth context
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // STOMP configuration for real-time updates
  const stompConfig = {
    url: process.env.REACT_APP_STOMP_URL || 'ws://localhost:8081/market-ingest/ws',
    topic: '/topic/chat/updates', // TODO: Configure appropriate topic
    onMessage: (message: any) => {
      console.log('Received STOMP message:', message);
      // TODO: Handle real-time chat updates if needed
    },
    onConnect: () => {
      console.log('Connected to STOMP broker');
    },
    onDisconnect: () => {
      console.log('Disconnected from STOMP broker');
    },
    onError: (error: any) => {
      console.error('STOMP error:', error);
    },
    autoReconnect: true,
    maxReconnectAttempts: 5,
    reconnectDelay: 1000,
    useSockJS: true
  };

  const { isConnected, connect, disconnect } = useStomp(stompConfig);

  // Load chat history on component mount
  useEffect(() => {
    const loadChatHistory = async () => {
      try {
        const history = await getChatHistory(userId);
        setMessages(history);
      } catch (error) {
        console.error('Error loading chat history:', error);
      }
    };

    loadChatHistory();
  }, [userId]);

  // Connect to STOMP on mount
  useEffect(() => {
    connect();
    return () => {
      disconnect();
    };
  }, [connect, disconnect]);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = async () => {
    if (!inputMessage.trim() || isLoading) {
      return;
    }

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      userId,
      query: inputMessage.trim(),
      answer: '',
      sources: [],
      timestamp: new Date(),
      isUser: true
    };

    // Add user message immediately
    setMessages(prev => [...prev, userMessage]);
    setInputMessage('');
    setIsLoading(true);

    try {
      const response: ChatResponse = await sendChatMessage(userId, inputMessage.trim());
      
      const botMessage: ChatMessage = {
        id: (Date.now() + 1).toString(),
        userId,
        query: response.sanitizedMessage || inputMessage.trim(),
        answer: response.answer,
        sources: response.sources || [],
        timestamp: new Date(),
        isUser: false
      };

      // Replace loading state with bot response
      setMessages(prev => [...prev.slice(0, -1), botMessage]);
      
    } catch (error) {
      console.error('Error sending message:', error);
      
      const errorMessage: ChatMessage = {
        id: (Date.now() + 1).toString(),
        userId,
        query: inputMessage.trim(),
        answer: 'Sorry, I encountered an error while processing your message. Please try again.',
        sources: [],
        timestamp: new Date(),
        isUser: false
      };

      setMessages(prev => [...prev.slice(0, -1), errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className="flex flex-col h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-200 p-4">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-900">Financial Assistant</h1>
          <div className="flex items-center space-x-2">
            {/* STOMP Connection Status */}
            <div className={`w-3 h-3 rounded-full ${isConnected ? 'bg-green-500' : 'bg-red-500'}`} />
            <span className="text-sm text-gray-600">
              {isConnected ? 'Connected' : 'Disconnected'}
            </span>
          </div>
        </div>
      </div>

      {/* Chat Messages */}
      <div className="flex-1 overflow-hidden">
        <ChatBox messages={messages} isLoading={isLoading} />
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="bg-white border-t border-gray-200 p-4">
        <div className="flex space-x-3">
          <div className="flex-1">
            <textarea
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Ask me about financial topics..."
              className="w-full px-4 py-3 border border-gray-300 rounded-lg resize-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              rows={2}
              disabled={isLoading}
            />
          </div>
          <button
            onClick={handleSendMessage}
            disabled={!inputMessage.trim() || isLoading}
            className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
          >
            {isLoading ? (
              <div className="flex items-center">
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2" />
                Sending...
              </div>
            ) : (
              'Send'
            )}
          </button>
        </div>
        
        {/* Input Help Text */}
        <div className="mt-2 text-sm text-gray-500">
          Press Enter to send, Shift+Enter for new line
        </div>
      </div>
    </div>
  );
};

export default Chat;
