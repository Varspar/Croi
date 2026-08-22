import apiClient from '@/lib/api';
import type { ApiResponse, Document, WorkspaceAnalytics, WorkspaceConfiguration } from '@/types';

export async function getWorkspaceDocuments(workspaceId: string): Promise<Document[]> {
  const { data } = await apiClient.get<ApiResponse<Document[]>>(`/workspaces/${workspaceId}/documents`);
  return data.data;
}
export async function uploadWorkspaceDocument(workspaceId: string, file: File, title = file.name): Promise<Document> {
  const form = new FormData(); form.append('file', file); form.append('title', title);
  const { data } = await apiClient.post<ApiResponse<Document>>(`/workspaces/${workspaceId}/documents`, form);
  return data.data;
}
export async function deleteWorkspaceDocument(workspaceId: string, documentId: string): Promise<void> { await apiClient.delete(`/workspaces/${workspaceId}/documents/${documentId}`); }
export async function regenerateDocument(workspaceId: string, documentId: string): Promise<Document> {
  const { data } = await apiClient.post<ApiResponse<Document>>(`/workspaces/${workspaceId}/documents/${documentId}/embeddings/generate`); return data.data;
}
export async function getWorkspaceConfiguration(workspaceId: string): Promise<WorkspaceConfiguration> {
  const { data } = await apiClient.get<ApiResponse<WorkspaceConfiguration>>(`/workspaces/${workspaceId}/configuration`); return data.data;
}
export async function saveWorkspaceConfiguration(workspaceId: string, configuration: WorkspaceConfiguration): Promise<WorkspaceConfiguration> {
  const { data } = await apiClient.put<ApiResponse<WorkspaceConfiguration>>(`/workspaces/${workspaceId}/configuration`, configuration); return data.data;
}
export async function getWorkspaceAnalytics(workspaceId: string): Promise<WorkspaceAnalytics> {
  const { data } = await apiClient.get<ApiResponse<WorkspaceAnalytics>>(`/workspaces/${workspaceId}/analytics`); return data.data;
}
