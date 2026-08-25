'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Loader2 } from 'lucide-react';
import Logo from '@/components/Logo';
import apiClient from '@/lib/api';

export default function SignupPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (!formData.email || !formData.password || !formData.firstName || !formData.lastName) {
      setError('All fields are required');
      setLoading(false);
      return;
    }

    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters');
      setLoading(false);
      return;
    }

    try {
      const response = await apiClient.post('/auth/signup', formData);
      const { token, user } = response.data.data;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      router.push('/dashboard/agents');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Signup failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md rounded-2xl border border-white/10 bg-croi-bg-card p-8 shadow-xl">
      <div className="mb-6 flex justify-center">
        <Logo size="large" />
      </div>
      <h1 className="mb-2 text-center text-2xl font-bold text-croi-text-light">Create Account</h1>
      <p className="mb-8 text-center text-sm text-gray-400">
        Join Croi and start automating customer support
      </p>

      {error && (
        <div className="mb-6 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-400">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-2 block text-sm font-medium text-gray-400">First Name</label>
          <input
            type="text"
            name="firstName"
            value={formData.firstName}
            onChange={handleChange}
            placeholder="John"
            required
            className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-gray-400">Last Name</label>
          <input
            type="text"
            name="lastName"
            value={formData.lastName}
            onChange={handleChange}
            placeholder="Doe"
            required
            className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-gray-400">Email Address</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="you@example.com"
            required
            className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
          />
        </div>

        <div>
          <label className="mb-2 block text-sm font-medium text-gray-400">Password</label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
            required
            className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
          />
          <p className="mt-1 text-xs text-gray-500">At least 8 characters</p>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="flex w-full items-center justify-center gap-2 rounded-lg bg-croi-gradient px-4 py-2.5 font-semibold text-croi-bg-dark transition-opacity duration-200 disabled:opacity-50"
        >
          {loading && <Loader2 size={18} className="animate-spin" />}
          {loading ? 'Creating account...' : 'Create Account'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-gray-400">
        Already have an account?{' '}
        <Link href="/auth/login" className="font-semibold text-croi-teal-bright hover:text-croi-teal-dark">
          Sign in
        </Link>
      </p>
    </div>
  );
}
