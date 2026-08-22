import apiClient from '@/lib/api';
import type { ApiResponse, ContactRequest } from '@/types';

/** Public, unauthenticated endpoint — used by the landing page contact form. */
export async function submitContactForm(request: ContactRequest): Promise<string> {
  const { data } = await apiClient.post<ApiResponse<null>>('/contact', request);
  return data.message ?? "Thanks for reaching out! We'll be in touch soon.";
}
