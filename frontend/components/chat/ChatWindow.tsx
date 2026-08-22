import { BarChart3, BookOpen, CheckSquare, Workflow } from 'lucide-react';
import Logo from '../Logo';
import ChatInput from '../ChatInput';
import QuickActionCard from '../QuickActionCard';
import ChatArea from './ChatArea';
import type { ChatMessageProps } from '../ChatMessage';

const QUICK_ACTIONS = [
  {
    icon: CheckSquare,
    title: 'Smart Tasks',
    description: 'Automate follow-ups and routine work for you.',
    prompt: 'What smart tasks can you help me automate?',
  },
  {
    icon: BookOpen,
    title: 'Knowledge Hub',
    description: 'Ask questions about your uploaded documents.',
    prompt: 'What do you know from my knowledge base?',
  },
  {
    icon: BarChart3,
    title: 'Data Analytics',
    description: 'Get insights from your customer conversations.',
    prompt: 'Show me insights from recent conversations.',
  },
  {
    icon: Workflow,
    title: 'Workflow',
    description: 'Set up automated response workflows.',
    prompt: 'Help me set up an automated workflow.',
  },
];

interface ChatWindowProps {
  messages: ChatMessageProps[];
  onSend: (content: string) => void;
  sending: boolean;
}

/**
 * Purely presentational shell — message/network state lives in the chat
 * page (app/dashboard/chat/page.tsx), which owns the org -> conversation ->
 * messages flow and passes it down here.
 */
export default function ChatWindow({ messages, onSend, sending }: ChatWindowProps) {
  const emptyState = (
    <div className="w-full max-w-2xl text-center">
      <div className="mb-4 flex justify-center">
        <Logo size="large" />
      </div>
      <h1 className="text-2xl font-bold text-croi-text-light">How can I help you today?</h1>
      <p className="mt-2 text-sm text-gray-400">
        Ask me anything, or pick a quick action to get started.
      </p>

      <div className="mt-8 grid grid-cols-1 gap-4 text-left sm:grid-cols-2">
        {QUICK_ACTIONS.map((action) => (
          <QuickActionCard
            key={action.title}
            icon={action.icon}
            title={action.title}
            description={action.description}
            onClick={() => onSend(action.prompt)}
          />
        ))}
      </div>
    </div>
  );

  return (
    <div className="flex h-full flex-col">
      <ChatArea messages={messages} emptyState={emptyState} />
      <div className="px-4 pb-4">
        <ChatInput onSend={onSend} loading={sending} />
      </div>
    </div>
  );
}
