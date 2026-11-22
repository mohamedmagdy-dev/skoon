import React, { useEffect, useMemo, useRef } from 'react';
import { useSukoonStore } from '../stores/useSukoonStore';
import { getFontOption } from '../design-system/fonts';
import { getDict, TAB_META } from '../design-system/i18n';
import { Toast } from '../design-system/components';
import { OnboardingFlow } from '../features/onboarding/OnboardingFlow';
import { HomeScreen } from '../features/home/HomeScreen';
import { QuranScreen } from '../features/quran/QuranScreen';
import { AzkarScreen } from '../features/azkar/AzkarScreen';
import { TasbihScreen } from '../features/tasbih/TasbihScreen';
import { HabitsScreen } from '../features/habits/HabitsScreen';
import { NotesScreen } from '../features/notes/NotesScreen';
import { PomodoroScreen } from '../features/pomodoro/PomodoroScreen';
import { AppDrawerSheet } from '../features/app-drawer/AppDrawerSheet';
import { UniversalSearchSheet } from '../features/search/UniversalSearchSheet';
import { SettingsSheet } from '../features/settings/SettingsSheet';
import { MindfulPauseModal } from '../features/focus-mode/MindfulPauseModal';
import { BlackScreenModal } from '../features/focus-mode/BlackScreenModal';
import type { TabId } from '../types';

