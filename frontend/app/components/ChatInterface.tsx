'use client';

import { useState, useRef, useEffect } from 'react';
import { useChat } from '@/app/hooks/useChat';
import { MessageBubble } from './common/MessageBubble';
import { ChatInput } from './common/ChatInput';

export const ChatInterface: React.FC = () => {
  const {
    messages,
    isLoading,
    error,
    config,
    setConfig,
    sendMessage,
    clearMessages
  } = useChat();

  const [input, setInput] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSubmit = () => {
    if (input.trim()) {
      sendMessage(input);
      setInput('');
    }
  };

  return (
    <div className="h-full flex flex-col">
      {/* Header */}
      <div className="border-b p-4 bg-white">
        <div className="flex justify-between items-center">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">Simple Chat</h2>
            <p className="text-sm text-gray-600">Chat directly with the AI model</p>
          </div>
          <div className="flex gap-2">
            <select
              value={config.model}
              onChange={(e) => setConfig({ ...config, model: e.target.value })}
              className="p-2 border rounded text-sm focus:ring-2 focus:ring-blue-500"
              disabled={isLoading}
            >
              <option value="llama3.2:3b">Llama 3.2 3B</option>
              <option value="llama2">Llama 2</option>
              <option value="mistral">Mistral</option>
            </select>
            <button
              onClick={clearMessages}
              disabled={isLoading}
              className="px-3 py-2 bg-gray-500 text-white rounded text-sm hover:bg-gray-600 disabled:opacity-50 transition-colors"
            >
              Clear Chat
            </button>
          </div>
        </div>
      </div>

      {/* Error Display */}
      {error && (
        <div className="mx-4 mt-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded text-sm">
          <strong>Error:</strong> {error}
        </div>
      )}

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
        {messages.length === 0 ? (
          <div className="text-center text-gray-500 py-8">
            <div className="text-lg font-semibold mb-2">Start a conversation</div>
            <div className="text-sm">Ask anything and get AI-powered responses</div>
          </div>
        ) : (
          messages.map(message => (
            <MessageBubble key={message.id} message={message} />
          ))
        )}
        {isLoading && (
          <div className="flex justify-start">
            <div className="bg-gray-200 text-gray-800 rounded-lg rounded-bl-none p-3">
              <div className="flex items-center space-x-2 text-sm">
                <div className="flex space-x-1">
                  <div className="w-2 h-2 bg-gray-500 rounded-full loading-dot"></div>
                  <div className="w-2 h-2 bg-gray-500 rounded-full loading-dot"></div>
                  <div className="w-2 h-2 bg-gray-500 rounded-full loading-dot"></div>
                </div>
                <span>AI is thinking...</span>
              </div>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="border-t p-4 bg-white">
        <ChatInput
          value={input}
          onChange={setInput}
          onSubmit={handleSubmit}
          disabled={isLoading}
          placeholder="Type your message..."
        />
      </div>
    </div>
  );
};