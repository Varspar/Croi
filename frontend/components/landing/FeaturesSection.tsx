import type { LucideIcon } from 'lucide-react';
import {
  BarChart3,
  BookOpen,
  Clock,
  DollarSign,
  Languages,
  Puzzle,
  UserRoundCheck,
  Zap,
} from 'lucide-react';

interface Feature {
  icon: LucideIcon;
  title: string;
  description: string;
}

const FEATURES: Feature[] = [
  {
    icon: Clock,
    title: '24/7 Availability',
    description: 'Always online, never sleeps.',
  },
  {
    icon: Zap,
    title: 'Instant Responses',
    description: 'Answer customer questions in seconds.',
  },
  {
    icon: Languages,
    title: 'Multi-Language Support',
    description: 'Communicate with customers worldwide.',
  },
  {
    icon: Puzzle,
    title: 'Easy Integration',
    description: 'Connect to your existing systems.',
  },
  {
    icon: BookOpen,
    title: 'Custom Knowledge Base',
    description: 'Train Croi on your company docs.',
  },
  {
    icon: BarChart3,
    title: 'Smart Analytics',
    description: 'Understand customer needs better.',
  },
  {
    icon: UserRoundCheck,
    title: 'Human Escalation',
    description: 'Seamless handoff to your team.',
  },
  {
    icon: DollarSign,
    title: 'Cost Effective',
    description: 'Reduce support costs by up to 60%.',
  },
];

export default function FeaturesSection() {
  return (
    <section id="features" className="scroll-mt-24 px-4 py-20 sm:px-6 sm:py-28 lg:px-8">
      <div className="mx-auto max-w-7xl">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-3xl font-bold text-croi-text-light sm:text-4xl">
            Everything your support team needs, minus the headcount
          </h2>
          <p className="mt-4 text-gray-400">
            Croi handles the repetitive, high-volume work so your team can focus on what
            actually needs a human.
          </p>
        </div>

        <div className="mt-16 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {FEATURES.map(({ icon: Icon, title, description }) => (
            <div
              key={title}
              className="group rounded-xl border border-white/10 bg-croi-bg-card p-6 transition-all duration-200 hover:-translate-y-1 hover:border-croi-teal-bright hover:shadow-[0_0_24px_rgba(0,230,181,0.2)]"
            >
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-croi-teal-muted text-croi-teal-bright transition-colors duration-200 group-hover:bg-croi-gradient group-hover:text-croi-bg-dark">
                <Icon size={24} />
              </div>
              <h3 className="mt-4 font-semibold text-croi-text-light">{title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-gray-400">{description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
