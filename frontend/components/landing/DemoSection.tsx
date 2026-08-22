'use client';

import { useState } from 'react';
import { MessageSquareText, Sparkles, Zap } from 'lucide-react';
import ChatMessage, { type ChatMessageProps } from '@/components/ChatMessage';
import ChatInput from '@/components/ChatInput';
import Logo from '@/components/Logo';

const HIGHLIGHTS = [
  { icon: Zap, label: 'Instant responses' },
  { icon: MessageSquareText, label: 'Natural conversation' },
  { icon: Sparkles, label: 'Context-aware answers' },
];

// Keyword-matched canned replies for this MVP demo — not connected to the real
// backend/AI. A visitor typing something unmatched gets an honest fallback
// rather than a made-up answer.
const CANNED_RESPONSES: { keywords: string[]; response: string }[] = [
  {
    keywords: ['baggage', 'luggage', 'bag'],
    response:
      'Business class passengers are entitled to higher baggage limits — typically 2 pieces up to 32kg each, though exact limits vary by route and fare. Want me to check your specific booking?',
  },
  {
    keywords: ['change', 'reschedule', 'flight'],
    response:
      "You can modify your booking through Manage Booking on our website or app. Change fees depend on your fare type — I can walk you through it, or connect you with a human agent if you'd prefer.",
  },
];

const FALLBACK_RESPONSE =
  "Great question! In the full version, Croi answers this from your live knowledge base. Sign up to see Croi respond with real, up-to-date information.";

function matchResponse(input: string): string {
  const lower = input.toLowerCase();
  const match = CANNED_RESPONSES.find(({ keywords }) =>
    keywords.some((keyword) => lower.includes(keyword)),
  );
  return match ? match.response : FALLBACK_RESPONSE;
}

const INITIAL_MESSAGES: ChatMessageProps[] = [
  {
    role: 'user',
    content: "What's your baggage allowance for business class?",
    timestamp: new Date(),
  },
  {
    role: 'ai',
    content:
      'Business class passengers are entitled to higher baggage limits — typically 2 pieces up to 32kg each, though exact limits vary by route and fare. Want me to check your specific booking?',
    timestamp: new Date(),
  },
];

export default function DemoSection() {
  const [messages, setMessages] = useState<ChatMessageProps[]>(INITIAL_MESSAGES);
  const [thinking, setThinking] = useState(false);

  const handleSend = (content: string) => {
    setMessages((prev) => [...prev, { role: 'user', content, timestamp: new Date() }]);
    setThinking(true);

    setTimeout(() => {
      setMessages((prev) => [
        ...prev,
        { role: 'ai', content: matchResponse(content), timestamp: new Date() },
      ]);
      setThinking(false);
    }, 900);
  };

  return (
    <section id="demo" className="scroll-mt-24 px-4 py-20 sm:px-6 sm:py-28 lg:px-8">
      <div className="mx-auto max-w-6xl">
        <div className="grid grid-cols-1 items-center gap-12 lg:grid-cols-2">
          <div>
            <h2 className="text-3xl font-bold text-croi-text-light sm:text-4xl">
              Try Croi Right Now
            </h2>
            <p className="mt-4 text-gray-400">
              See how Croi responds to customer inquiries. Ask about baggage policies, flight
              information, or anything else.
            </p>

            <ul className="mt-8 space-y-3">
              {HIGHLIGHTS.map(({ icon: Icon, label }) => (
                <li key={label} className="flex items-center gap-3 text-sm text-gray-300">
                  <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-croi-teal-muted text-croi-teal-bright">
                    <Icon size={16} />
                  </span>
                  {label}
                </li>
              ))}
            </ul>

            <div className="mt-8 flex flex-col gap-4 sm:flex-row">
              <a
                href="/auth/signup"
                className="rounded-lg bg-croi-gradient px-6 py-3 text-center text-sm font-semibold text-croi-bg-dark transition-opacity duration-200 hover:opacity-90"
              >
                Get Started
              </a>
              <a
                href="#contact"
                className="rounded-lg border border-white/10 bg-croi-bg-card px-6 py-3 text-center text-sm font-semibold text-croi-text-light transition-colors duration-200 hover:border-croi-teal-bright"
              >
                Contact Sales
              </a>
            </div>

            <p className="mt-4 text-xs text-gray-500">
              This is a simulated preview. Sign up to connect Croi to your real knowledge base.
            </p>
          </div>

          <div className="flex h-[32rem] flex-col overflow-hidden rounded-2xl border border-white/10 bg-croi-bg-card">
            <div className="flex items-center gap-2 border-b border-white/5 px-5 py-4">
              <Logo size="small" />
              <div>
                <p className="text-sm font-semibold text-croi-text-light">Croi Assistant</p>
                <p className="flex items-center gap-1.5 text-xs text-gray-500">
                  <span className="h-1.5 w-1.5 rounded-full bg-croi-teal-bright" />
                  Online
                </p>
              </div>
            </div>

            <div className="flex-1 space-y-4 overflow-y-auto px-5 py-4">
              {messages.map((message, index) => (
                <ChatMessage key={index} {...message} />
              ))}
              {thinking && (
                <div className="flex justify-start">
                  <div className="rounded-2xl border border-white/5 bg-croi-bg-dark px-4 py-3 text-sm text-gray-500">
                    Croi is typing...
                  </div>
                </div>
              )}
            </div>

            <div className="px-4 pb-4">
              <ChatInput onSend={handleSend} loading={thinking} />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
