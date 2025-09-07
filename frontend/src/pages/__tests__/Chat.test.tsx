import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Chat from '../pages/Chat';
import { ChatBox } from '../components/ChatBox';
import { sendChatMessage, getChatHistory } from '../lib/api';
import { ChatMessage } from '../types/chat';

// Mock the API functions
jest.mock('../lib/api', () => ({
  sendChatMessage: jest.fn(),
  getChatHistory: jest.fn(),
}));

// Mock the useStomp hook
jest.mock('../hooks/useStomp', () => ({
  useStomp: jest.fn(() => ({
    isConnected: true,
    isConnecting: false,
    error: null,
    reconnectAttempts: 0,
    connect: jest.fn(),
    disconnect: jest.fn(),
    reconnect: jest.fn(),
    sendMessage: jest.fn(),
    subscribe: jest.fn(),
    unsubscribe: jest.fn(),
  })),
}));

const mockSendChatMessage = sendChatMessage as jest.MockedFunction<typeof sendChatMessage>;
const mockGetChatHistory = getChatHistory as jest.MockedFunction<typeof getChatHistory>;

describe('Chat Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockGetChatHistory.mockResolvedValue([]);
  });

  it('should render chat interface', () => {
    render(<Chat />);
    
    expect(screen.getByText('Financial Assistant')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Ask me about financial topics...')).toBeInTheDocument();
    expect(screen.getByText('Send')).toBeInTheDocument();
  });

  it('should display disclaimer banner above bot answers', async () => {
    const mockMessages: ChatMessage[] = [
      {
        id: '1',
        userId: 1,
        query: 'What is compound interest?',
        answer: 'Compound interest is interest calculated on the initial principal and accumulated interest.',
        sources: ['Financial Education Guide'],
        timestamp: new Date(),
        isUser: false,
      },
    ];

    render(<ChatBox messages={mockMessages} isLoading={false} />);
    
    expect(screen.getByText('Not Financial Advice:')).toBeInTheDocument();
    expect(screen.getByText(/This information is for educational purposes only/)).toBeInTheDocument();
  });

  it('should not display disclaimer for user messages', () => {
    const mockMessages: ChatMessage[] = [
      {
        id: '1',
        userId: 1,
        query: 'What is compound interest?',
        answer: '',
        sources: [],
        timestamp: new Date(),
        isUser: true,
      },
    ];

    render(<ChatBox messages={mockMessages} isLoading={false} />);
    
    expect(screen.queryByText('Not Financial Advice:')).not.toBeInTheDocument();
  });

  it('should display sources in collapsible section', () => {
    const mockMessages: ChatMessage[] = [
      {
        id: '1',
        userId: 1,
        query: 'What is compound interest?',
        answer: 'Compound interest is interest calculated on the initial principal.',
        sources: ['Financial Education Guide', 'Investment Basics'],
        timestamp: new Date(),
        isUser: false,
      },
    ];

    render(<ChatBox messages={mockMessages} isLoading={false} />);
    
    expect(screen.getByText('Sources')).toBeInTheDocument();
    
    // Click to expand sources
    fireEvent.click(screen.getByText('Sources'));
    
    expect(screen.getByText('Financial Education Guide')).toBeInTheDocument();
    expect(screen.getByText('Investment Basics')).toBeInTheDocument();
  });

  it('should handle empty sources gracefully', () => {
    const mockMessages: ChatMessage[] = [
      {
        id: '1',
        userId: 1,
        query: 'What is compound interest?',
        answer: 'Compound interest is interest calculated on the initial principal.',
        sources: [],
        timestamp: new Date(),
        isUser: false,
      },
    ];

    render(<ChatBox messages={mockMessages} isLoading={false} />);
    
    expect(screen.queryByText('Sources')).not.toBeInTheDocument();
  });

  it('should send message when send button is clicked', async () => {
    mockSendChatMessage.mockResolvedValue({
      answer: 'Test response',
      sources: ['Test source'],
      sanitizedMessage: 'Test message',
    });

    render(<Chat />);
    
    const input = screen.getByPlaceholderText('Ask me about financial topics...');
    const sendButton = screen.getByText('Send');
    
    fireEvent.change(input, { target: { value: 'Test message' } });
    fireEvent.click(sendButton);
    
    await waitFor(() => {
      expect(mockSendChatMessage).toHaveBeenCalledWith(1, 'Test message');
    });
  });

  it('should send message when Enter key is pressed', async () => {
    mockSendChatMessage.mockResolvedValue({
      answer: 'Test response',
      sources: ['Test source'],
      sanitizedMessage: 'Test message',
    });

    render(<Chat />);
    
    const input = screen.getByPlaceholderText('Ask me about financial topics...');
    
    fireEvent.change(input, { target: { value: 'Test message' } });
    fireEvent.keyPress(input, { key: 'Enter', code: 'Enter' });
    
    await waitFor(() => {
      expect(mockSendChatMessage).toHaveBeenCalledWith(1, 'Test message');
    });
  });

  it('should not send message when Shift+Enter is pressed', async () => {
    render(<Chat />);
    
    const input = screen.getByPlaceholderText('Ask me about financial topics...');
    
    fireEvent.change(input, { target: { value: 'Test message' } });
    fireEvent.keyPress(input, { key: 'Enter', code: 'Enter', shiftKey: true });
    
    await waitFor(() => {
      expect(mockSendChatMessage).not.toHaveBeenCalled();
    });
  });

  it('should disable send button when loading', async () => {
    mockSendChatMessage.mockImplementation(() => new Promise(() => {})); // Never resolves
    
    render(<Chat />);
    
    const input = screen.getByPlaceholderText('Ask me about financial topics...');
    const sendButton = screen.getByText('Send');
    
    fireEvent.change(input, { target: { value: 'Test message' } });
    fireEvent.click(sendButton);
    
    await waitFor(() => {
      expect(sendButton).toBeDisabled();
      expect(screen.getByText('Sending...')).toBeInTheDocument();
    });
  });

  it('should handle API errors gracefully', async () => {
    mockSendChatMessage.mockRejectedValue(new Error('API Error'));
    
    render(<Chat />);
    
    const input = screen.getByPlaceholderText('Ask me about financial topics...');
    const sendButton = screen.getByText('Send');
    
    fireEvent.change(input, { target: { value: 'Test message' } });
    fireEvent.click(sendButton);
    
    await waitFor(() => {
      expect(screen.getByText(/Sorry, I encountered an error/)).toBeInTheDocument();
    });
  });

  it('should load chat history on mount', async () => {
    const mockHistory: ChatMessage[] = [
      {
        id: '1',
        userId: 1,
        query: 'Previous question',
        answer: 'Previous answer',
        sources: ['Previous source'],
        timestamp: new Date(),
        isUser: false,
      },
    ];

    mockGetChatHistory.mockResolvedValue(mockHistory);
    
    render(<Chat />);
    
    await waitFor(() => {
      expect(mockGetChatHistory).toHaveBeenCalledWith(1);
    });
  });

  it('should display loading state', () => {
    render(<ChatBox messages={[]} isLoading={true} />);
    
    expect(screen.getByText('Thinking...')).toBeInTheDocument();
  });

  it('should display empty state when no messages', () => {
    render(<ChatBox messages={[]} isLoading={false} />);
    
    expect(screen.getByText('Welcome to Financial Assistant')).toBeInTheDocument();
    expect(screen.getByText(/Ask me anything about financial topics/)).toBeInTheDocument();
  });
});
