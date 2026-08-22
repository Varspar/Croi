import apiClient from '@/lib/api';
import type { ApiResponse, Message } from '@/types';

export async function sendMessage(conversationId: string, content: string): Promise<Message> {
  const { data } = await apiClient.post<ApiResponse<Message>>('/messages', {
    conversationId,
    content,
    senderType: 'CUSTOMER',
  });
  return data.data;
}

/** The backend returns messages newest-first; reverse for chronological display. */
export async function getMessages(conversationId: string): Promise<Message[]> {
  const { data } = await apiClient.get<ApiResponse<Message[]>>(
    `/messages/conversation/${conversationId}`,
  );
  return data.data;
}

export async function deleteMessage(messageId: string): Promise<void> {
  await apiClient.delete<ApiResponse<void>>(`/messages/${messageId}`);
}
