import { Phone, PhoneOff } from 'lucide-react';
import type { VoiceAgent } from '@/types';

export default function AgentStatusCard({ agent }: { agent: VoiceAgent }) {
  const active = agent.status === 'ACTIVE';

  return (
    <div className="flex items-center gap-3 rounded-lg border border-white/10 bg-croi-bg-card p-4">
      <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full ${active ? 'bg-emerald-500/15 text-emerald-400' : 'bg-white/5 text-gray-500'}`}>
        {active ? <Phone size={18} /> : <PhoneOff size={18} />}
      </div>
      <div className="min-w-0 flex-1">
        <p className="truncate font-medium text-croi-text-light">{agent.name}</p>
        <p className="truncate text-xs text-gray-400">{agent.phoneNumber ?? 'No phone number bound'}</p>
      </div>
      <span className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-medium ${active ? 'bg-emerald-500/15 text-emerald-400' : 'bg-white/5 text-gray-400'}`}>
        {active ? 'Active' : 'Inactive'}
      </span>
    </div>
  );
}
