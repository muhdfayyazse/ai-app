'use client';

import { useState, useRef, useEffect } from 'react';
import { useRagChat } from '@/app/hooks/useRagChat';
import { MessageBubble } from './common/MessageBubble';
import { ChatInput } from './common/ChatInput';
import { DocumentUpload } from './common/DocumentUpload';

export const RagInterface: React.FC = () => {
  const {
    messages,
    isLoading,
    error,
    config,
    setConfig,
    documents,
    sendMessage,
    uploadDocument,
    deleteDocument,
    clearMessages
  } = useRagChat();

  const [input, setInput] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSubmit = () => {
    if (input.trim() && documents.length > 0) {
      sendMessage(input);
      setInput('');
    }
  };

  const handleDeleteDocument = async (id: number) => {
    await deleteDocument(id);
  };

  return (
    <div className="h-full flex">
      {/* Sidebar - Document Management */}
      <div className="w-80 border-r p-4 flex flex-col bg-white">
        <h3 className="font-semibold mb-4 text-gray-900">Document Management</h3>
        <DocumentUpload
          onUpload={uploadDocument}
          documents={documents}
          onDelete={handleDeleteDocument}
          disabled={isLoading}
        />
        
        {/* Configuration */}
        <div className="mt-6 space-y-3">
          <div>
            <label className="block text-sm font-medium mb-1 text-gray-700">Max Results</label>
            <input
              type="number"
              value={config.maxResults}
              onChange={(e) => setConfig({ ...config, maxResults: parseInt(e.target.value) || 5 })}
              className="w-full p-2 border rounded text-sm focus:ring-2 focus:ring-blue-500"
              min="1"
              max="10"
              disabled={isLoading}
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1 text-gray-700">Model</label>
            <select
              value={config.model}
              onChange={(e) => setConfig({ ...config, model: e.target.value })}
              className="w-full p-2 border rounded text-sm focus:ring-2 focus:ring-blue-500"
              disabled={isLoading}
            >
              <option value="llama3.2:3b">Llama 3.2 3B</option>
              <option value="llama2">Llama 2</option>
              <option value="mistral">Mistral</option>
            </select>
          </div>
        </div>
      </div>

      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col">
        {/* Header */}
        <div className="border-b p-4 bg-white">
          <div className="flex justify-between items-center">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">
                RAG Chat {documents.length > 0 && `(${documents.length} documents)`}
              </h2>
              <p className="text-sm text-gray-600">
                {documents.length === 0 
                  ? 'Upload documents to enable document-based chat'
                  : 'Ask questions about your uploaded documents'
                }
              </p>
            </div>
            <button
              onClick={clearMessages}
              disabled={isLoading}
              className="px-3 py-2 bg-gray-500 text-white rounded text-sm hover:bg-gray-600 disabled:opacity-50 transition-colors"
            >
              Clear Chat
            </button>
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
              <div className="text-lg font-semibold mb-2">
                {documents.length === 0 ? 'Upload documents to get started' : 'Ask questions about your documents'}
              </div>
              <div className="text-sm">
                {documents.length === 0 
                  ? 'Upload PDF, DOCX, or TXT files to enable document-based conversations'
                  : 'The AI will search through your documents to provide accurate answers'
                }
              </div>
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
                  <span>Searching documents and generating answer...</span>
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
            disabled={isLoading || documents.length === 0}
            placeholder={
              documents.length === 0 
                ? "Upload documents to enable chat..." 
                : "Ask a question about your documents..."
            }
          />
        </div>
      </div>
    </div>
  );
};