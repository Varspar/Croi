import type { LucideIcon } from 'lucide-react';

interface QuickActionCardProps {
  icon: LucideIcon;
  title: string;
  description: string;
  onClick?: () => void;
}

export default function QuickActionCard({
  icon: Icon,
  title,
  description,
  onClick,
}: QuickActionCardProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="group flex flex-col items-start gap-3 rounded-xl border border-white/10 bg-croi-bg-card p-5 text-left transition-all duration-200 hover:border-croi-teal-bright hover:shadow-[0_0_20px_rgba(0,230,181,0.15)]"
    >
      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-croi-teal-muted text-croi-teal-bright transition-colors duration-200 group-hover:bg-croi-gradient group-hover:text-croi-bg-dark">
        <Icon size={20} />
      </div>
      <div>
        <p className="font-semibold text-croi-text-light">{title}</p>
        <p className="mt-1 text-sm text-gray-400">{description}</p>
      </div>
    </button>
  );
}
