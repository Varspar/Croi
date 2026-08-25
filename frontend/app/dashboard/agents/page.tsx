'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Loader2, Plus } from 'lucide-react';
import { getCurrentUserOrganization } from '@/services/organizationService';
import { createAgent, listAgents } from '@/services/voiceAgentService';
import AgentStatusCard from '@/components/agents/AgentStatusCard';
import type { VoiceAgent } from '@/types';

export default function AgentDashboardPage() {
  const [workspaceId, setWorkspaceId] = useState<string>();
  const [agents, setAgents] = useState<VoiceAgent[]>([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    try {
      const id = await getCurrentUserOrganization();
      setWorkspaceId(id);
      setAgents(await listAgents(id));
    } catch {
      setError('Could not load your voice agents.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleCreate = async () => {
    if (!workspaceId) return;
    const name = window.prompt('Name this agent (e.g. "Front Desk")');
    if (!name?.trim()) return;

    setCreating(true);
    setError(null);
    try {
      const agent = await createAgent(workspaceId, name.trim());
      setAgents((current) => [agent, ...current]);
    } catch {
      setError('Could not create a new agent.');
    } finally {
      setCreating(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Loader2 className="animate-spin text-croi-teal-bright" size={32} />
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto p-6 text-croi-text-light">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Voice agents</h1>
          <p className="mt-1 text-sm text-gray-400">Configure the AI agents answering your inbound calls.</p>
        </div>
        <button
          type="button"
          disabled={creating}
          onClick={handleCreate}
          className="flex items-center gap-2 rounded-lg bg-croi-gradient px-4 py-2 text-sm font-semibold text-croi-bg-dark disabled:opacity-50"
        >
          <Plus size={16} />
          {creating ? 'Creating…' : 'New agent'}
        </button>
      </div>

      {error && <p className="mt-4 rounded bg-red-500/10 p-3 text-sm text-red-300">{error}</p>}

      <div className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {agents.map((agent) => (
          <Link key={agent.id} href={`/dashboard/agents/${agent.id}`} className="block">
            <AgentStatusCard agent={agent} />
          </Link>
        ))}
      </div>

      {!agents.length && (
        <p className="mt-10 text-center text-sm text-gray-400">
          No voice agents yet. Create one to get started.
        </p>
      )}
    </div>
  );
}
