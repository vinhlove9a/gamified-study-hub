import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{vue,ts,js,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eefbf3',
          100: '#d7f5e2',
          500: '#1f9d55',
          700: '#15703d'
        }
      }
    }
  },
  plugins: []
} satisfies Config;
