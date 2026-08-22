import { Check } from 'lucide-react';
import Logo from '@/components/Logo';

const KEY_POINTS = [
  'Powered by state-of-the-art AI models',
  'Trained on best customer service practices',
  'Respects customer privacy',
  'Fully customizable to your brand',
  'Built for airlines, government agencies, and enterprises worldwide',
];

const WHY_DIFFERENT = [
  "Not a chatbot — it's an AI employee",
  'Understands context and nuance',
  'Continuous learning',
  'Real-time analytics',
];

export default function AboutSection() {
  return (
    <section id="about" className="scroll-mt-24 px-4 py-20 sm:px-6 sm:py-28 lg:px-8">
      <div className="mx-auto max-w-6xl">
        <div className="grid grid-cols-1 items-center gap-12 lg:grid-cols-2">
          <div>
            <h2 className="text-3xl font-bold sm:text-4xl">
              <span className="bg-croi-gradient bg-clip-text text-transparent">
                Why Choose Croi?
              </span>
            </h2>

            <p className="mt-6 text-lg font-medium text-croi-text-light">
              Every company deserves world-class customer support, 24/7. Croi makes that
              possible.
            </p>

            <p className="mt-4 text-gray-400">
              We're building the future of customer support by creating AI employees that
              understand your business and delight your customers.
            </p>

            <p className="mt-4 text-gray-400">
              Croi's name comes from <span className="text-croi-teal-bright">croí</span>, the
              Irish word for heart. That's the idea behind the product: support that's
              genuinely empathetic, not just automated.
            </p>
          </div>

          <div className="flex items-center justify-center rounded-2xl border border-white/10 bg-croi-bg-card p-12">
            <Logo size="large" />
          </div>
        </div>

        <div className="mt-16 grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div className="rounded-xl border border-white/10 bg-croi-bg-card p-6">
            <h3 className="font-semibold text-croi-text-light">What you get</h3>
            <ul className="mt-4 space-y-3">
              {KEY_POINTS.map((point) => (
                <li key={point} className="flex items-start gap-2 text-sm text-gray-300">
                  <Check size={18} className="mt-0.5 shrink-0 text-croi-teal-bright" />
                  {point}
                </li>
              ))}
            </ul>
          </div>

          <div className="rounded-xl border border-white/10 bg-croi-bg-card p-6">
            <h3 className="font-semibold text-croi-text-light">Why Croi is different</h3>
            <ul className="mt-4 space-y-3">
              {WHY_DIFFERENT.map((point) => (
                <li key={point} className="flex items-start gap-2 text-sm text-gray-300">
                  <Check size={18} className="mt-0.5 shrink-0 text-croi-teal-bright" />
                  {point}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
}
