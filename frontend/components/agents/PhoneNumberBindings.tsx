'use client';

import { useState } from 'react';
import { Phone, Save } from 'lucide-react';

interface PhoneNumberBindingsProps {
  phoneNumber?: string;
  onSave: (phoneNumber: string) => Promise<void>;
}

/**
 * Records which phone number routes to this agent. There's no telephony/SIP
 * integration wired up yet (see AGENT.md) — this only persists the binding on
 * VoiceAgent so the number is ready once inbound routing exists.
 */
export default function PhoneNumberBindings({ phoneNumber, onSave }: PhoneNumberBindingsProps) {
  const [value, setValue] = useState(phoneNumber ?? '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const save = async () => {
    setSaving(true);
    setError(null);
    try {
      await onSave(value.trim());
    } catch {
      setError('Could not save this phone number. It may already be bound to another agent.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="rounded-lg border border-white/10 bg-croi-bg-card p-4">
      <label className="block text-sm font-medium text-croi-text-light">
        Phone number
        <div className="mt-2 flex items-center gap-2">
          <div className="flex flex-1 items-center gap-2 rounded bg-croi-bg-dark px-3 py-2">
            <Phone size={16} className="shrink-0 text-gray-500" />
            <input
              className="w-full bg-transparent text-sm text-croi-text-light outline-none"
              placeholder="+1 555 123 4567"
              value={value}
              onChange={(e) => setValue(e.target.value)}
            />
          </div>
          <button
            type="button"
            disabled={saving || value.trim() === (phoneNumber ?? '')}
            onClick={save}
            className="flex shrink-0 items-center gap-1.5 rounded bg-croi-gradient px-3 py-2 text-sm font-semibold text-croi-bg-dark disabled:opacity-50"
          >
            <Save size={14} />
            {saving ? 'Saving…' : 'Save'}
          </button>
        </div>
      </label>
      {error && <p className="mt-2 text-xs text-red-400">{error}</p>}
    </div>
  );
}
