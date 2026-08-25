import apiClient from '@/lib/api';
import type { ApiResponse, VoiceAgent, VoiceAgentConfig } from '@/types';

export async function listAgents(organizationId: string): Promise<VoiceAgent[]> {
  const { data } = await apiClient.get<ApiResponse<VoiceAgent[]>>('/voice-agents', { params: { organizationId } });
  return data.data;
}

export async function getAgent(id: string, organizationId: string): Promise<VoiceAgent> {
  const { data } = await apiClient.get<ApiResponse<VoiceAgent>>(`/voice-agents/${id}`, { params: { organizationId } });
  return data.data;
}

export async function createAgent(organizationId: string, name: string, phoneNumber?: string): Promise<VoiceAgent> {
  const { data } = await apiClient.post<ApiResponse<VoiceAgent>>('/voice-agents', { organizationId, name, phoneNumber });
  return data.data;
}

export async function updateAgent(
  id: string,
  organizationId: string,
  updates: { name?: string; status?: string; phoneNumber?: string },
): Promise<VoiceAgent> {
  const { data } = await apiClient.put<ApiResponse<VoiceAgent>>(`/voice-agents/${id}`, updates, { params: { organizationId } });
  return data.data;
}

export async function updateAgentConfig(id: string, organizationId: string, config: VoiceAgentConfig): Promise<VoiceAgent> {
  const { data } = await apiClient.post<ApiResponse<VoiceAgent>>(`/voice-agents/${id}/config`, config, { params: { organizationId } });
  return data.data;
}
