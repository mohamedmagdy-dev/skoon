import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type {
  ActiveOverlay,
  AppDisplayStyle,
  AppFontId,
  AppLanguage,
  BlackScreenSource,
  DhikrItem,
  HabitItem,
  NoteItem,
  OnboardingStatus,
  PomodoroModeKey,
  SimulatedApp,
  SukoonPersistedState,
  TabConfig,
  TabId,
  ThemeMode,
} from '../types';
import {
  createDefaultAdhkar,
  createDefaultEveningAzkar,
  createDefaultMorningAzkar,
} from '../services/azkarData';
import {
  DEFAULT_DISTRACTING_APP_IDS,
  DEFAULT_FAVORITE_APP_IDS,
  webPlatformAdapter,
} from '../platform/platformAdapter';
import { getDict } from '../design-system/i18n';
import { findCity } from '../services/prayerTimes';

export const SCHEMA_VERSION = 2;
const STORAGE_KEY = 'sukoon_web_state_v2';

export function getTodayIso(offsetDays = 0): string {
  const d = new Date();
  d.setDate(d.getDate() + offsetDays);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export function calculateHabitStreak(completedDates: string[], todayIso: string): number {
  const set = new Set(completedDates);
  let streak = 0;
  let cursor = set.has(todayIso) ? 0 : -1;
  while (true) {
    const iso = getTodayIso(cursor);
    if (set.has(iso)) {
      streak += 1;
      cursor -= 1;
    } else {
      break;
    }
  }
  return streak;
}

export function defaultTabs(): TabConfig[] {
  return [
    { id: 'pomodoro', enabled: true },
    { id: 'notes', enabled: true },
    { id: 'habits', enabled: true },
    { id: 'home', enabled: true },
    { id: 'quran', enabled: true },
    { id: 'azkar', enabled: true },
    { id: 'tasbih', enabled: true },
  ];
}

function defaultHabits(today: string): HabitItem[] {
  return [
    { id: 'h_fajr', text: 'صلاة الفجر في وقتها', completedDates: [], createdAt: today },
    { id: 'h_quran', text: 'قراءة الورد اليومي من القرآن', completedDates: [], createdAt: today },
    { id: 'h_azkar', text: 'أذكار الصباح والمساء', completedDates: [], createdAt: today },
    { id: 'h_walk', text: 'مشي أو تمرين ٣٠ دقيقة', completedDates: [], createdAt: today },
  ];
}

function defaultNotes(): NoteItem[] {
  return [
    {
      id: 'n_welcome',
      title: 'مرحباً في سكون',
      body: 'مساحتك الشخصية الهادئة.\n• اسحب يميناً أو يساراً للتنقل بين التابات\n• اسحب لأعلى لدرج التطبيقات، ولأسفل للبحث السريع\n• يمكنك تخصيص اللغة والخط وترتيب التابات من الإعدادات',
      pinned: true,
      updatedAt: Date.now(),
    },
  ];
}

export function createInitialPersistedState(): SukoonPersistedState {
  const today = getTodayIso();
  return {
    schemaVersion: SCHEMA_VERSION,
    onboardingStatus: 'not_started',
    onboardingStep: 0,

    theme: 'dark',
    language: 'ar',
    fontId: 'ibm',
    appDisplayStyle: 'text',
    prayerCityId: 'alexandria',

    tabs: defaultTabs(),

    focusMode: false,
    mindfulPauseSeconds: 5,
    hapticsEnabled: true,

    favoriteAppIds: [...DEFAULT_FAVORITE_APP_IDS],
    distractingAppIds: [...DEFAULT_DISTRACTING_APP_IDS],

    quranPage: 1,
    quranFontOffset: 0,
    quranLineHeight: 2.15,
    quranPaperMode: false,

    morningAzkar: createDefaultMorningAzkar(),
    eveningAzkar: createDefaultEveningAzkar(),
    morningAzkarIndex: 0,
    eveningAzkarIndex: 0,
    lastAzkarDate: today,

    adhkar: createDefaultAdhkar(),
    activeDhikrId: 'subhan',

    habits: defaultHabits(today),
    lastHabitDate: today,

    notes: defaultNotes(),
    pomodoro: {
      mode: 'focus',
      durationSeconds: 25 * 60,
      remainingSeconds: 25 * 60,
      isRunning: false,
      endTimeMs: null,
      task: '',
      sessionsToday: 0,
      lastSessionDate: today,
      autoFocus: true,
    },
    volumes: {
      media: 60,
      ring: 70,
      alarm: 85,
      muted: false,
    },
  };
}

interface SukoonStore extends SukoonPersistedState {
  // Ephemeral UI state (not persisted)
  activeTabId: TabId;
  activeOverlay: ActiveOverlay;
  mindfulTargetApp: SimulatedApp | null;
  blackScreenSource: BlackScreenSource;
  toastMessage: string | null;

  // Onboarding actions
  setOnboardingStatus: (status: OnboardingStatus) => void;
  setOnboardingStep: (step: number) => void;
  completeOnboarding: () => void;
  restartSetup: () => void;
  resetApp: () => void;

  // Navigation & overlays
  setActiveTab: (id: TabId) => void;
  setActiveOverlay: (overlay: ActiveOverlay) => void;
  setBlackScreenSource: (source: BlackScreenSource) => void;
  showToast: (msg: string) => void;

  // Preferences
  setTheme: (theme: ThemeMode) => void;
  setLanguage: (lang: AppLanguage) => void;
  setFontId: (fontId: AppFontId) => void;
  setAppDisplayStyle: (style: AppDisplayStyle) => void;
  setPrayerCityId: (cityId: string, silent?: boolean) => void;
  setMindfulPauseSeconds: (sec: number) => void;
  setHapticsEnabled: (enabled: boolean) => void;
  toggleFocusMode: (force?: boolean) => void;

  // Tabs customization
  toggleTabEnabled: (tabId: TabId) => void;
  reorderTabs: (fromIndex: number, toIndex: number) => void;
  setTabs: (tabs: TabConfig[]) => void;

  // Apps & Mindful Pause
  openApp: (app: SimulatedApp) => void;
  dismissMindfulPause: () => void;
  proceedMindfulApp: () => void;
  toggleFavoriteApp: (appId: string) => void;
  toggleDistractingApp: (appId: string) => void;

  // Quran
  goToQuranPage: (page: number) => void;
  adjustQuranFontOffset: (delta: number) => void;
  setQuranLineHeight: (lh: number) => void;
  toggleQuranPaperMode: () => void;

  // Azkar
  selectAzkarIndex: (isMorning: boolean, idx: number) => void;
  incrementAzkar: (isMorning: boolean) => void;
  resetAzkar: (isMorning: boolean) => void;

  // Tasbih / Dhikr
  selectDhikr: (id: string) => void;
  incrementDhikr: () => void;
  adjustActiveDhikrTarget: (delta: number) => void;
  resetActiveDhikr: () => void;
  addCustomDhikr: (text: string, target: number) => void;
  deleteActiveDhikr: () => void;

  // Habits
  toggleHabitToday: (id: string) => void;
  addHabit: (text: string) => void;
  deleteHabit: (id: string) => void;
  triggerNewDay: () => void;
  checkDailyRollover: () => void;

  // Notes
  createNote: () => string;
  updateNote: (id: string, title: string, body: string) => void;
  toggleNotePin: (id: string) => void;
  deleteNote: (id: string) => void;

  // Pomodoro
  setPomodoroMode: (mode: PomodoroModeKey, minutes: number) => void;
  startOrResumePomodoro: () => void;
  pausePomodoro: () => void;
  resetPomodoro: () => void;
  syncPomodoroTick: () => void;
  setPomodoroTask: (task: string) => void;
  setPomodoroAutoFocus: (enabled: boolean) => void;

  // Volume simulation
  setVolume: (channel: 'media' | 'ring' | 'alarm', value: number) => void;
  toggleMuteAll: () => void;
}

let toastTimer: ReturnType<typeof setTimeout> | null = null;

export const useSukoonStore = create<SukoonStore>()(
  persist(
    (set, get) => ({
      ...createInitialPersistedState(),

      activeTabId: 'home',
      activeOverlay: null,
      mindfulTargetApp: null,
      blackScreenSource: null,
      toastMessage: null,

      // ===== Onboarding =====
      setOnboardingStatus: (status) => set({ onboardingStatus: status }),
      setOnboardingStep: (step) =>
        set((s) => ({
          onboardingStep: step,
          onboardingStatus:
            s.onboardingStatus === 'not_started' && step > 0
              ? 'in_progress'
              : s.onboardingStatus,
        })),
      completeOnboarding: () =>
        set({
          onboardingStatus: 'completed',
          onboardingStep: 0,
          activeTabId: 'home',
        }),
      restartSetup: () =>
        set({
          onboardingStatus: 'in_progress',
          onboardingStep: 1, // Welcome screen
          activeOverlay: null,
        }),
      resetApp: () => {
        const fresh = createInitialPersistedState();
        set({
          ...fresh,
          activeTabId: 'home',
          activeOverlay: null,
          mindfulTargetApp: null,
          blackScreenSource: null,
        });
      },

      // ===== Navigation & Overlays =====
      setActiveTab: (id) => set({ activeTabId: id }),
      setActiveOverlay: (overlay) => set({ activeOverlay: overlay }),
      setBlackScreenSource: (source) => set({ blackScreenSource: source }),
      showToast: (msg) => {
        if (toastTimer) clearTimeout(toastTimer);
        set({ toastMessage: msg });
        toastTimer = setTimeout(() => {
          set({ toastMessage: null });
        }, 2300);
      },

      // ===== Preferences =====
      setTheme: (theme) => set({ theme }),
      setLanguage: (language) => set({ language }),
      setFontId: (fontId) => set({ fontId }),
      setAppDisplayStyle: (appDisplayStyle) => set({ appDisplayStyle }),
      setPrayerCityId: (cityId, silent = false) => {
        const city = findCity(cityId);
        set({ prayerCityId: city.id });
        if (!silent) {
          const d = getDict(get().language);
          get().showToast(d.toastCityUpdated(d.isArabic ? city.nameAr : city.nameEn));
        }
      },
      setMindfulPauseSeconds: (sec) =>
        set({ mindfulPauseSeconds: Math.max(3, Math.min(15, sec)) }),
      setHapticsEnabled: (enabled) => set({ hapticsEnabled: enabled }),
      toggleFocusMode: (force) => {
        const next = force ?? !get().focusMode;
        set({ focusMode: next });
        const d = getDict(get().language);
        get().showToast(next ? d.toastFocusOn : d.toastFocusOff);
      },

      // ===== Tabs =====
      toggleTabEnabled: (tabId) => {
        if (tabId === 'home') return;
        set((s) => {
          const tabs = s.tabs.map((t) =>
            t.id === tabId ? { ...t, enabled: !t.enabled } : t
          );
          const stillVisible = tabs.some((t) => t.id === s.activeTabId && t.enabled);
          return {
            tabs,
            activeTabId: stillVisible ? s.activeTabId : 'home',
          };
        });
      },
      reorderTabs: (fromIndex, toIndex) => {
        set((s) => {
          const list = [...s.tabs];
          if (
            fromIndex < 0 ||
            toIndex < 0 ||
            fromIndex >= list.length ||
            toIndex >= list.length
          ) {
            return s;
          }
          const [moved] = list.splice(fromIndex, 1);
          list.splice(toIndex, 0, moved);
          return { tabs: list };
        });
      },
      setTabs: (tabs) => set({ tabs }),

      // ===== Apps & Mindful Pause =====
      openApp: (app) => {
        const s = get();
        if (s.distractingAppIds.includes(app.id)) {
          set({ mindfulTargetApp: app, activeOverlay: null });
        } else {
          webPlatformAdapter.launchApp(app);
          const d = getDict(s.language);
          const label = d.isArabic && app.nameAr ? app.nameAr : app.name;
          s.showToast(d.toastOpenedApp(label));
          set({ activeOverlay: null });
        }
      },
      dismissMindfulPause: () => {
        const d = getDict(get().language);
        set({ mindfulTargetApp: null });
        get().showToast(d.toastStayedCalm);
      },
      proceedMindfulApp: () => {
        const app = get().mindfulTargetApp;
        if (!app) return;
        webPlatformAdapter.launchApp(app);
        const d = getDict(get().language);
        const label = d.isArabic && app.nameAr ? app.nameAr : app.name;
        set({ mindfulTargetApp: null });
        get().showToast(d.toastOpenedApp(label));
      },
      toggleFavoriteApp: (appId) =>
        set((s) => ({
          favoriteAppIds: s.favoriteAppIds.includes(appId)
            ? s.favoriteAppIds.filter((id) => id !== appId)
            : [...s.favoriteAppIds, appId],
        })),
      toggleDistractingApp: (appId) =>
        set((s) => ({
          distractingAppIds: s.distractingAppIds.includes(appId)
            ? s.distractingAppIds.filter((id) => id !== appId)
            : [...s.distractingAppIds, appId],
        })),

      // ===== Quran =====
      goToQuranPage: (page) =>
        set({ quranPage: Math.max(1, Math.min(604, Math.floor(page))) }),
      adjustQuranFontOffset: (delta) =>
        set((s) => ({
          quranFontOffset: Math.max(-5, Math.min(10, s.quranFontOffset + delta)),
        })),
      setQuranLineHeight: (lh) =>
        set({ quranLineHeight: Math.max(1.6, Math.min(2.9, lh)) }),
      toggleQuranPaperMode: () =>
        set((s) => ({ quranPaperMode: !s.quranPaperMode })),

      // ===== Azkar =====
      selectAzkarIndex: (isMorning, idx) =>
        set((s) => {
          const list = isMorning ? s.morningAzkar : s.eveningAzkar;
          const clamped = Math.max(0, Math.min(list.length - 1, idx));
          return isMorning
            ? { morningAzkarIndex: clamped }
            : { eveningAzkarIndex: clamped };
        }),
      incrementAzkar: (isMorning) => {
        const s = get();
        const list = isMorning ? s.morningAzkar : s.eveningAzkar;
        const idx = isMorning ? s.morningAzkarIndex : s.eveningAzkarIndex;
        const item = list[idx];
        if (!item) return;

        if (item.count < item.target) {
          const newCount = item.count + 1;
          const updated = list.map((z, i) =>
            i === idx ? { ...z, count: newCount } : z
          );
          set(isMorning ? { morningAzkar: updated } : { eveningAzkar: updated });

          if (newCount >= item.target) {
            webPlatformAdapter.vibrateSuccess(s.hapticsEnabled);
            const nextIdx =
              updated.findIndex((z, i) => i > idx && z.count < z.target) !== -1
                ? updated.findIndex((z, i) => i > idx && z.count < z.target)
                : updated.findIndex((z) => z.count < z.target);
            if (nextIdx !== -1 && nextIdx !== idx) {
              setTimeout(() => get().selectAzkarIndex(isMorning, nextIdx), 380);
            } else if (updated.every((z) => z.count >= z.target)) {
              get().showToast(getDict(s.language).toastAzkarDone);
            }
          } else {
            webPlatformAdapter.vibrateLight(s.hapticsEnabled);
          }
        } else {
          const nextIdx = list.findIndex((z, i) => i > idx && z.count < z.target);
          if (nextIdx !== -1) {
            webPlatformAdapter.vibrateLight(s.hapticsEnabled);
            get().selectAzkarIndex(isMorning, nextIdx);
          }
        }
      },
      resetAzkar: (isMorning) => {
        set(
          isMorning
            ? { morningAzkar: createDefaultMorningAzkar(), morningAzkarIndex: 0 }
            : { eveningAzkar: createDefaultEveningAzkar(), eveningAzkarIndex: 0 }
        );
        get().showToast(getDict(get().language).toastResetDone);
      },

      // ===== Tasbih / Dhikr =====
      selectDhikr: (id) => set({ activeDhikrId: id }),
      incrementDhikr: () => {
        const s = get();
        const active =
          s.adhkar.find((d) => d.id === s.activeDhikrId) ?? s.adhkar[0];
        if (!active) return;

        if (active.count < active.target) {
          const newCount = active.count + 1;
          const updated = s.adhkar.map((d) =>
            d.id === active.id ? { ...d, count: newCount } : d
          );
          set({ adhkar: updated });

          if (newCount >= active.target) {
            webPlatformAdapter.vibrateSuccess(s.hapticsEnabled);
            get().showToast(getDict(s.language).toastDhikrDone(active.short));
            const curIdx = updated.findIndex((d) => d.id === active.id);
            const next =
              updated.slice(curIdx + 1).find((d) => d.count < d.target) ??
              updated.find((d) => d.count < d.target);
            if (next && next.id !== active.id) {
              setTimeout(() => set({ activeDhikrId: next.id }), 450);
            }
          } else {
            webPlatformAdapter.vibrateLight(s.hapticsEnabled);
          }
        } else {
          webPlatformAdapter.vibrateLight(s.hapticsEnabled);
        }
      },
      adjustActiveDhikrTarget: (delta) =>
        set((s) => ({
          adhkar: s.adhkar.map((d) => {
            if (d.id !== s.activeDhikrId) return d;
            const step =
              d.target >= 100 && delta > 0
                ? 10
                : d.target > 100 && delta < 0
                  ? -10
                  : delta;
            const nextTarget = Math.max(1, Math.min(10000, d.target + step));
            return {
              ...d,
              target: nextTarget,
              count: Math.min(d.count, nextTarget),
            };
          }),
        })),
      resetActiveDhikr: () => {
        set((s) => ({
          adhkar: s.adhkar.map((d) =>
            d.id === s.activeDhikrId ? { ...d, count: 0 } : d
          ),
        }));
        get().showToast(getDict(get().language).toastResetDone);
      },
      addCustomDhikr: (text, target) => {
        const clean = text.trim();
        if (!clean) return;
        const item: DhikrItem = {
          id: `dhikr_${Date.now()}`,
          text: clean,
          short: clean.split(/\s+/).slice(0, 2).join(' '),
          target: Math.max(1, Math.min(10000, target || 33)),
          count: 0,
          custom: true,
        };
        set((s) => ({
          adhkar: [...s.adhkar, item],
          activeDhikrId: item.id,
        }));
      },
      deleteActiveDhikr: () =>
        set((s) => {
          if (s.adhkar.length <= 1) return s;
          const remaining = s.adhkar.filter((d) => d.id !== s.activeDhikrId);
          return {
            adhkar: remaining,
            activeDhikrId: remaining[0].id,
          };
        }),

      // ===== Habits =====
      toggleHabitToday: (id) => {
        const today = getTodayIso();
        webPlatformAdapter.vibrateLight(get().hapticsEnabled);
        set((s) => ({
          habits: s.habits.map((h) => {
            if (h.id !== id) return h;
            const hasToday = h.completedDates.includes(today);
            const nextDates = hasToday
              ? h.completedDates.filter((d) => d !== today)
              : Array.from(new Set([...h.completedDates, today]));
            return { ...h, completedDates: nextDates };
          }),
        }));
      },
      addHabit: (text) => {
        const clean = text.trim();
        if (!clean) return;
        const today = getTodayIso();
        set((s) => ({
          habits: [
            ...s.habits,
            {
              id: `h_${Date.now()}`,
              text: clean,
              completedDates: [],
              createdAt: today,
            },
          ],
        }));
      },
      deleteHabit: (id) =>
        set((s) => ({ habits: s.habits.filter((h) => h.id !== id) })),
      triggerNewDay: () => {
        const today = getTodayIso();
        const yesterday = getTodayIso(-1);
        // Shift today's completions to yesterday if not already recorded so streak preserves, then clear today
        set((s) => ({
          habits: s.habits.map((h) => {
            const doneToday = h.completedDates.includes(today);
            const filtered = h.completedDates.filter((d) => d !== today);
            if (doneToday && !filtered.includes(yesterday)) {
              filtered.push(yesterday);
            }
            return { ...h, completedDates: filtered };
          }),
          lastHabitDate: today,
        }));
        get().showToast(getDict(get().language).toastNewDay);
      },
      checkDailyRollover: () => {
        const today = getTodayIso();
        const s = get();
        const patch: Partial<SukoonPersistedState> = {};
        if (s.lastAzkarDate && s.lastAzkarDate !== today) {
          patch.morningAzkar = createDefaultMorningAzkar();
          patch.eveningAzkar = createDefaultEveningAzkar();
          patch.morningAzkarIndex = 0;
          patch.eveningAzkarIndex = 0;
          patch.lastAzkarDate = today;
        }
        if (s.pomodoro.lastSessionDate && s.pomodoro.lastSessionDate !== today) {
          patch.pomodoro = {
            ...s.pomodoro,
            sessionsToday: 0,
            lastSessionDate: today,
          };
        }
        if (s.lastHabitDate !== today) {
          patch.lastHabitDate = today;
        }
        if (Object.keys(patch).length > 0) {
          set(patch);
        }
      },

      // ===== Notes =====
      createNote: () => {
        const id = `n_${Date.now()}`;
        const note: NoteItem = {
          id,
          title: '',
          body: '',
          pinned: false,
          updatedAt: Date.now(),
        };
        set((s) => ({ notes: [note, ...s.notes] }));
        return id;
      },
      updateNote: (id, title, body) =>
        set((s) => ({
          notes: s.notes.map((n) =>
            n.id === id ? { ...n, title, body, updatedAt: Date.now() } : n
          ),
        })),
      toggleNotePin: (id) =>
        set((s) => ({
          notes: s.notes.map((n) =>
            n.id === id ? { ...n, pinned: !n.pinned } : n
          ),
        })),
      deleteNote: (id) =>
        set((s) => ({ notes: s.notes.filter((n) => n.id !== id) })),

      // ===== Pomodoro (Timestamp-Based — Immune to Browser Throttling) =====
      setPomodoroMode: (mode, minutes) =>
        set((s) => ({
          pomodoro: {
            ...s.pomodoro,
            mode,
            durationSeconds: minutes * 60,
            remainingSeconds: minutes * 60,
            isRunning: false,
            endTimeMs: null,
          },
        })),
      startOrResumePomodoro: () => {
        const s = get();
        const rem =
          s.pomodoro.remainingSeconds > 0
            ? s.pomodoro.remainingSeconds
            : s.pomodoro.durationSeconds;
        const endTimeMs = Date.now() + rem * 1000;
        const enableFocus =
          s.pomodoro.autoFocus && s.pomodoro.mode === 'focus' && !s.focusMode;
        set({
          pomodoro: {
            ...s.pomodoro,
            remainingSeconds: rem,
            isRunning: true,
            endTimeMs,
          },
          ...(enableFocus ? { focusMode: true } : {}),
        });
      },
      pausePomodoro: () => {
        const s = get();
        const rem =
          s.pomodoro.endTimeMs != null
            ? Math.max(0, Math.ceil((s.pomodoro.endTimeMs - Date.now()) / 1000))
            : s.pomodoro.remainingSeconds;
        set({
          pomodoro: {
            ...s.pomodoro,
            remainingSeconds: rem,
            isRunning: false,
            endTimeMs: null,
          },
        });
      },
      resetPomodoro: () =>
        set((s) => ({
          pomodoro: {
            ...s.pomodoro,
            remainingSeconds: s.pomodoro.durationSeconds,
            isRunning: false,
            endTimeMs: null,
          },
        })),
      syncPomodoroTick: () => {
        const s = get();
        if (!s.pomodoro.isRunning || s.pomodoro.endTimeMs == null) return;
        const rem = Math.max(0, Math.ceil((s.pomodoro.endTimeMs - Date.now()) / 1000));
        if (rem <= 0) {
          webPlatformAdapter.vibrateSuccess(s.hapticsEnabled);
          const today = getTodayIso();
          const inc = s.pomodoro.mode === 'focus' ? 1 : 0;
          set({
            pomodoro: {
              ...s.pomodoro,
              remainingSeconds: s.pomodoro.durationSeconds,
              isRunning: false,
              endTimeMs: null,
              sessionsToday:
                (s.pomodoro.lastSessionDate === today ? s.pomodoro.sessionsToday : 0) +
                inc,
              lastSessionDate: today,
            },
          });
          get().showToast(getDict(s.language).toastSessionDone);
        } else if (rem !== s.pomodoro.remainingSeconds) {
          set({
            pomodoro: {
              ...s.pomodoro,
              remainingSeconds: rem,
            },
          });
        }
      },
      setPomodoroTask: (task) =>
        set((s) => ({ pomodoro: { ...s.pomodoro, task } })),
      setPomodoroAutoFocus: (autoFocus) =>
        set((s) => ({ pomodoro: { ...s.pomodoro, autoFocus } })),

      // ===== Volume Simulation =====
      setVolume: (channel, value) =>
        set((s) => ({
          volumes: {
            ...s.volumes,
            [channel]: Math.max(0, Math.min(100, value)),
            muted: false,
          },
        })),
      toggleMuteAll: () =>
        set((s) => ({
          volumes: {
            ...s.volumes,
            muted: !s.volumes.muted,
          },
        })),
    }),
    {
      name: STORAGE_KEY,
      version: SCHEMA_VERSION,
      storage: createJSONStorage(() => localStorage),
      partialize: (state): SukoonPersistedState => ({
        schemaVersion: SCHEMA_VERSION,
        onboardingStatus: state.onboardingStatus,
        onboardingStep: state.onboardingStep,
        theme: state.theme,
        language: state.language,
        fontId: state.fontId,
        appDisplayStyle: state.appDisplayStyle,
        prayerCityId: state.prayerCityId,
        tabs: state.tabs,
        focusMode: state.focusMode,
        mindfulPauseSeconds: state.mindfulPauseSeconds,
        hapticsEnabled: state.hapticsEnabled,
        favoriteAppIds: state.favoriteAppIds,
        distractingAppIds: state.distractingAppIds,
        quranPage: state.quranPage,
        quranFontOffset: state.quranFontOffset,
        quranLineHeight: state.quranLineHeight,
        quranPaperMode: state.quranPaperMode,
        morningAzkar: state.morningAzkar,
        eveningAzkar: state.eveningAzkar,
        morningAzkarIndex: state.morningAzkarIndex,
        eveningAzkarIndex: state.eveningAzkarIndex,
        lastAzkarDate: state.lastAzkarDate,
        adhkar: state.adhkar,
        activeDhikrId: state.activeDhikrId,
        habits: state.habits,
        lastHabitDate: state.lastHabitDate,
        notes: state.notes,
        pomodoro: state.pomodoro,
        volumes: state.volumes,
      }),
      migrate: (persistedState: unknown) => {
        const base = createInitialPersistedState();
        if (!persistedState || typeof persistedState !== 'object') return base;
        return { ...base, ...(persistedState as Partial<SukoonPersistedState>), schemaVersion: SCHEMA_VERSION };
      },
    }
  )
);
