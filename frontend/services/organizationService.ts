import apiClient from '@/lib/api';
import type { ApiResponse, Organization } from '@/types';

function defaultOrganizationName(): string {
  const raw = localStorage.getItem('user');
  if (raw) {
    try {
      const user = JSON.parse(raw);
      if (user?.firstName) return `${user.firstName}'s Workspace`;
    } catch {
      // malformed cache, fall through to default
    }
  }
  return 'My Workspace';
}

export async function getOrganizations(): Promise<Organization[]> {
  const { data } = await apiClient.get<ApiResponse<Organization[]>>('/organizations');
  return data.data;
}

export async function createOrganization(name: string): Promise<Organization> {
  const { data } = await apiClient.post<ApiResponse<Organization>>('/organizations', { name });
  return data.data;
}

/**
 * Returns the id of the user's organization, creating one automatically on
 * first use. Every authenticated user currently gets exactly one workspace
 * (there's no org-switcher UI yet), so the first org returned is used.
 */
export async function getCurrentUserOrganization(): Promise<string> {
  const organizations = await getOrganizations();
  if (organizations.length > 0) {
    return organizations[0].id;
  }

  const created = await createOrganization(defaultOrganizationName());
  return created.id;
}
