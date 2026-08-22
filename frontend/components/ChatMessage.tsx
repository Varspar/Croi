import DocumentSourceBadge from './chat/DocumentSourceBadge';

export type MessageRole = 'user' | 'ai';

export interface ChatMessageSource {
  documentName: string;
  relevanceScore: number;
  content?: string;
}

export interface ChatMessageProps {
  role: MessageRole;
  content: string;
  timestamp: Date;
  sources?: ChatMessageSource[];
}

export default function ChatMessage({ role, content, timestamp, sources = [] }: ChatMessageProps) {
  const isUser = role === 'user';

  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div
        className={`max-w-[75%] rounded-2xl px-4 py-3 ${
          isUser
            ? 'bg-croi-gradient text-croi-bg-dark'
            : 'border border-white/5 bg-croi-bg-card text-croi-text-light'
        }`}
      >
        <p className="whitespace-pre-wrap text-sm leading-relaxed">{content}</p>
        {!isUser && sources.length > 0 && (
          <div className="mt-3 flex flex-col gap-1.5 border-t border-white/10 pt-2">
            {sources.map((source, index) => (
              <DocumentSourceBadge
                key={`${source.documentName}-${index}`}
                documentName={source.documentName}
                relevanceScore={source.relevanceScore}
                content={source.content}
              />
            ))}
          </div>
        )}
        <p
          className={`mt-1.5 text-[11px] ${
            isUser ? 'text-croi-bg-dark/70' : 'text-gray-500'
          }`}
        >
          {timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
        </p>
      </div>
    </div>
  );
}
