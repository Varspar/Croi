import { ChevronDown } from 'lucide-react';
import Logo from '@/components/Logo';

const STATS = ['Available 24/7', 'Handles 80% of inquiries', 'Saves 60% on support costs'];

export default function HeroSection() {
  return (
    <section className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden px-4 py-20 sm:px-6 lg:px-8">
      {/* Soft teal glow behind the hero content */}
      <div
        aria-hidden
        className="pointer-events-none absolute left-1/2 top-1/3 h-[32rem] w-[32rem] -translate-x-1/2 -translate-y-1/2 rounded-full bg-croi-teal-bright/10 blur-3xl"
      />

      <div className="relative mx-auto flex max-w-4xl animate-in fade-in slide-in-from-bottom-4 flex-col items-center text-center duration-700">
        <Logo size="large" />

        <h1 className="mt-8 text-4xl font-bold tracking-tight text-croi-text-light sm:text-5xl lg:text-6xl">
          Meet <span className="bg-croi-gradient bg-clip-text text-transparent">Croi</span>
        </h1>

        <p className="mt-4 text-xl font-medium text-gray-200 sm:text-2xl">
          Your AI Customer Support Employee
        </p>

        <div className="mt-6 flex flex-wrap items-center justify-center gap-x-3 gap-y-2 text-sm font-medium text-gray-400 sm:text-base">
          {STATS.map((stat, index) => (
            <span key={stat} className="flex items-center gap-x-3">
              <span className="text-croi-teal-bright">{stat}</span>
              {index < STATS.length - 1 && <span className="text-gray-600">&middot;</span>}
            </span>
          ))}
        </div>

        <div className="mt-10 flex flex-col gap-4 sm:flex-row">
          <a
            href="#demo"
            className="rounded-lg bg-croi-gradient px-8 py-3.5 text-base font-semibold text-croi-bg-dark transition-opacity duration-200 hover:opacity-90"
          >
            Try Demo
          </a>
          <a
            href="#contact"
            className="rounded-lg border border-white/10 bg-croi-bg-card px-8 py-3.5 text-base font-semibold text-croi-text-light transition-colors duration-200 hover:border-croi-teal-bright"
          >
            Contact Sales
          </a>
        </div>
      </div>

      <a
        href="#features"
        aria-label="Scroll to learn more"
        className="absolute bottom-8 animate-bounce text-gray-500 transition-colors duration-200 hover:text-croi-teal-bright"
      >
        <ChevronDown size={28} />
      </a>
    </section>
  );
}
