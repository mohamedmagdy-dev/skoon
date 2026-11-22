import type { AppFontId } from '../types';

export interface FontOption {
  id: AppFontId;
  nameAr: string;
  nameEn: string;
  cssFamily: string;
  sampleAr: string;
  sampleEn: string;
}

export const FONT_OPTIONS: FontOption[] = [
  {
    id: 'ibm',
    nameAr: 'IBM Plex',
    nameEn: 'IBM Plex Arabic',
    cssFamily: '"IBM Plex Sans Arabic", -apple-system, BlinkMacSystemFont, sans-serif',
    sampleAr: 'أقل ضجيجاً. أكثر معنى.',
    sampleEn: 'Less noise. More meaning.',
  },
  {
    id: 'tajawal',
    nameAr: 'تجوال',
    nameEn: 'Tajawal',
    cssFamily: '"Tajawal", -apple-system, BlinkMacSystemFont, sans-serif',
    sampleAr: 'أقل ضجيجاً. أكثر معنى.',
    sampleEn: 'Less noise. More meaning.',
  },
  {
    id: 'almarai',
    nameAr: 'المراعي',
    nameEn: 'Almarai',
    cssFamily: '"Almarai", -apple-system, BlinkMacSystemFont, sans-serif',
    sampleAr: 'أقل ضجيجاً. أكثر معنى.',
    sampleEn: 'Less noise. More meaning.',
  },
  {
    id: 'amiri',
    nameAr: 'أميري (نسخ)',
    nameEn: 'Amiri (Serif)',
    cssFamily: '"Amiri", Georgia, serif',
    sampleAr: 'أقل ضجيجاً. أكثر معنى.',
    sampleEn: 'Less noise. More meaning.',
  },
  {
    id: 'system',
    nameAr: 'خط النظام',
    nameEn: 'System Sans',
    cssFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    sampleAr: 'أقل ضجيجاً. أكثر معنى.',
    sampleEn: 'Less noise. More meaning.',
  },
];

export function getFontOption(id: AppFontId): FontOption {
  return FONT_OPTIONS.find((f) => f.id === id) ?? FONT_OPTIONS[0];
}
