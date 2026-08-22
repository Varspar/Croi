'use client';

import { useState } from 'react';
import axios from 'axios';
import { useGoogleReCaptcha } from 'react-google-recaptcha-v3';
import { Loader2, Mail, MapPin, Phone, RefreshCw } from 'lucide-react';
import { submitContactForm } from '@/services/contactService';
import type { ContactRequest } from '@/types';

const EMPTY_FORM: Omit<ContactRequest, 'recaptchaToken'> = {
  firstName: '',
  lastName: '',
  companyName: '',
  email: '',
  phone: '',
  message: '',
};

// TODO: replace with a real support inbox once one exists.
const PLACEHOLDER_EMAIL = 'hello@croi.ai';

export default function ContactSection() {
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { executeRecaptcha } = useGoogleReCaptcha();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // executeRecaptcha is undefined until the reCAPTCHA script has finished
    // loading — guard rather than call it and crash.
    if (!executeRecaptcha) {
      setError('Still loading spam protection — please try again in a moment.');
      return;
    }

    setSubmitting(true);

    try {
      // Tokens are single-use and short-lived, so generate one fresh right
      // before submitting rather than storing it in component state.
      const recaptchaToken = await executeRecaptcha('contact_form');
      const payload: ContactRequest = { ...form, recaptchaToken };

      const message = await submitContactForm(payload);
      setSuccessMessage(message);
      setForm(EMPTY_FORM);
    } catch (err) {
      // Keep the form filled in so the visitor doesn't lose what they typed.
      const message = axios.isAxiosError(err)
        ? err.response?.data?.message ?? 'Something went wrong. Please try again.'
        : 'Something went wrong. Please try again.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section id="contact" className="scroll-mt-24 px-4 py-20 sm:px-6 sm:py-28 lg:px-8">
      <div className="mx-auto max-w-6xl">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-3xl font-bold text-croi-text-light sm:text-4xl">Get in touch</h2>
          <p className="mt-4 text-gray-400">
            Questions about Croi, or want a walkthrough for your team? Send us a message.
          </p>
        </div>

        <div className="mt-12 grid grid-cols-1 gap-8 lg:grid-cols-5">
          <div className="rounded-2xl border border-white/10 bg-croi-bg-card p-8 lg:col-span-3">
            {successMessage ? (
              <div className="flex flex-col items-center gap-3 py-8 text-center">
                <Mail size={32} className="text-croi-teal-bright" />
                <p className="font-semibold text-croi-text-light">{successMessage}</p>
                <button
                  type="button"
                  onClick={() => setSuccessMessage(null)}
                  className="mt-2 text-sm text-croi-teal-bright hover:text-croi-teal-dark"
                >
                  Send another message
                </button>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <div>
                    <label className="mb-2 block text-sm font-medium text-gray-400">
                      First Name
                    </label>
                    <input
                      type="text"
                      name="firstName"
                      value={form.firstName}
                      onChange={handleChange}
                      required
                      className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
                    />
                  </div>
                  <div>
                    <label className="mb-2 block text-sm font-medium text-gray-400">
                      Last Name
                    </label>
                    <input
                      type="text"
                      name="lastName"
                      value={form.lastName}
                      onChange={handleChange}
                      required
                      className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
                    />
                  </div>
                </div>

                <div>
                  <label className="mb-2 block text-sm font-medium text-gray-400">
                    Company Name
                  </label>
                  <input
                    type="text"
                    name="companyName"
                    value={form.companyName}
                    onChange={handleChange}
                    required
                    className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
                  />
                </div>

                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <div>
                    <label className="mb-2 block text-sm font-medium text-gray-400">Email</label>
                    <input
                      type="email"
                      name="email"
                      value={form.email}
                      onChange={handleChange}
                      required
                      className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
                    />
                  </div>
                  <div>
                    <label className="mb-2 block text-sm font-medium text-gray-400">
                      Phone <span className="text-gray-600">(optional)</span>
                    </label>
                    <input
                      type="tel"
                      name="phone"
                      value={form.phone}
                      onChange={handleChange}
                      className="w-full rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
                    />
                  </div>
                </div>

                <div>
                  <label className="mb-2 block text-sm font-medium text-gray-400">Message</label>
                  <textarea
                    name="message"
                    value={form.message}
                    onChange={handleChange}
                    required
                    rows={4}
                    className="w-full resize-none rounded-lg border border-white/10 bg-croi-bg-dark px-4 py-2 text-croi-text-light placeholder:text-gray-500 outline-none transition-colors duration-200 focus:border-croi-teal-bright"
                  />
                </div>

                {error && (
                  <div className="flex items-center justify-between gap-4 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-400">
                    <span>{error}</span>
                    <button
                      type="submit"
                      className="flex shrink-0 items-center gap-1.5 rounded-md bg-red-500/20 px-3 py-1.5 font-medium text-red-300 transition-colors duration-200 hover:bg-red-500/30"
                    >
                      <RefreshCw size={14} />
                      Retry
                    </button>
                  </div>
                )}

                <button
                  type="submit"
                  disabled={submitting}
                  className="flex w-full items-center justify-center gap-2 rounded-lg bg-croi-gradient px-4 py-2.5 font-semibold text-croi-bg-dark transition-opacity duration-200 disabled:opacity-50"
                >
                  {submitting && <Loader2 size={18} className="animate-spin" />}
                  {submitting ? 'Sending...' : 'Send Message'}
                </button>
              </form>
            )}
          </div>

          <div className="flex flex-col justify-center gap-6 lg:col-span-2">
            <div className="flex items-start gap-4 rounded-xl border border-white/10 bg-croi-bg-card p-5">
              <Mail size={20} className="mt-0.5 shrink-0 text-croi-teal-bright" />
              <div>
                <p className="text-sm font-semibold text-croi-text-light">Email</p>
                <p className="mt-1 text-sm text-gray-400">{PLACEHOLDER_EMAIL}</p>
              </div>
            </div>

            <div className="flex items-start gap-4 rounded-xl border border-white/10 bg-croi-bg-card p-5">
              <Phone size={20} className="mt-0.5 shrink-0 text-croi-teal-bright" />
              <div>
                <p className="text-sm font-semibold text-croi-text-light">Phone</p>
                <p className="mt-1 text-sm text-gray-400">
                  Prefer a call? Mention it in your message and we&apos;ll set one up.
                </p>
              </div>
            </div>

            <div className="flex items-start gap-4 rounded-xl border border-white/10 bg-croi-bg-card p-5">
              <MapPin size={20} className="mt-0.5 shrink-0 text-croi-teal-bright" />
              <div>
                <p className="text-sm font-semibold text-croi-text-light">Location</p>
                <p className="mt-1 text-sm text-gray-400">Remote-first, serving customers worldwide.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
