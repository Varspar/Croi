'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { Bell, LogOut } from 'lucide-react';

interface StoredUser {
  firstName?: string;
  lastName?: string;
}

function greetingForHour(hour: number): string {
  if (hour < 12) return 'Good morning';
  if (hour < 18) return 'Good afternoon';
  return 'Good evening';
}

export default function Header() {
  const router = useRouter();
  const [user, setUser] = useState<StoredUser | null>(null);
  const [greeting, setGreeting] = useState('Welcome');

  useEffect(() => {
    setGreeting(greetingForHour(new Date().getHours()));

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

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    router.push('/auth/login');
  };

  return (
    <header className="flex items-center justify-between border-b border-white/5 bg-croi-bg-dark px-6 py-4">
      <div className="hidden lg:block" />

      <p className="text-lg font-semibold text-croi-text-light">
        {greeting}
        {user?.firstName ? `, ${user.firstName}!` : '!'}
      </p>

      <div className="flex items-center gap-4">
        <button
          type="button"
          className="rounded-full p-2 text-gray-400 transition-colors duration-200 hover:bg-croi-bg-card hover:text-croi-teal-bright"
          aria-label="Notifications"
        >
          <Bell size={20} />
        </button>

        <div className="flex h-9 w-9 items-center justify-center rounded-full bg-croi-gradient text-sm font-semibold text-croi-bg-dark">
          {initials}
        </div>

        <button
          type="button"
          onClick={handleLogout}
          className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium text-gray-400 transition-colors duration-200 hover:bg-croi-bg-card hover:text-croi-text-light"
        >
          <LogOut size={18} />
          <span className="hidden sm:inline">Logout</span>
        </button>
      </div>
    </header>
  );
}
