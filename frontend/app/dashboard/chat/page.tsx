'use client';

import { useCallback, useEffect, useState } from 'react';
import { Loader2, RefreshCw } from 'lucide-react';
import ChatWindow from '@/components/chat/ChatWindow';
import { getCurrentUserOrganization } from '@/services/organizationService';
import { getCurrentConversation } from '@/services/conversationService';
import { getMessages, sendMessage } from '@/services/messageService';
import type { ChatMessageProps } from '@/components/ChatMessage';
import type { Message } from '@/types';

function toChronological(history: Message[]): ChatMessageProps[] {
  // Backend returns newest-first; a transcript reads oldest-first.
  return [...history].reverse().map((message) => ({
    role: message.senderType === 'CUSTOMER' ? 'user' : 'ai',
    content: message.content,
    timestamp: new Date(message.createdAt),
    sources: message.sources,
  }));
}

export default function ChatPage() {
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessageProps[]>([]);
  const [loadingChat, setLoadingChat] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pendingContent, setPendingContent] = useState<string | null>(null);

  const initialize = useCallback(async () => {
    setLoadingChat(true);
    setError(null);

    try {
      const organizationId = await getCurrentUserOrganization();
      const convId = await getCurrentConversation(organizationId);
      setConversationId(convId);

      const history = await getMessages(convId);
      setMessages(toChronological(history));
    } catch {
      setError('Could not load your chat. Please check your connection and try again.');
    } finally {
      setLoadingChat(false);
    }
  }, []);

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
        await sendMessage(conversationId, content);
        const history = await getMessages(conversationId);
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

  if (loadingChat) {
    return (
      <div className="flex h-full items-center justify-center">
        <Loader2 className="animate-spin text-croi-teal-bright" size={32} />
      </div>
    );
  }

  return (
    <div className="flex h-full flex-col">
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