export const App: React.FC = () => {
  const onboardingStatus = useSukoonStore((s) => s.onboardingStatus);
  const theme = useSukoonStore((s) => s.theme);
  const language = useSukoonStore((s) => s.language);
  const fontId = useSukoonStore((s) => s.fontId);
  const tabs = useSukoonStore((s) => s.tabs);
  const activeTabId = useSukoonStore((s) => s.activeTabId);
  const setActiveTab = useSukoonStore((s) => s.setActiveTab);
  const activeOverlay = useSukoonStore((s) => s.activeOverlay);
  const setActiveOverlay = useSukoonStore((s) => s.setActiveOverlay);
  const toastMessage = useSukoonStore((s) => s.toastMessage);
  const checkDailyRollover = useSukoonStore((s) => s.checkDailyRollover);

  const d = getDict(language);

  // Sync HTML theme, dir, lang, font CSS var, and theme-color meta
  useEffect(() => {
    const root = document.documentElement;
    root.setAttribute('data-theme', theme);
    root.setAttribute('lang', language);
    root.setAttribute('dir', language === 'ar' ? 'rtl' : 'ltr');
    const fontOpt = getFontOption(fontId);
    root.style.setProperty('--font-app', fontOpt.cssFamily);

    const meta = document.querySelector('meta[name="theme-color"]');
    if (meta) {
      meta.setAttribute('content', theme === 'dark' ? '#09090B' : '#F7F5F0');
    }
  }, [theme, language, fontId]);

  // Daily rollover check on mount + visibility return
  useEffect(() => {
    checkDailyRollover();
    const onVis = () => {
      if (document.visibilityState === 'visible') checkDailyRollover();
    };
    document.addEventListener('visibilitychange', onVis);
    return () => document.removeEventListener('visibilitychange', onVis);
  }, [checkDailyRollover]);

  const enabledTabs = useMemo(
    () => tabs.filter((t) => t.enabled || t.id === 'home').map((t) => t.id),
    [tabs]
  );

  const activeIndex = useMemo(() => {
    const idx = enabledTabs.indexOf(activeTabId);
    if (idx !== -1) return idx;
    const homeIdx = enabledTabs.indexOf('home');
    return homeIdx !== -1 ? homeIdx : 0;
  }, [enabledTabs, activeTabId]);

  const currentTabId: TabId = enabledTabs[activeIndex] ?? 'home';
  const prevTabId: TabId | null = enabledTabs[activeIndex - 1] ?? null;
  const nextTabId: TabId | null = enabledTabs[activeIndex + 1] ?? null;

  // Keyboard navigation (respecting input focus)
  useEffect(() => {
    if (onboardingStatus !== 'completed') return;
    const onKeyDown = (e: KeyboardEvent) => {
      const tag = (e.target as HTMLElement | null)?.tagName;
      const isEditing = tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT';

      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setActiveOverlay(activeOverlay === 'search' ? null : 'search');
        return;
      }

      if (isEditing || activeOverlay !== null) return;

      if (e.key === 'ArrowRight' && nextTabId) {
        setActiveTab(nextTabId);
      } else if (e.key === 'ArrowLeft' && prevTabId) {
        setActiveTab(prevTabId);
      } else if (e.key === 'Escape' && currentTabId !== 'home') {
        setActiveTab('home');
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [
    onboardingStatus,
    activeOverlay,
    prevTabId,
    nextTabId,
    currentTabId,
    setActiveTab,
    setActiveOverlay,
  ]);

  // Touch swipe gestures (horizontal tab switching + vertical Home gestures without fighting scroll)
  const touchStart = useRef<{ x: number; y: number } | null>(null);

  const handleTouchStart = (e: React.TouchEvent) => {
    if (e.touches.length !== 1) return;
    touchStart.current = {
      x: e.touches[0].clientX,
      y: e.touches[0].clientY,
    };
  };

  const handleTouchEnd = (e: React.TouchEvent) => {
    if (!touchStart.current || e.changedTouches.length !== 1) return;
    const dx = e.changedTouches[0].clientX - touchStart.current.x;
    const dy = e.changedTouches[0].clientY - touchStart.current.y;
    touchStart.current = null;

    const absX = Math.abs(dx);
    const absY = Math.abs(dy);

    // Horizontal swipe -> switch tabs
    if (absX > 56 && absX > absY * 1.35) {
      if (dx < 0 && nextTabId) {
        setActiveTab(nextTabId);
      } else if (dx > 0 && prevTabId) {
        setActiveTab(prevTabId);
      }
      return;
    }

    // Vertical swipe on Home -> App Drawer (up) / Search (down)
    if (currentTabId === 'home' && absY > 72 && absY > absX * 1.4) {
      if (dy < 0) {
        setActiveOverlay('app-drawer');
      } else if (dy > 0) {
        setActiveOverlay('search');
      }
    }
  };

  // First-Launch Guided Onboarding
  if (onboardingStatus !== 'completed') {
    return (
      <>
        <OnboardingFlow />
        <Toast message={toastMessage} />
      </>
    );
  }

  return (
    <div className="h-[100dvh] w-full flex items-center justify-center bg-bg sm:p-4 overflow-hidden">
      {/* Mobile-First App Shell (Full-bleed on phones; centered framed container on desktop) */}
      <main
        onTouchStart={handleTouchStart}
        onTouchEnd={handleTouchEnd}
        className="w-full max-w-[440px] h-full sm:max-h-[860px] sm:rounded-[28px] sm:border sm:border-border bg-bg flex flex-col overflow-hidden relative"
      >
        {/* Active Screen Viewport */}
        <div className="flex-1 min-h-0 overflow-hidden animate-fade-in" key={currentTabId}>
          {currentTabId === 'home' && (
            <HomeScreen
              prevTabId={prevTabId}
              nextTabId={nextTabId}
              onNavigateTab={setActiveTab}
            />
          )}
          {currentTabId === 'quran' && <QuranScreen />}
          {currentTabId === 'azkar' && <AzkarScreen />}
          {currentTabId === 'tasbih' && <TasbihScreen />}
          {currentTabId === 'habits' && <HabitsScreen />}
          {currentTabId === 'notes' && <NotesScreen />}
          {currentTabId === 'pomodoro' && <PomodoroScreen />}
        </div>

        {/* Minimal Page Indicator Dots */}
        <nav
          aria-label="Pages"
          dir="ltr"
          className="shrink-0 flex items-center justify-center gap-2 py-2.5 px-4 bg-bg"
        >
          {enabledTabs.map((id) => {
            const active = id === currentTabId;
            const isSpiritual = id === 'quran' || id === 'azkar' || id === 'tasbih';
            const meta = TAB_META[id];
            return (
              <button
                key={id}
                type="button"
                aria-label={d.isArabic ? meta.labelAr : meta.labelEn}
                aria-current={active ? 'page' : undefined}
                onClick={() => setActiveTab(id)}
                className="p-1.5 -m-1 focus:outline-none"
              >
                <span
                  className={`block h-[5px] rounded-pill transition-all duration-200 ${
                    active
                      ? isSpiritual
                        ? 'w-5 bg-accent'
                        : 'w-5 bg-ink'
                      : 'w-[5px] bg-border-strong hover:bg-muted'
                  }`}
                />
              </button>
            );
          })}
        </nav>
      </main>

      {/* Overlays & Modals */}
      <AppDrawerSheet />
      <UniversalSearchSheet />
      <SettingsSheet />
      <MindfulPauseModal />
      <BlackScreenModal />
      <Toast message={toastMessage} />
    </div>
  );
};
