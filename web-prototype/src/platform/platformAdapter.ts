import type { SimulatedApp } from '../types';

/**
 * Platform Adapter Abstraction
 * Isolates all device/OS capabilities so the UI layer never couples directly
 * to browser-only APIs and can cleanly bridge to native Android later.
 */
export interface PlatformAdapter {
  readonly isNativeAndroid: boolean;
  vibrateLight(enabled: boolean): void;
  vibrateSuccess(enabled: boolean): void;
  getInstalledApps(): SimulatedApp[];
  launchApp(app: SimulatedApp): { simulated: boolean; message: string };
  requestCoordinates(): Promise<{ lat: number; lng: number } | null>;
}

export const SIMULATED_APPS: SimulatedApp[] = [
  { id: 'phone', name: 'Phone', nameAr: 'الهاتف', category: 'communication', iconKey: 'phone', brandColor: '#22C55E' },
  { id: 'messages', name: 'Messages', nameAr: 'الرسائل', category: 'communication', iconKey: 'message', brandColor: '#3B82F6' },
  { id: 'whatsapp', name: 'WhatsApp', nameAr: 'واتساب', category: 'communication', iconKey: 'whatsapp', brandColor: '#25D366' },
  { id: 'telegram', name: 'Telegram', nameAr: 'تيليجرام', category: 'communication', iconKey: 'send', brandColor: '#2AABEE' },
  { id: 'camera', name: 'Camera', nameAr: 'الكاميرا', category: 'utility', iconKey: 'camera', brandColor: '#64748B' },
  { id: 'maps', name: 'Maps', nameAr: 'الخرائط', category: 'utility', iconKey: 'map', brandColor: '#10B981' },
  { id: 'calendar', name: 'Calendar', nameAr: 'التقويم', category: 'work', iconKey: 'calendar', brandColor: '#2563EB' },
  { id: 'mail', name: 'Mail', nameAr: 'البريد', category: 'work', iconKey: 'mail', brandColor: '#EA4335' },
  { id: 'chrome', name: 'Browser', nameAr: 'المتصفح', category: 'utility', iconKey: 'globe', brandColor: '#4285F4' },
  { id: 'files', name: 'Files', nameAr: 'الملفات', category: 'utility', iconKey: 'folder', brandColor: '#F59E0B' },
  { id: 'clock', name: 'Clock', nameAr: 'الساعة', category: 'utility', iconKey: 'clock', brandColor: '#6366F1' },
  { id: 'calculator', name: 'Calculator', nameAr: 'الآلة الحاسبة', category: 'utility', iconKey: 'calc', brandColor: '#475569' },
  { id: 'banking', name: 'Banking', nameAr: 'البنك', category: 'utility', iconKey: 'bank', brandColor: '#0D9488' },
  { id: 'notion', name: 'Notion', nameAr: 'نوشن', category: 'work', iconKey: 'file', brandColor: '#374151' },
  { id: 'spotify', name: 'Podcasts', nameAr: 'بودكاست', category: 'media', iconKey: 'headphones', brandColor: '#8B5CF6' },
  { id: 'youtube', name: 'YouTube', nameAr: 'يوتيوب', category: 'media', iconKey: 'play', brandColor: '#EF4444', defaultDistracting: true },
  { id: 'instagram', name: 'Instagram', nameAr: 'إنستغرام', category: 'social', iconKey: 'instagram', brandColor: '#E1306C', defaultDistracting: true },
  { id: 'tiktok', name: 'TikTok', nameAr: 'تيك توك', category: 'social', iconKey: 'music', brandColor: '#111827', defaultDistracting: true },
  { id: 'facebook', name: 'Facebook', nameAr: 'فيسبوك', category: 'social', iconKey: 'users', brandColor: '#1877F2', defaultDistracting: true },
  { id: 'x', name: 'X (Twitter)', nameAr: 'إكس', category: 'social', iconKey: 'at', brandColor: '#1F2937', defaultDistracting: true },
  { id: 'snapchat', name: 'Snapchat', nameAr: 'سناب شات', category: 'social', iconKey: 'ghost', brandColor: '#EAB308', defaultDistracting: true },
];

export const DEFAULT_FAVORITE_APP_IDS = [
  'phone',
  'whatsapp',
  'camera',
  'maps',
  'calendar',
  'youtube',
];

export const DEFAULT_DISTRACTING_APP_IDS = SIMULATED_APPS.filter(
  (a) => a.defaultDistracting
).map((a) => a.id);

export const webPlatformAdapter: PlatformAdapter = {
  isNativeAndroid: false,

  vibrateLight(enabled: boolean) {
    if (!enabled) return;
    try {
      if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
        navigator.vibrate(18);
      }
    } catch {
      // Ignore unsupported browsers
    }
  },

  vibrateSuccess(enabled: boolean) {
    if (!enabled) return;
    try {
      if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
        navigator.vibrate([35, 50, 55]);
      }
    } catch {
      // Ignore unsupported browsers
    }
  },

  getInstalledApps(): SimulatedApp[] {
    return SIMULATED_APPS;
  },

  launchApp(app: SimulatedApp) {
    return {
      simulated: true,
      message: app.name,
    };
  },

  requestCoordinates(): Promise<{ lat: number; lng: number } | null> {
    return new Promise((resolve) => {
      if (typeof navigator === 'undefined' || !navigator.geolocation) {
        resolve(null);
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) =>
          resolve({
            lat: pos.coords.latitude,
            lng: pos.coords.longitude,
          }),
        () => resolve(null),
        { timeout: 8000, maximumAge: 600000 }
      );
    });
  },
};
