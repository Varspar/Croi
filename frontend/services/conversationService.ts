import apiClient from '@/lib/api';
import type { ApiResponse, Conversation, ConversationStatus } from '@/types';

export async function getConversations(organizationId: string): Promise<Conversation[]> {
  const { data } = await apiClient.get<ApiResponse<Conversation[]>>('/conversations', {
    params: { organizationId },
  });
  return data.data;
}

export async function createConversation(
  organizationId: string,
  customerName?: string,
  customerEmail?: string,
): Promise<Conversation> {
  const { data } = await apiClient.post<ApiResponse<Conversation>>('/conversations', {
    organizationId,
    customerName,
    customerEmail,
  });
  return data.data;
}

/**
 * Returns the id of the conversation to chat in, creating one if the
 * organization doesn't have one yet. Note: the backend doesn't order
 * `GET /conversations`, so this picks whichever conversation comes back
 * first — fine while each org has a single conversation, but will need a
 * real "current conversation" concept once multiple are supported.
 */
export async function getCurrentConversation(organizationId: string): Promise<string> {
  const conversations = await getConversations(organizationId);
  if (conversations.length > 0) {
    return conversations[0].id;
  }

  const created = await createConversation(organizationId);
  return created.id;
}

export async function updateConversationStatus(
  conversationId: string,
  status: ConversationStatus,
): Promise<Conversation> {
  const { data } = await apiClient.put<ApiResponse<Conversation>>(`/conversations/${conversationId}`, {
    status,
  });
  return data.data;
}

export async function escalateConversation(conversationId: string): Promise<Conversation> {
  const { data } = await apiClient.post<ApiResponse<Conversation>>(
    `/conversations/${conversationId}/escalate`,
  );
  return data.data;
}
