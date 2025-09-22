export interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  sources?: string[];
}

export interface Document {
  id: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploadedAt: string;
}

export interface ChatTab {
  id: string;
  label: string;
  component: React.ComponentType<any>;
  icon?: string;
}

export interface ChatConfig {
  model: string;
  temperature?: number;
  maxTokens?: number;
}

export interface RagConfig extends ChatConfig {
  maxResults: number;
  similarityThreshold: number;
}