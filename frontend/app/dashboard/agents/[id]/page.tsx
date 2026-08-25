'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import { getCurrentUserOrganization } from '@/services/organizationService';
import { getAgent, updateAgent, updateAgentConfig } from '@/services/voiceAgentService';
import AgentStatusCard from '@/components/agents/AgentStatusCard';
import PhoneNumberBindings from '@/components/agents/PhoneNumberBindings';
import AgentForm from '@/components/agents/AgentForm';
import CallHistoryTable from '@/components/agents/CallHistoryTable';
import type { VoiceAgent, VoiceAgentConfig } from '@/types';

export default function AgentConfigPage() {
  const params = useParams<{ id: string }>();
  const [workspaceId, setWorkspaceId] = useState<string>();
  const [agent, setAgent] = useState<VoiceAgent>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const orgId = await getCurrentUserOrganization();
        setWorkspaceId(orgId);
        setAgent(await getAgent(params.id, orgId));
      } catch {
        setError('Could not load this agent.');
      } finally {
        setLoading(false);
      }
    })();
  }, [params.id]);

  const savePhoneNumber = async (phoneNumber: string) => {
    if (!workspaceId || !agent) return;
    setAgent(await updateAgent(agent.id, workspaceId, { phoneNumber }));
  };

  const saveConfig = async (config: VoiceAgentConfig) => {
    if (!workspaceId || !agent) return;
    setAgent(await updateAgentConfig(agent.id, workspaceId, config));
  };

  const toggleStatus = async () => {
    if (!workspaceId || !agent) return;
    const status = agent.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    setAgent(await updateAgent(agent.id, workspaceId, { status }));
  };

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Loader2 className="animate-spin text-croi-teal-bright" size={32} />
      </div>
    );
  }

  if (error || !agent) {
    return (
      <div className="flex h-full items-center justify-center p-6 text-center">
        <p className="text-sm text-red-300">{error ?? 'Agent not found.'}</p>
      </div>
    );
  }

  return (
    <div className="h-full space-y-8 overflow-y-auto p-6 text-croi-text-light">
      <div>
        <h1 className="text-2xl font-bold">{agent.name}</h1>
        <p className="mt-1 text-sm text-gray-400">Configure how this agent handles inbound calls.</p>
      </div>

      <div className="flex items-center gap-3">
        <div className="flex-1">
          <AgentStatusCard agent={agent} />
        </div>
        <button
          type="button"
          onClick={toggleStatus}
          className="shrink-0 rounded-lg border border-white/10 px-4 py-2 text-sm font-medium text-croi-text-light hover:bg-croi-bg-card"
        >
          {agent.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
        </button>
      </div>

      <section>
        <h2 className="mb-3 text-lg font-semibold">Phone number</h2>
        <PhoneNumberBindings phoneNumber={agent.phoneNumber} onSave={savePhoneNumber} />
      </section>

      <section>
        <h2 className="mb-3 text-lg font-semibold">Voice configuration</h2>
        <AgentForm config={agent.config} onSave={saveConfig} />
      </section>

      <section>
        <h2 className="mb-3 text-lg font-semibold">Call history</h2>
        <CallHistoryTable />
      </section>
    </div>
  );
}
