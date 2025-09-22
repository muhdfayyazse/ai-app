'use client';

import { useState } from 'react';
import { ChatInterface } from '@/app/components/ChatInterface';
import { RagInterface } from '@/app/components/RagInterface';
import { TabNavigation } from '@/app/components/TabNavigation';
import { ChatTab } from '@/app/types/chat';

const tabs: ChatTab[] = [
  {
    id: 'simple-chat',
    label: 'Simple Chat',
    component: ChatInterface,
    icon: '💬'
  },
  {
    id: 'rag-chat',
    label: 'RAG Chat',
    component: RagInterface,
    icon: '📚'
  },
  // Add more tabs here in the future:
  // {
  //   id: 'image-chat',
  //   label: 'Image Chat',
  //   component: ImageChatInterface,
  //   icon: '🖼️'
  // },
  // {
  //   id: 'tools-chat',
  //   label: 'Tools Chat',
  //   component: ToolsChatInterface,
  //   icon: '🛠️'
  // }
];

export default function Home() {
  const [activeTab, setActiveTab] = useState<string>('simple-chat');

  const ActiveComponent = tabs.find(tab => tab.id === activeTab)?.component || ChatInterface;

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto bg-white rounded-lg shadow-sm min-h-screen flex flex-col">
        {/* Header */}
        <div className="border-b">
          <div className="px-6 py-4">
            <h1 className="text-2xl font-bold text-gray-900">AI Chat Application</h1>
            <p className="text-gray-600">Choose your chat mode below</p>
          </div>
          
          {/* Tab Navigation */}
          <TabNavigation
            tabs={tabs}
            activeTab={activeTab}
            onTabChange={setActiveTab}
          />
        </div>

        {/* Active Tab Content */}
        <div className="flex-1">
          <ActiveComponent />
        </div>
      </div>
    </div>
  );
}