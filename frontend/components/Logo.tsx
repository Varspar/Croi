'use client';

import { useId } from 'react';

type LogoSize = 'small' | 'medium' | 'large';

const SIZE_MAP: Record<LogoSize, number> = {
  small: 28,
  medium: 36,
  large: 48,
};

interface LogoProps {
  size?: LogoSize;
  /** Renders the "Croi" wordmark next to the mark. */
  showText?: boolean;
  className?: string;
}

/**
 * Croi mark: heart (empathy) + a "C" carved into it (identity) + circuit
 * dots/lines trailing off the right edge (AI/tech). Gradient ids are made
 * unique per instance so multiple logos (header + sidebar) don't collide.
 */
export default function Logo({ size = 'medium', showText = false, className = '' }: LogoProps) {
  const uid = useId();
  const gradientId = `croi-logo-gradient-${uid}`;
  const height = SIZE_MAP[size];
  const width = Math.round(height * (56 / 48));

  return (
    <div className={`flex items-center gap-2 ${className}`}>
      <svg
        width={width}
        height={height}
        viewBox="0 0 56 48"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        role="img"
        aria-label="Croi logo"
      >
        <defs>
          <linearGradient id={gradientId} x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#00E6B5" />
            <stop offset="100%" stopColor="#008BA0" />
          </linearGradient>
        </defs>

        {/* Heart */}
        <path
          transform="translate(2, 2) scale(1.35)"
          d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"
          fill={`url(#${gradientId})`}
        />

        {/* "C" carved into the heart */}
        <circle
          cx="17"
          cy="15"
          r="7.5"
          fill="none"
          stroke="#0F1720"
          strokeWidth="3"
          strokeLinecap="round"
          strokeDasharray="35 12"
          strokeDashoffset="6"
          transform="rotate(45 17 15)"
        />

        {/* Circuit dots + connecting lines (AI/tech accent) */}
        <g stroke="#00E6B5" strokeWidth="1.5" strokeLinecap="round">
          <line x1="34" y1="16" x2="44" y2="10" />
          <line x1="35" y1="22" x2="47" y2="22" />
          <line x1="34" y1="28" x2="44" y2="34" />
        </g>
        <circle cx="44" cy="10" r="2" fill="#00E6B5" />
        <circle cx="47" cy="22" r="2.2" fill="#00E6B5" />
        <circle cx="44" cy="34" r="2" fill="#00E6B5" />
      </svg>

      {showText && <span className="text-xl font-bold tracking-tight text-white">Croi</span>}
    </div>
  );
}
