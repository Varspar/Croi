import type { Config } from 'tailwindcss';

const config: Config = {
  darkMode: 'class',
  content: [
    './app/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        croi: {
          'teal-bright': '#00E6B5',
          'teal-dark': '#008BA0',
          'teal-muted': '#0A3D3A',
          'bg-dark': '#0F1720',
          'bg-card': '#1E232B',
          'text-light': '#FFFFFF',
        },
      },
      backgroundImage: {
        'croi-gradient': 'linear-gradient(135deg, #00E6B5 0%, #008BA0 100%)',
      },
      transitionDuration: {
        DEFAULT: '200ms',
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
};

export default config;
