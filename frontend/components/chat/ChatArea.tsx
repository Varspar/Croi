'use client';

import { useEffect, useRef } from 'react';
import ChatMessage, { type ChatMessageProps } from '../ChatMessage';

interface ChatAreaProps {
  messages: ChatMessageProps[];
  /** Shown instead of the message list when there are no messages yet. */
  emptyState?: React.ReactNode;
}

export default function ChatArea({ messages, emptyState }: ChatAreaProps) {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages.length]);

  if (messages.length === 0 && emptyState) {
    return <div className="flex flex-1 flex-col items-center justify-center overflow-y-auto px-4">{emptyState}</div>;
  }

  return (
    <div className="flex-1 space-y-4 overflow-y-auto px-4 py-6">
      {messages.map((message, index) => (
        <ChatMessage key={index} {...message} />
      ))}
      <div ref={bottomRef} />
    </div>
  );
}
