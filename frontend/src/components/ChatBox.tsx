import React, { useState } from 'react';
import { ChatMessage } from '../types/chat';

interface ChatBoxProps {
  messages: ChatMessage[];
  isLoading: boolean;
}

interface SourcesProps {
  sources: string[];
}

const Sources: React.FC<SourcesProps> = ({ sources }) => {
  const [isExpanded, setIsExpanded] = useState(false);

  if (!sources || sources.length === 0) {
    return null;
  }

  return (
    <div className="mt-3 border-t border-gray-200 pt-3">
      <button
        onClick={() => setIsExpanded(!isExpanded)}
        className="flex items-center text-sm text-gray-600 hover:text-gray-800 transition-colors"
      >
        <span className="mr-1">Sources</span>
        <svg
          className={`w-4 h-4 transform transition-transform ${isExpanded ? 'rotate-180' : ''}`}
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </button>
      
      {isExpanded && (
        <div className="mt-2 space-y-1">
          {sources.map((source, index) => (
            <div
              key={index}
              className="text-sm text-gray-600 bg-gray-50 rounded px-3 py-2"
            >
              {source}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

const DisclaimerBanner: React.FC = () => {
  return (
    <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-4">
      <div className="flex">
        <div className="flex-shrink-0">
          <svg
            className="h-5 w-5 text-yellow-400"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
              clipRule="evenodd"
            />
          </svg>
        </div>
        <div className="ml-3">
          <p className="text-sm text-yellow-700">
            <strong>Not Financial Advice:</strong> This information is for educational purposes only and should not be considered as financial advice. Please consult with a licensed financial advisor for personalized recommendations.
          </p>
        </div>
      </div>
    </div>
  );
};

const ChatMessage: React.FC<{ message: ChatMessage }> = ({ message }) => {
  return (
    <div className={`flex ${message.isUser ? 'justify-end' : 'justify-start'} mb-4`}>
      <div
        className={`max-w-3xl px-4 py-3 rounded-lg ${
          message.isUser
            ? 'bg-blue-600 text-white'
            : 'bg-white border border-gray-200'
        }`}
      >
        <div className="text-sm font-medium mb-1">
          {message.isUser ? 'You' : 'Financial Assistant'}
        </div>
        
        <div className="text-sm">
          {message.isUser ? message.query : message.answer}
        </div>
        
        {!message.isUser && (
          <>
            <DisclaimerBanner />
            <Sources sources={message.sources} />
          </>
        )}
        
        <div className={`text-xs mt-2 ${
          message.isUser ? 'text-blue-100' : 'text-gray-500'
        }`}>
          {message.timestamp.toLocaleTimeString()}
        </div>
      </div>
    </div>
  );
};

const LoadingMessage: React.FC = () => {
  return (
    <div className="flex justify-start mb-4">
      <div className="max-w-3xl px-4 py-3 rounded-lg bg-white border border-gray-200">
        <div className="text-sm font-medium mb-1">Financial Assistant</div>
        <div className="flex items-center space-x-2">
          <div className="flex space-x-1">
            <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" />
            <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }} />
            <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }} />
          </div>
          <span className="text-sm text-gray-500">Thinking...</span>
        </div>
      </div>
    </div>
  );
};

export const ChatBox: React.FC<ChatBoxProps> = ({ messages, isLoading }) => {
  return (
    <div className="flex-1 overflow-y-auto p-4 space-y-4">
      {messages.length === 0 && !isLoading && (
        <div className="text-center text-gray-500 mt-8">
          <div className="text-6xl mb-4">💬</div>
          <h3 className="text-lg font-medium mb-2">Welcome to Financial Assistant</h3>
          <p className="text-sm">
            Ask me anything about financial topics, investments, or personal finance.
          </p>
        </div>
      )}
      
      {messages.map((message) => (
        <ChatMessage key={message.id} message={message} />
      ))}
      
      {isLoading && <LoadingMessage />}
    </div>
  );
};
