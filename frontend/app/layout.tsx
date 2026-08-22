import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'Croi — AI Customer Support Employee',
  description:
    "Croi is an AI customer support employee that gives your business 24/7, scalable customer support. Heart of your business.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}