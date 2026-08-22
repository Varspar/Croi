'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { Menu, X } from 'lucide-react';
import Logo from '@/components/Logo';

const NAV_LINKS = [
  { label: 'Features', href: '#features' },
  { label: 'Pricing', href: '#pricing' },
  { label: 'About', href: '#about' },
  { label: 'Contact', href: '#contact' },
  { label: 'Try Demo', href: '#demo' },
];

const SECTION_IDS = NAV_LINKS.map((link) => link.href.slice(1));

export default function Navigation() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [activeSection, setActiveSection] = useState<string>('');

  useEffect(() => {
    const sections = SECTION_IDS.map((id) => document.getElementById(id)).filter(
      (el): el is HTMLElement => el !== null,
    );

    if (sections.length === 0) return;

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setActiveSection(entry.target.id);
          }
        });
      },
      // Treat a section as "active" once it's within the band just below the
      // sticky nav, roughly a third of the way down the viewport.
      { rootMargin: '-96px 0px -60% 0px', threshold: 0 },
    );

    sections.forEach((section) => observer.observe(section));
    return () => observer.disconnect();
  }, []);

  return (
    <header className="sticky top-0 z-50 border-b border-white/5 bg-croi-bg-dark/90 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
        <Link href="/" className="shrink-0">
          <Logo size="small" showText />
        </Link>

        <nav className="hidden items-center gap-8 lg:flex">
          {NAV_LINKS.map((link) => {
            const isActive = activeSection === link.href.slice(1);
            return (
              <a
                key={link.label}
                href={link.href}
                className={`text-sm font-medium transition-colors duration-200 hover:text-croi-teal-bright ${
                  isActive ? 'text-croi-teal-bright' : 'text-gray-300'
                }`}
              >
                {link.label}
              </a>
            );
          })}
        </nav>

        <div className="hidden lg:block">
          <Link
            href="/auth/signup"
            className="rounded-lg bg-croi-gradient px-5 py-2.5 text-sm font-semibold text-croi-bg-dark transition-opacity duration-200 hover:opacity-90"
          >
            Get Started
          </Link>
        </div>

        <button
          type="button"
          onClick={() => setMobileOpen((open) => !open)}
          className="text-gray-300 hover:text-croi-teal-bright lg:hidden"
          aria-label={mobileOpen ? 'Close menu' : 'Open menu'}
        >
          {mobileOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {mobileOpen && (
        <div className="border-t border-white/5 bg-croi-bg-dark px-4 pb-6 pt-2 lg:hidden">
          <nav className="flex flex-col gap-1">
            {NAV_LINKS.map((link) => {
              const isActive = activeSection === link.href.slice(1);
              return (
                <a
                  key={link.label}
                  href={link.href}
                  onClick={() => setMobileOpen(false)}
                  className={`rounded-lg px-3 py-2.5 text-sm font-medium transition-colors duration-200 hover:bg-croi-bg-card hover:text-croi-teal-bright ${
                    isActive ? 'text-croi-teal-bright' : 'text-gray-300'
                  }`}
                >
                  {link.label}
                </a>
              );
            })}
            <Link
              href="/auth/signup"
              onClick={() => setMobileOpen(false)}
              className="mt-2 rounded-lg bg-croi-gradient px-3 py-2.5 text-center text-sm font-semibold text-croi-bg-dark"
            >
              Get Started
            </Link>
          </nav>
        </div>
      )}
    </header>
  );
}
