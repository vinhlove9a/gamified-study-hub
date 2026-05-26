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
