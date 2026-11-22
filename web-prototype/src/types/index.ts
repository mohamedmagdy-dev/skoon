export type ThemeMode = 'dark' | 'light';
export type AppLanguage = 'ar' | 'en';
export type AppFontId = 'ibm' | 'tajawal' | 'almarai' | 'amiri' | 'system';
export type AppDisplayStyle = 'text' | 'monochrome' | 'original';

export type TabId =
  | 'pomodoro'
  | 'notes'
  | 'habits'
  | 'home'
  | 'quran'
  | 'azkar'
  | 'tasbih';

export type OnboardingStatus = 'not_started' | 'in_progress' | 'completed';

export interface TabConfig {
  id: TabId;
  enabled: boolean;
}

export interface SimulatedApp {
  id: string;
  name: string;
  nameAr?: string;
  category: 'communication' | 'utility' | 'social' | 'media' | 'work';
  iconKey: string;
  brandColor: string;
  defaultDistracting?: boolean;
}

export interface HabitItem {
  id: string;
  text: string;
  /** ISO dates 'YYYY-MM-DD' on which this habit was completed */
  completedDates: string[];
  createdAt: string;
}

export interface NoteItem {
  id: string;
  title: string;
  body: string;
  pinned: boolean;
  updatedAt: number;
}

export interface DhikrItem {
  id: string;
  text: string;
  short: string;
  target: number;
  count: number;
  custom?: boolean;
}

export interface ZikrCard {
  id: string;
  title: string;
  text: string;
  target: number;
  count: number;
}

export interface QuranAyah {
  n: number;
  t: string;
  sn?: string;
  sb?: boolean;
}

export interface QuranPage {
  p: number;
  j: number;
  s: string;
  a: QuranAyah[];
}

export type PomodoroModeKey = 'focus' | 'short' | 'long';

export interface PomodoroState {
  mode: PomodoroModeKey;
  durationSeconds: number;
  remainingSeconds: number;
  isRunning: boolean;
  /** Epoch ms when the current running session is scheduled to end */
  endTimeMs: number | null;
  task: string;
  sessionsToday: number;
  lastSessionDate: string;
  autoFocus: boolean;
}

export interface VolumeSimulation {
  media: number;
  ring: number;
  alarm: number;
  muted: boolean;
}

export type ActiveOverlay =
  | null
  | 'app-drawer'
  | 'search'
  | 'settings';

export type BlackScreenSource = null | 'azkar' | 'tasbih';

export interface SukoonPersistedState {
  schemaVersion: number;
  onboardingStatus: OnboardingStatus;
  onboardingStep: number;

  theme: ThemeMode;
  language: AppLanguage;
  fontId: AppFontId;
  appDisplayStyle: AppDisplayStyle;
  prayerCityId: string;

  tabs: TabConfig[];

  focusMode: boolean;
  mindfulPauseSeconds: number;
  hapticsEnabled: boolean;

  favoriteAppIds: string[];
  distractingAppIds: string[];

  quranPage: number;
  quranFontOffset: number;
  quranLineHeight: number;
  quranPaperMode: boolean;

  morningAzkar: ZikrCard[];
  eveningAzkar: ZikrCard[];
  morningAzkarIndex: number;
  eveningAzkarIndex: number;
  lastAzkarDate: string;

  adhkar: DhikrItem[];
  activeDhikrId: string;

  habits: HabitItem[];
  lastHabitDate: string;

  notes: NoteItem[];
  pomodoro: PomodoroState;
  volumes: VolumeSimulation;
}
