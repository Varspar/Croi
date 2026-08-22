'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Loader2 } from 'lucide-react';
import Logo from '@/components/Logo';
import apiClient from '@/lib/api';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await apiClient.post('/auth/login', {
        email,
        password,
      });

      const { token, user } = response.data.data;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      router.push('/dashboard/chat');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md rounded-2xl border border-white/10 bg-croi-bg-card p-8 shadow-xl">
      <div className="mb-6 flex justify-center">
        <Logo size="large" />
      </div>
      <h1 className="mb-2 text-center text-2xl font-bold text-croi-text-light">Welcome Back</h1>
      <p className="mb-8 text-center text-sm text-gray-400">Sign in to your Croi account</p>

      {error && (
        <div className="mb-6 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-400">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-2 block text-sm font-medium text-gray-400">Email Address</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            required
            className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-gray-400">Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            required
            className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="flex w-full items-center justify-center gap-2 rounded-lg bg-croi-gradient px-4 py-2.5 font-semibold text-croi-bg-dark transition-opacity duration-200 disabled:opacity-50"
        >
          {loading && <Loader2 size={18} className="animate-spin" />}
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-400">
        Don't have an account?{' '}
        <Link href="/auth/signup" className="font-semibold text-croi-teal-bright hover:text-croi-teal-dark">
          Sign up
        </Link>
      </p>
    </div>
  );
}
