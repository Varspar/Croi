'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { GoogleReCaptchaProvider } from 'react-google-recaptcha-v3';
import Navigation from '@/components/landing/Navigation';
import HeroSection from '@/components/landing/HeroSection';
import FeaturesSection from '@/components/landing/FeaturesSection';
import AboutSection from '@/components/landing/AboutSection';
import PricingSection from '@/components/landing/PricingSection';
import ContactSection from '@/components/landing/ContactSection';
import Footer from '@/components/landing/Footer';

export default function Home() {
  const router = useRouter();

  // Signed-in visitors skip the marketing page and go straight to the app;
  // everyone else sees the landing page normally (no forced redirect to login).
  useEffect(() => {
    if (localStorage.getItem('token')) {
      router.push('/dashboard/agents');
    }
  }, [router]);

  return (
    <div className="min-h-screen bg-croi-bg-dark">
      <Navigation />
      <main>
        <HeroSection />
        <FeaturesSection />
        <AboutSection />
        <PricingSection />
        <GoogleReCaptchaProvider reCaptchaKey={process.env.NEXT_PUBLIC_RECAPTCHA_SITE_KEY ?? ''}>
          <ContactSection />
        </GoogleReCaptchaProvider>
      </main>
      <Footer />
    </div>
  );
}
