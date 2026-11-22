/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        bg: 'var(--color-bg)',
        surface: 'var(--color-surface)',
        elevated: 'var(--color-elevated)',
        ink: 'var(--color-text-primary)',
        muted: 'var(--color-text-secondary)',
        subtle: 'var(--color-text-muted)',
        accent: 'var(--color-accent)',
        'accent-soft': 'var(--color-accent-soft)',
        border: 'var(--color-border)',
        'border-strong': 'var(--color-border-strong)',
        success: 'var(--color-success)',
        danger: 'var(--color-danger)',
        mushaf: 'var(--color-mushaf-bg)',
        'mushaf-ink': 'var(--color-mushaf-text)',
      },
      fontFamily: {
        app: ['var(--font-app)', 'system-ui', 'sans-serif'],
        amiri: ['"Amiri"', 'serif'],
        ibm: ['"IBM Plex Sans Arabic"', 'system-ui', 'sans-serif'],
        tajawal: ['"Tajawal"', 'system-ui', 'sans-serif'],
        almarai: ['"Almarai"', 'system-ui', 'sans-serif'],
        system: ['-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', 'sans-serif'],
      },
      spacing: {
        'page-x': '20px',
      },
      borderRadius: {
        sm: '8px',
        md: '12px',
        lg: '16px',
        xl: '20px',
        pill: '9999px',
      },
    },
  },
  plugins: [],
};
