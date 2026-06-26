import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{vue,ts,js,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        gsh: {
          bg: '#FFFFFF',
          'bg-alt': '#F7F8FA',
          'bg-muted': '#F3F4F6',
          text: '#111827',
          secondary: '#4B5563',
          muted: '#6B7280',
          border: '#E5E7EB',
          accent: '#2563EB',
          'accent-hover': '#1D4ED8',
          'accent-soft': '#EFF6FF',
          purple: '#7C3AED',
          amber: '#F59E0B',
          success: '#059669'
        },
        // Deep Space dashboard tokens (ported from deep-workspace).
        // Hardcoded oklch with the <alpha-value> placeholder so opacity modifiers
        // like `bg-background/60` resolve to `oklch(... / 0.6)` — Tailwind v3 cannot
        // derive alpha from a `var()`-based color, hence literal values here.
        background: 'oklch(0.08 0.02 280 / <alpha-value>)',
        foreground: 'oklch(0.96 0.01 240 / <alpha-value>)',
        card: {
          DEFAULT: 'oklch(0.13 0.025 280 / <alpha-value>)',
          foreground: 'oklch(0.96 0.01 240 / <alpha-value>)'
        },
        muted: {
          DEFAULT: 'oklch(0.18 0.03 280 / <alpha-value>)',
          foreground: 'oklch(0.7 0.03 240 / <alpha-value>)'
        }
      },
      fontFamily: {
        sans: ['"Be Vietnam Pro"', '-apple-system', 'BlinkMacSystemFont', 'ui-sans-serif', 'system-ui'],
        display: ['"Be Vietnam Pro"', '-apple-system', 'BlinkMacSystemFont', 'ui-sans-serif', 'system-ui']
      },
      fontSize: {
        'hero': ['clamp(2.75rem, 5.5vw, 5rem)', { lineHeight: '1.06', letterSpacing: '-0.025em', fontWeight: '800' }],
        'display-lg': ['clamp(2rem, 3.8vw, 3.25rem)', { lineHeight: '1.1', letterSpacing: '-0.02em', fontWeight: '700' }],
        'display': ['clamp(1.5rem, 2.5vw, 2.25rem)', { lineHeight: '1.15', letterSpacing: '-0.015em', fontWeight: '700' }],
        'body-lg': ['1.125rem', { lineHeight: '1.65', fontWeight: '400' }],
        'body': ['1rem', { lineHeight: '1.6', fontWeight: '400' }],
        'caption': ['0.8125rem', { lineHeight: '1.5', fontWeight: '400' }]
      },
      boxShadow: {
        'mockup': '0 30px 80px rgba(0, 0, 0, 0.12), 0 0 1px rgba(0, 0, 0, 0.08)'
      }
    }
  },
  plugins: []
} satisfies Config;
