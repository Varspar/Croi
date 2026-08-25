import { PhoneCall } from 'lucide-react';

export interface CallHistoryEntry {
  id: string;
  patientPhone: string;
  callStartTime: string;
  durationSeconds?: number;
  transcriptionStatus: string;
}

interface CallHistoryTableProps {
  calls?: CallHistoryEntry[];
}

/**
 * There's no call-history endpoint yet — CallRecording exists in the backend
 * schema (from the appointment-system work) but nothing exposes it over the API,
 * since no voice pipeline produces calls yet. This renders the empty state until
 * that endpoint exists; pass `calls` once it does.
 */
export default function CallHistoryTable({ calls = [] }: CallHistoryTableProps) {
  if (calls.length === 0) {
    return (
      <div className="flex flex-col items-center gap-2 rounded-lg border border-white/10 bg-croi-bg-card p-10 text-center">
        <PhoneCall size={24} className="text-gray-600" />
        <p className="text-sm text-gray-400">No calls yet.</p>
        <p className="text-xs text-gray-500">Calls will appear here once this agent starts taking inbound calls.</p>
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-lg border border-white/10">
      <table className="w-full text-left text-sm">
        <thead className="bg-croi-bg-card text-xs uppercase text-gray-500">
          <tr>
            <th className="px-4 py-3 font-medium">Caller</th>
            <th className="px-4 py-3 font-medium">Time</th>
            <th className="px-4 py-3 font-medium">Duration</th>
            <th className="px-4 py-3 font-medium">Transcript</th>
          </tr>
        </thead>
        <tbody>
          {calls.map((call) => (
            <tr key={call.id} className="border-t border-white/5">
              <td className="px-4 py-3 text-croi-text-light">{call.patientPhone}</td>
              <td className="px-4 py-3 text-gray-400">{new Date(call.callStartTime).toLocaleString()}</td>
              <td className="px-4 py-3 text-gray-400">{call.durationSeconds ? `${Math.round(call.durationSeconds / 60)}m` : '—'}</td>
              <td className="px-4 py-3 text-gray-400 capitalize">{call.transcriptionStatus}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
