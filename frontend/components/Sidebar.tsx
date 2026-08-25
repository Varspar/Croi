'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';
import {
  BarChart3,
  BookOpen,
  ChevronLeft,
  ChevronRight,
  Home,
  Menu,
  Phone,
  Plug,
  Settings,
  ShieldCheck,
  X,
} from 'lucide-react';
import Logo from './Logo';

interface NavItem {
  label: string;
  href: string;
  icon: typeof Home;
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Agents', href: '/dashboard/agents', icon: Phone },
  { label: 'Knowledge', href: '/dashboard/knowledge', icon: BookOpen },
  { label: 'Analytics', href: '/dashboard/analytics', icon: BarChart3 },
  { label: 'Integrations', href: '/dashboard/integrations', icon: Plug },
  { label: 'Settings', href: '/dashboard/settings', icon: Settings },
  { label: 'Admin', href: '/dashboard/admin', icon: ShieldCheck },
];

interface StoredUser {
  firstName?: string;
  lastName?: string;
  email?: string;
}

export default function Sidebar() {
  const pathname = usePathname();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [user, setUser] = useState<StoredUser | null>(null);

  useEffect(() => {
    const raw = localStorage.getItem('user');
    if (raw) {
      try {
        setUser(JSON.parse(raw));
      } catch {
        setUser(null);
      }
    }
  }, []);

  const initials = user?.firstName
    ? `${user.firstName[0]}${user.lastName?.[0] ?? ''}`.toUpperCase()
    : '?';

  const navList = (
    <nav className="flex-1 space-y-1 px-3">
      {NAV_ITEMS.map((item) => {
        const active = pathname === item.href;
        const Icon = item.icon;
        return (
          <Link
            key={item.label}
            href={item.href}
            onClick={() => setMobileOpen(false)}
            className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors duration-200 ${
              active
                ? 'bg-croi-teal-muted text-croi-teal-bright'
                : 'text-gray-400 hover:bg-croi-bg-card hover:text-croi-text-light'
            }`}
          >
            <Icon size={20} className="shrink-0" />
            {!collapsed && <span>{item.label}</span>}
          </Link>
        );
      })}
    </nav>
  );

  return (
    <>
      {/* Mobile top bar toggle */}
      <button
        type="button"
        onClick={() => setMobileOpen(true)}
        className="fixed left-4 top-4 z-30 rounded-lg bg-croi-bg-card p-2 text-croi-text-light lg:hidden"
        aria-label="Open navigation"
      >
        <Menu size={22} />
      </button>

      {/* Mobile overlay */}
      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/60 lg:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-50 flex h-full flex-col border-r border-white/5 bg-croi-bg-dark transition-all duration-200 lg:static lg:z-auto ${
          collapsed ? 'lg:w-20' : 'lg:w-64'
        } ${mobileOpen ? 'w-64 translate-x-0' : 'w-64 -translate-x-full lg:translate-x-0'}`}
      >
        <div className="flex items-center justify-between px-4 py-5">
          <Logo size="small" showText={!collapsed} />
          <button
            type="button"
            onClick={() => setMobileOpen(false)}
            className="text-gray-400 hover:text-croi-text-light lg:hidden"
            aria-label="Close navigation"
          >
            <X size={20} />
          </button>
        </div>

        {navList}

        <div className="border-t border-white/5 p-3">
          <button
            type="button"
            onClick={() => setCollapsed((c) => !c)}
            className="mb-2 hidden w-full items-center justify-center gap-2 rounded-lg px-3 py-2 text-xs text-gray-400 transition-colors duration-200 hover:bg-croi-bg-card hover:text-croi-text-light lg:flex"
          >
            {collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
            {!collapsed && <span>Collapse</span>}
          </button>

          <div className="flex items-center gap-3 rounded-lg px-2 py-2">
            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-croi-gradient text-sm font-semibold text-croi-bg-dark">
              {initials}
            </div>
            {!collapsed && (
              <div className="min-w-0">
                <p className="truncate text-sm font-medium text-croi-text-light">
                  {user?.firstName ? `${user.firstName} ${user.lastName ?? ''}` : 'Croi user'}
                </p>
                <p className="truncate text-xs text-gray-500">{user?.email ?? ''}</p>
              </div>
            )}
          </div>
        </div>
      </aside>
    </>
  );
}
