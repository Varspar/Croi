import type { LucideIcon } from 'lucide-react';
import { Github, Linkedin, Twitter } from 'lucide-react';
import Logo from '@/components/Logo';

interface FooterLink {
  label: string;
  href?: string;
  comingSoon?: boolean;
}

const COMPANY_LINKS: FooterLink[] = [
  { label: 'About Croi', href: '#about' },
  { label: 'Blog', comingSoon: true },
  { label: 'Careers', comingSoon: true },
];

const PRODUCT_LINKS: FooterLink[] = [
  { label: 'Features', href: '#features' },
  { label: 'Pricing', href: '#pricing' },
  { label: 'Security', comingSoon: true },
];

const LEGAL_LINKS: FooterLink[] = [
  { label: 'Privacy Policy', comingSoon: true },
  { label: 'Terms of Service', comingSoon: true },
  { label: 'Contact', href: '#contact' },
];

// No real social accounts exist yet — rendered as visible but non-navigable
// rather than linking to a guessed URL that might belong to someone else.
const SOCIAL_LINKS: { label: string; icon: LucideIcon }[] = [
  { label: 'LinkedIn', icon: Linkedin },
  { label: 'Twitter', icon: Twitter },
  { label: 'GitHub', icon: Github },
];

function FooterLinkList({ title, links }: { title: string; links: FooterLink[] }) {
  return (
    <div>
      <p className="text-sm font-semibold text-croi-text-light">{title}</p>
      <ul className="mt-4 space-y-2">
        {links.map((link) => (
          <li key={link.label}>
            {link.comingSoon ? (
              <span className="text-sm text-gray-600" title="Coming soon">
                {link.label}
              </span>
            ) : (
              <a
                href={link.href}
                className="text-sm text-gray-400 transition-colors duration-200 hover:text-croi-teal-bright"
              >
                {link.label}
              </a>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default function Footer() {
  return (
    <footer className="border-t border-white/5 bg-croi-bg-dark px-4 py-12 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-6xl">
        <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-4">
          <div>
            <Logo size="small" showText />
            <p className="mt-4 max-w-xs text-sm text-gray-500">
              AI Customer Support Employee. Heart of your business.
            </p>
            <div className="mt-6 flex items-center gap-3">
              {SOCIAL_LINKS.map(({ label, icon: Icon }) => (
                <span
                  key={label}
                  title={`${label} (coming soon)`}
                  className="flex h-9 w-9 cursor-default items-center justify-center rounded-lg border border-white/10 text-gray-600"
                >
                  <Icon size={16} />
                </span>
              ))}
            </div>
          </div>

          <FooterLinkList title="Company" links={COMPANY_LINKS} />
          <FooterLinkList title="Product" links={PRODUCT_LINKS} />
          <FooterLinkList title="Legal" links={LEGAL_LINKS} />
        </div>

        <div className="mt-12 border-t border-white/5 pt-6">
          <p className="text-xs text-gray-500">
            &copy; {new Date().getFullYear()} Croi. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
}
