'use client';

import { useState } from 'react';
import { FileText, ChevronDown } from 'lucide-react';

export interface DocumentSourceBadgeProps {
  documentName: string;
  relevanceScore: number;
  /** Chunk text the answer drew on. Absent if the backend couldn't resolve the chunk. */
  content?: string;
}

/**
 * "📄 Based on {document} • {score}% relevant" — click to expand the chunk
 * of the source document the AI actually used for that claim.
 */
export default function DocumentSourceBadge({ documentName, relevanceScore, content }: DocumentSourceBadgeProps) {
  const [expanded, setExpanded] = useState(false);
  const percent = Math.round(relevanceScore * 100);
  const canExpand = Boolean(content);

  return (
    <div className="rounded-lg border border-white/10 bg-croi-teal-muted/40 text-xs">
      <button
        type="button"
        onClick={() => canExpand && setExpanded((value) => !value)}
        className={`flex w-full items-center gap-1.5 px-2.5 py-1.5 text-left text-croi-teal-bright ${canExpand ? 'cursor-pointer' : 'cursor-default'}`}
        aria-expanded={expanded}
      >
        <FileText size={12} className="shrink-0" />
        <span className="truncate">
          Based on <span className="font-medium">{documentName}</span> · {percent}% relevant
        </span>
        {canExpand && (
          <ChevronDown size={12} className={`ml-auto shrink-0 transition-transform ${expanded ? 'rotate-180' : ''}`} />
        )}
      </button>
      {expanded && content && (
        <div className="border-t border-white/10 px-2.5 py-2 text-gray-400">
          <p className="mb-1 font-medium text-gray-300">From {documentName}:</p>
          <p className="whitespace-pre-wrap leading-relaxed">&ldquo;{content}&rdquo;</p>
        </div>
      )}
    </div>
  );
}
