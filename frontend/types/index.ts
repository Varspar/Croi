export interface Organization {
  id: string;
  name: string;
  slug?: string;
  createdAt: string;
}

export type ConversationStatus = 'OPEN' | 'CLOSED' | 'ESCALATED';

export interface Conversation {
  id: string;
  organizationId: string;
  customerName?: string;
  customerEmail?: string;
  status: ConversationStatus;
  isEscalated?: boolean;
  createdAt: string;
}

export type SenderType = 'CUSTOMER' | 'AI' | 'AGENT';

export interface MessageSource {
  documentId: string; chunkId: string; documentName: string; content?: string; relevanceScore: number;
}

export interface Message {
  id: string;
  conversationId: string;
  content: string;
  senderType: SenderType;
  sources?: MessageSource[];
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

export interface ContactRequest {
  firstName: string;
  lastName: string;
  companyName: string;
  email: string;
  phone?: string;
  message: string;
  recaptchaToken: string;
}

export interface Document {
  id: string; organizationId: string; title: string; fileName: string; fileType: string;
  status: 'PROCESSING' | 'READY' | 'FAILED'; fileSize: number; errorMessage?: string;
  chunkCount: number; embeddingCount: number; createdAt: string;
}

export interface WorkspaceConfiguration {
  systemPrompt: string; tone: 'FORMAL' | 'FRIENDLY' | 'PROFESSIONAL' | 'CASUAL'; embeddingModel: string;
  similarityThreshold: number; maxChunksInPrompt: number; temperature: number; maxTokens: number;
}

export interface DocumentCitation {
  documentId: string; title: string; citations: number;
}

export interface WorkspaceBranding {
  name: string;
  logo?: string;
  primaryColor?: string;
  description?: string;
}

export interface WorkspaceAnalytics {
  totalConversations: number;
  totalMessages: number;
  /** Share of AI replies that cited at least one document chunk. Null if no AI replies exist yet. */
  ragHitRate: number | null;
  /** Average AI reply latency in ms. Null if no timed replies exist yet. */
  avgResponseTimeMs: number | null;
  topDocuments: DocumentCitation[];
}
