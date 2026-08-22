'use client';

import { Suspense, useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { Loader2, RefreshCw } from 'lucide-react';
import ChatWindow from '@/components/chat/ChatWindow';
import Logo from '@/components/Logo';
import {
  createGuestConversation,
  getGuestMessages,
  getWorkspaceBranding,
  sendGuestMessage,
} from '@/services/publicChatService';
import type { ChatMessageProps } from '@/components/ChatMessage';
import type { Message, WorkspaceBranding } from '@/types';

function toChronological(history: Message[]): ChatMessageProps[] {
  // Backend returns newest-first; a transcript reads oldest-first.
  return [...history].reverse().map((message) => ({
    role: message.senderType === 'CUSTOMER' ? 'user' : 'ai',
    content: message.content,
    timestamp: new Date(message.createdAt),
    sources: message.sources,
  }));
}

function conversationStorageKey(workspaceId: string) {
  return `croi-guest-conversation-${workspaceId}`;
}

function GuestChat({ workspaceId }: { workspaceId: string }) {
  const [branding, setBranding] = useState<WorkspaceBranding | null>(null);
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessageProps[]>([]);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pendingContent, setPendingContent] = useState<string | null>(null);

  const initialize = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const brandingData = await getWorkspaceBranding(workspaceId);
      setBranding(brandingData);

      // Reuse an existing guest conversation from this browser session if there is
      // one, so a page refresh doesn't start the visitor over from a blank chat.
      const storageKey = conversationStorageKey(workspaceId);
      let convId = typeof window !== 'undefined' ? sessionStorage.getItem(storageKey) : null;
      if (!convId) {
        const conversation = await createGuestConversation(workspaceId);
        convId = conversation.id;
        if (typeof window !== 'undefined') sessionStorage.setItem(storageKey, convId);
      }
      setConversationId(convId);

      const history = await getGuestMessages(convId);
      setMessages(toChronological(history));
    } catch {
      setError('Could not load this chat. Check the link and try again.');
    } finally {
      setLoading(false);
    }
  }, [workspaceId]);

  useEffect(() => {
    initialize();
  }, [initialize]);

  const handleSend = useCallback(
    async (content: string) => {
      if (!conversationId) return;

      setError(null);
      setPendingContent(null);

      const optimisticMessage: ChatMessageProps = { role: 'user', content, timestamp: new Date() };
      setMessages((prev) => [...prev, optimisticMessage]);
      setSending(true);

      try {
        await sendGuestMessage(conversationId, content);
        const history = await getGuestMessages(conversationId);
        setMessages(toChronological(history));
      } catch {
        setMessages((prev) => prev.filter((m) => m !== optimisticMessage));
        setPendingContent(content);
        setError('Failed to send your message. Please try again.');
      } finally {
        setSending(false);
      }
    },
    [conversationId],
  );

  const handleRetry = () => {
    if (pendingContent) {
      handleSend(pendingContent);
    } else {
      initialize();
    }
  };

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center bg-croi-bg-dark">
        <Loader2 className="animate-spin text-croi-teal-bright" size={32} />
      </div>
    );
  }

  if (error && !conversationId) {
    return (
      <div className="flex h-screen flex-col items-center justify-center gap-3 bg-croi-bg-dark text-center text-croi-text-light">
        <p className="text-lg font-semibold">Chat unavailable</p>
        <p className="max-w-sm text-sm text-gray-400">{error}</p>
      </div>
    );
  }

  return (
    <div className="flex h-screen flex-col bg-croi-bg-dark">
      <header className="flex items-center gap-3 border-b border-white/10 px-4 py-3">
        {branding?.logo ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={branding.logo} alt={branding.name} className="h-8 w-8 rounded object-contain" />
        ) : (
          <Logo size="small" />
        )}
        <div className="min-w-0">
          <p className="truncate font-semibold text-croi-text-light">{branding?.name ?? 'Chat'}</p>
          {branding?.description && <p className="truncate text-xs text-gray-400">{branding.description}</p>}
        </div>
      </header>

      {error && (
        <div className="mx-4 mt-4 flex items-center justify-between gap-4 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-400">
          <span>{error}</span>
          <button
            type="button"
            onClick={handleRetry}
            className="flex shrink-0 items-center gap-1.5 rounded-md bg-red-500/20 px-3 py-1.5 font-medium text-red-300 transition-colors duration-200 hover:bg-red-500/30"
          >
            <RefreshCw size={14} />
            Retry
          </button>
        </div>
      )}

      <ChatWindow messages={messages} onSend={handleSend} sending={sending} />
    </div>
  );
}

function GuestChatEntry() {
  const searchParams = useSearchParams();
  const workspaceId = searchParams.get('workspace');

  if (!workspaceId) {
    return (
      <div className="flex h-screen flex-col items-center justify-center gap-3 bg-croi-bg-dark text-center text-croi-text-light">
        <p className="text-lg font-semibold">No workspace specified</p>
        <p className="max-w-sm text-sm text-gray-400">
          This chat link is missing a workspace. Ask the site owner for the correct link.
        </p>
      </div>
    );
  }

  return <GuestChat workspaceId={workspaceId} />;
}

export default function PublicChatPage() {
  return (
    <Suspense
      fallback={
        <div className="flex h-screen items-center justify-center bg-croi-bg-dark">
          <Loader2 className="animate-spin text-croi-teal-bright" size={32} />
        </div>
      }
    >
      <GuestChatEntry />
    </Suspense>
  );
}
