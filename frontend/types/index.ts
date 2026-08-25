export interface Organization {
  id: string;
  name: string;
  slug?: string;
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

export type VoiceAgentStatus = 'ACTIVE' | 'INACTIVE';

export interface VoiceAgentConfig {
  systemPrompt: string;
  tone: string;
  temperature: number;
  model: string;
  maxDurationSeconds: number;
}

export interface VoiceAgent {
  id: string;
  organizationId: string;
  name: string;
  status: VoiceAgentStatus;
  phoneNumber?: string;
  config: VoiceAgentConfig;
  createdAt: string;
}
