import { Check, Star } from 'lucide-react';

interface PricingTier {
  name: string;
  description: string;
  price: string;
  cadence?: string;
  features: string[];
  ctaLabel: string;
  ctaHref: string;
  highlighted?: boolean;
}

const TIERS: PricingTier[] = [
  {
    name: 'Starter',
    description: 'For small teams getting started with AI support.',
    price: '$299',
    cadence: '/month',
    features: [
      'Up to 1,000 conversations/month',
      '1 Knowledge Base',
      'Basic Analytics',
      'Email Support',
    ],
    ctaLabel: 'Contact Sales',
    ctaHref: '#contact',
  },
  {
    name: 'Professional',
    description: 'For growing businesses with higher support volume.',
    price: '$799',
    cadence: '/month',
    features: [
      'Up to 10,000 conversations/month',
      '3 Knowledge Bases',
      'Advanced Analytics',
      'Priority Support',
      'Custom Branding',
    ],
    ctaLabel: 'Try Demo',
    ctaHref: '#demo',
    highlighted: true,
  },
  {
    name: 'Enterprise',
    description: 'For large organizations with custom requirements.',
    price: 'Custom',
    features: [
      'Unlimited conversations',
      'Unlimited Knowledge Bases',
      'White-label Solution',
      'Dedicated Support',
      'Custom Integrations',
    ],
    ctaLabel: 'Contact Sales',
    ctaHref: '#contact',
  },
];

export default function PricingSection() {
  return (
    <section id="pricing" className="scroll-mt-24 px-4 py-20 sm:px-6 sm:py-28 lg:px-8">
      <div className="mx-auto max-w-6xl">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-3xl font-bold text-croi-text-light sm:text-4xl">
            Simple pricing that scales with you
          </h2>
          <p className="mt-4 text-gray-400">
            Choose the plan that fits your support volume — upgrade any time.
          </p>
        </div>

        <div className="mt-16 grid grid-cols-1 gap-6 lg:grid-cols-3">
          {TIERS.map((tier) => (
            <div
              key={tier.name}
              className={`relative flex flex-col rounded-2xl border p-8 transition-transform duration-200 hover:scale-[1.02] ${
                tier.highlighted
                  ? 'border-croi-teal-bright bg-croi-bg-card shadow-[0_0_30px_rgba(0,230,181,0.2)]'
                  : 'border-white/10 bg-croi-bg-card'
              }`}
            >
              {tier.highlighted && (
                <span className="absolute -top-3 left-1/2 flex -translate-x-1/2 items-center gap-1 rounded-full bg-croi-gradient px-3 py-1 text-xs font-semibold text-croi-bg-dark">
                  <Star size={12} fill="currentColor" />
                  Recommended
                </span>
              )}

              <h3 className="text-lg font-semibold text-croi-text-light">{tier.name}</h3>
              <p className="mt-2 text-sm text-gray-400">{tier.description}</p>
              <p className="mt-6 text-4xl font-bold text-croi-text-light">
                {tier.price}
                {tier.cadence && (
                  <span className="text-base font-normal text-gray-400">{tier.cadence}</span>
                )}
              </p>

              <ul className="mt-6 flex-1 space-y-3">
                {tier.features.map((feature) => (
                  <li key={feature} className="flex items-start gap-2 text-sm text-gray-300">
                    <Check size={18} className="mt-0.5 shrink-0 text-croi-teal-bright" />
                    {feature}
                  </li>
                ))}
              </ul>

              <a
                href={tier.ctaHref}
                className={`mt-8 rounded-lg px-4 py-2.5 text-center text-sm font-semibold transition-opacity duration-200 hover:opacity-90 ${
                  tier.highlighted
                    ? 'bg-croi-gradient text-croi-bg-dark'
                    : 'border border-white/10 text-croi-text-light'
                }`}
              >
                {tier.ctaLabel}
              </a>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
