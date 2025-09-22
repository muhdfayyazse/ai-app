'use client';

import { Message } from '@/app/types/chat';

interface MessageBubbleProps {
  message: Message;
}

export const MessageBubble: React.FC<MessageBubbleProps> = ({ message }) => {
  const isUser = message.role === 'user';
  
  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'} mb-4 message-bubble`}>
      <div className={`max-w-xs md:max-w-md lg:max-w-lg xl:max-w-xl rounded-lg p-3 ${
        isUser 
          ? 'bg-blue-500 text-white rounded-br-none' 
          : 'bg-gray-200 text-gray-800 rounded-bl-none'
      }`}>
        <div className="text-sm whitespace-pre-wrap">{message.content}</div>
        {message.sources && message.sources.length > 0 && (
          <div className="mt-2 text-xs opacity-75">
            Sources: {message.sources.join(', ')}
          </div>
        )}
        <div className="text-xs mt-1 opacity-60">
          {message.timestamp.toLocaleTimeString()}
        </div>
      </div>
    </div>
  );
};