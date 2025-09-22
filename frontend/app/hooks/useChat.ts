'use client';

import { useState, useCallback } from 'react';
import { Message, ChatConfig } from '@/app/types/chat';

export const useChat = (initialConfig: ChatConfig = { model: 'llama3.2:3b' }) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [config, setConfig] = useState<ChatConfig>(initialConfig);
  const [error, setError] = useState<string | null>(null);

  const addMessage = useCallback((message: Omit<Message, 'id' | 'timestamp'>) => {
    const generateId = () => {
      if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
        return crypto.randomUUID();
      }
      return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
    };
    const newMessage: Message = {
      ...message,
      id: generateId(),
      timestamp: new Date(),
    };
    setMessages(prev => [...prev, newMessage]);
    return newMessage;
  }, []);

  const sendMessage = useCallback(async (content: string) => {
    if (!content.trim() || isLoading) return;

    addMessage({ role: 'user', content });
    setIsLoading(true);
    setError(null);

    try {
      const assistantMessage = addMessage({ role: 'assistant', content: '' });
      console.log('model: ' + config.model + ', content: '+ content);
      const response = await fetch('http://localhost:8080/api/ai/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        
        body: JSON.stringify({ message: content, model: config.model }),
      });

      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      if (!response.body) throw new Error('No response body');

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let accumulatedContent = '';
      let shouldStop = false;

      while (true) {
        const {value } = await reader.read();
        const chunk = decoder.decode(value, { stream: true });
        if (!chunk) continue;
        let data = chunk.trim();
        if (chunk.startsWith('data:')) {
          data = chunk.slice(5).trim();
          if (!chunk) continue;
        }

        try {
          const payload = JSON.parse(data);
          const done = payload?.done;
          if (done) { shouldStop = true; break; }
          const token = payload?.message?.content ?? '';
          if (token) {
            accumulatedContent += token;
            setMessages(prev => prev.map(msg => 
              msg.id === assistantMessage.id 
                ? { ...msg, content: accumulatedContent }
                : msg
            ));
          }
          if (payload?.done === true) { shouldStop = true; break; }
        } catch {
          // Fallback: treat as raw token text
          accumulatedContent += data;
          setMessages(prev => prev.map(msg => 
            msg.id === assistantMessage.id 
              ? { ...msg, content: accumulatedContent }
              : msg
          ));
        }
        if (shouldStop) break;
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      setMessages(prev => prev.slice(0, -1));
    } finally {
      setIsLoading(false);
    }
  }, [addMessage, isLoading, config.model]);

  const clearMessages = useCallback(() => {
    setMessages([]);
    setError(null);
  }, []);


  
  return { messages, isLoading, error, config, setConfig, sendMessage, clearMessages };
};