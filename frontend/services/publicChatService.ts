import apiClient from '@/lib/api';
import type { ApiResponse, Conversation, Message, WorkspaceBranding } from '@/types';

/**
 * Calls the unauthenticated /public and /workspaces/{id}/branding endpoints used by
 * the guest chat widget (frontend/app/chat). No Authorization header is sent —
 * apiClient only attaches one when a token exists in localStorage, which it won't
 * for an anonymous visitor.
 */
export async function getWorkspaceBranding(workspaceId: string): Promise<WorkspaceBranding> {
  const { data } = await apiClient.get<ApiResponse<WorkspaceBranding>>(`/workspaces/${workspaceId}/branding`);
  return data.data;
}

export async function createGuestConversation(organizationId: string): Promise<Conversation> {
  const { data } = await apiClient.post<ApiResponse<Conversation>>('/public/conversations', { organizationId });
  return data.data;
}

export async function sendGuestMessage(conversationId: string, content: string): Promise<Message> {
  const { data } = await apiClient.post<ApiResponse<Message>>('/public/messages', {
    conversationId,
    content,
    senderType: 'CUSTOMER',
  });
  return data.data;
}

export async function getGuestMessages(conversationId: string): Promise<Message[]> {
  const { data } = await apiClient.get<ApiResponse<Message[]>>(`/public/messages/conversation/${conversationId}`);
  return data.data;
}
