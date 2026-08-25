'use client';

import { useState } from 'react';
import { Save } from 'lucide-react';
import type { VoiceAgentConfig } from '@/types';

const TONES = ['FORMAL', 'FRIENDLY', 'PROFESSIONAL', 'CASUAL'];

interface AgentFormProps {
  config: VoiceAgentConfig;
  onSave: (config: VoiceAgentConfig) => Promise<void>;
}

export default function AgentForm({ config, onSave }: AgentFormProps) {
  const [draft, setDraft] = useState<VoiceAgentConfig>(config);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const set = <K extends keyof VoiceAgentConfig>(key: K, value: VoiceAgentConfig[K]) =>
    setDraft((current) => ({ ...current, [key]: value }));

  const save = async () => {
    setSaving(true);
    setError(null);
    try {
      await onSave(draft);
    } catch {
      setError('Configuration could not be saved.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-2xl space-y-5">
      <label className="block text-sm font-medium text-croi-text-light">
        System prompt
        <textarea
          className="mt-2 h-36 w-full rounded bg-croi-bg-card p-3 text-sm text-croi-text-light"
          value={draft.systemPrompt}
          onChange={(e) => set('systemPrompt', e.target.value)}
        />
      </label>

      <div>
        <p className="text-sm font-medium text-croi-text-light">Tone</p>
        <div className="mt-2 flex gap-2">
          {TONES.map((tone) => (
            <button
              key={tone}
              type="button"
              onClick={() => set('tone', tone)}
              className={`rounded-full px-3 py-1.5 text-xs font-medium capitalize transition-colors ${
                draft.tone === tone
                  ? 'bg-croi-teal-bright text-croi-bg-dark'
                  : 'bg-croi-bg-card text-gray-400 hover:text-croi-text-light'
              }`}
            >
              {tone.toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      <label className="block text-sm font-medium text-croi-text-light">
        Temperature — {draft.temperature.toFixed(1)}
        <input
          type="range"
          min={0}
          max={2}
          step={0.1}
          value={draft.temperature}
          onChange={(e) => set('temperature', Number(e.target.value))}
          className="mt-2 w-full accent-croi-teal-bright"
        />
      </label>

      <div className="grid grid-cols-2 gap-4">
        <label className="text-sm font-medium text-croi-text-light">
          Model
          <input
            className="mt-2 block w-full rounded bg-croi-bg-card p-3 text-sm"
            value={draft.model}
            onChange={(e) => set('model', e.target.value)}
          />
        </label>
        <label className="text-sm font-medium text-croi-text-light">
          Max call duration (sec)
          <input
            type="number"
            min={1}
            className="mt-2 block w-full rounded bg-croi-bg-card p-3 text-sm"
            value={draft.maxDurationSeconds}
            onChange={(e) => set('maxDurationSeconds', Number(e.target.value))}
          />
        </label>
      </div>

      {error && <p className="text-sm text-red-400">{error}</p>}

      <button
        type="button"
        disabled={saving}
        onClick={save}
        className="flex items-center gap-2 rounded bg-croi-gradient px-4 py-2 font-semibold text-croi-bg-dark disabled:opacity-50"
      >
        <Save size={16} />
        {saving ? 'Saving…' : 'Save configuration'}
      </button>
    </div>
  );
}
