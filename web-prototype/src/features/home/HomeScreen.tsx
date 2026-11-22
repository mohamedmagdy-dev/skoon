import React, { useEffect, useMemo, useState } from 'react';
import { ChevronDown, ChevronUp, Search, Settings } from 'lucide-react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { getDict, TAB_META } from '../../design-system/i18n';
import {
  formatPrayerTime12h,
  formatPrayerTimeShort,
  formatRemainingPrayer,
  getNextPrayer,
} from '../../services/prayerTimes';
import { webPlatformAdapter } from '../../platform/platformAdapter';
import { AppIconBadge } from '../../design-system/AppIconBadge';
import type { TabId } from '../../types';

interface HomeScreenProps {
  prevTabId: TabId | null;
  nextTabId: TabId | null;
  onNavigateTab: (id: TabId) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  prevTabId,
  nextTabId,
  onNavigateTab,
}) => {
  const language = useSukoonStore((s) => s.language);
  const appDisplayStyle = useSukoonStore((s) => s.appDisplayStyle);
  const prayerCityId = useSukoonStore((s) => s.prayerCityId);
  const focusMode = useSukoonStore((s) => s.focusMode);
  const toggleFocusMode = useSukoonStore((s) => s.toggleFocusMode);
  const favoriteAppIds = useSukoonStore((s) => s.favoriteAppIds);
  const distractingAppIds = useSukoonStore((s) => s.distractingAppIds);
  const openApp = useSukoonStore((s) => s.openApp);
  const toggleFavoriteApp = useSukoonStore((s) => s.toggleFavoriteApp);
  const setActiveOverlay = useSukoonStore((s) => s.setActiveOverlay);

  const d = getDict(language);
  const [editing, setEditing] = useState(false);
  const [showSchedule, setShowSchedule] = useState(false);
  const [now, setNow] = useState(() => new Date());

  // Minute-aligned clock ticker
  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>;
    const schedule = () => {
      const cur = new Date();
      setNow(cur);
      const msToNextMin = Math.max(1000, 60000 - (cur.getTime() % 60000));
      timer = setTimeout(schedule, msToNextMin);
    };
    schedule();
    return () => clearTimeout(timer);
  }, []);

  const prayerInfo = useMemo(
    () => getNextPrayer(prayerCityId, now),
    [prayerCityId, now]
  );

  const timeStr = useMemo(() => {
    const h = String(now.getHours()).padStart(2, '0');
    const m = String(now.getMinutes()).padStart(2, '0');
    return `${h}:${m}`;
  }, [now]);

  const gregDate = useMemo(() => {
    try {
      return new Intl.DateTimeFormat(d.isArabic ? 'ar-EG' : 'en-US', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
      }).format(now);
    } catch {
      return now.toDateString();
    }
  }, [now, d.isArabic]);

  const hijriDate = useMemo(() => {
    try {
      return new Intl.DateTimeFormat(
        d.isArabic ? 'ar-SA-u-ca-islamic' : 'en-US-u-ca-islamic',
        { day: 'numeric', month: 'long' }
      ).format(now);
    } catch {
      return '';
    }
  }, [now, d.isArabic]);

  const allApps = useMemo(() => webPlatformAdapter.getInstalledApps(), []);
  const favoriteApps = useMemo(
    () =>
      favoriteAppIds
        .map((id) => allApps.find((a) => a.id === id))
        .filter((a): a is NonNullable<typeof a> => Boolean(a)),
    [favoriteAppIds, allApps]
  );

  const visibleFavorites = useMemo(() => {
    if (focusMode && !editing) {
      return favoriteApps.filter((a) => !distractingAppIds.includes(a.id));
    }
    return favoriteApps;
  }, [favoriteApps, focusMode, distractingAppIds, editing]);

  const hiddenCount = favoriteApps.length - visibleFavorites.length;

  return (
    <div className="h-full flex flex-col justify-between px-page-x pt-6 pb-3 select-none">
      {/* Top Glance Section */}
      <div className="space-y-4">
        {/* Digital Clock + Dual Calendar */}
        <div className="space-y-1">
          <div className="text-[52px] leading-none font-extralight tracking-[-0.04em] text-ink tabular-nums">
            {timeStr}
          </div>
          <div className="flex flex-wrap items-center gap-2 text-[13px] text-muted font-light">
            <span>{gregDate}</span>
            {hijriDate && (
              <>
                <span className="text-subtle">•</span>
                <span>{hijriDate}</span>
              </>
            )}
          </div>
        </div>

        {/* Next Prayer Card (Expandable) */}
        <div className="space-y-2">
          <button
            type="button"
            onClick={() => setShowSchedule((v) => !v)}
            aria-expanded={showSchedule}
            className="w-full flex items-center justify-between gap-3 px-4 py-3 rounded-lg bg-surface border border-border hover:border-border-strong transition-colors text-start"
          >
            <div className="flex items-center gap-2.5 min-w-0">
              <span className="w-2 h-2 rounded-pill bg-accent shrink-0" />
              <div className="min-w-0">
                <p className="text-[14px] font-medium text-ink truncate">
                  {d.isArabic
                    ? prayerInfo.nextPrayer.nameAr
                    : prayerInfo.nextPrayer.nameEn}{' '}
                  <span className="text-accent">
                    {formatPrayerTime12h(prayerInfo.nextPrayer, d.isArabic)}
                  </span>
                </p>
                <p className="text-[12px] text-muted">
                  {formatRemainingPrayer(prayerInfo.remainingMinutes, d.isArabic)} •{' '}
                  {d.isArabic ? prayerInfo.city.nameAr : prayerInfo.city.nameEn}
                </p>
              </div>
            </div>
            {showSchedule ? (
              <ChevronUp className="w-4 h-4 text-subtle shrink-0" strokeWidth={1.6} />
            ) : (
              <ChevronDown className="w-4 h-4 text-subtle shrink-0" strokeWidth={1.6} />
            )}
          </button>

          {showSchedule && (
            <div className="p-3.5 rounded-lg bg-surface border border-border space-y-3 animate-fade-in">
              <div className="flex items-center justify-between text-[12px]">
                <span className="text-muted">
                  {d.isArabic
                    ? `${prayerInfo.city.nameAr}، ${prayerInfo.city.countryAr}`
                    : `${prayerInfo.city.nameEn}, ${prayerInfo.city.countryEn}`}
                </span>
                <button
                  type="button"
                  onClick={() => setActiveOverlay('settings')}
                  className="text-accent hover:underline"
                >
                  {d.changeCity}
                </button>
              </div>
              <div className="grid grid-cols-5 gap-1 text-center">
                {prayerInfo.todayPrayers.map((p) => {
                  const isNext = p.id === prayerInfo.nextPrayer.id;
                  return (
                    <div
                      key={p.id}
                      className={`py-2 px-1 rounded-sm ${
                        isNext ? 'bg-accent-soft' : ''
                      }`}
                    >
                      <p
                        className={`text-[11px] ${
                          isNext ? 'text-accent font-medium' : 'text-muted'
                        }`}
                      >
                        {d.isArabic ? p.nameAr : p.nameEn}
                      </p>
                      <p
                        className={`text-[12px] mt-0.5 tabular-nums ${
                          isNext ? 'text-ink font-medium' : 'text-muted'
                        }`}
                      >
                        {formatPrayerTimeShort(p, d.isArabic)}
                      </p>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>

        {/* Minimal Quick Action Row */}
        <div className="flex items-center gap-2 overflow-x-auto no-scrollbar pb-0.5">
          <button
            type="button"
            onClick={() => toggleFocusMode()}
            className={`inline-flex items-center gap-2 min-h-[36px] px-3.5 rounded-pill border text-[13px] whitespace-nowrap transition-colors ${
              focusMode
                ? 'bg-[var(--color-success-soft)] border-success/50 text-success font-medium'
                : 'bg-surface border-border text-muted hover:text-ink'
            }`}
          >
            <span
              className={`w-1.5 h-1.5 rounded-pill ${
                focusMode ? 'bg-success' : 'bg-subtle'
              }`}
            />
            <span>{focusMode ? d.focusActive : d.focusInactive}</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveOverlay('search')}
            className="inline-flex items-center gap-1.5 min-h-[36px] px-3.5 rounded-pill bg-surface border border-border text-[13px] text-muted hover:text-ink whitespace-nowrap transition-colors"
          >
            <Search className="w-3.5 h-3.5 text-accent" strokeWidth={1.7} />
            <span>{d.searchAction}</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveOverlay('settings')}
            className="inline-flex items-center gap-1.5 min-h-[36px] px-3.5 rounded-pill bg-surface border border-border text-[13px] text-muted hover:text-ink whitespace-nowrap transition-colors"
          >
            <Settings className="w-3.5 h-3.5" strokeWidth={1.7} />
            <span>{d.settings}</span>
          </button>
        </div>
      </div>

      {/* Center Stage: Text-Only Favorites */}
      <div className="flex-1 flex flex-col justify-center py-5 min-h-0">
        <div className="flex items-center justify-between mb-2">
          <span className="text-[12px] tracking-wide text-subtle">{d.favorites}</span>
          <button
            type="button"
            onClick={() => setEditing((v) => !v)}
            className={`min-h-[32px] px-2 text-[12px] transition-colors ${
              editing ? 'text-accent font-medium' : 'text-subtle hover:text-ink'
            }`}
          >
            {editing ? d.done : d.edit}
          </button>
        </div>

        <div className="overflow-y-auto space-y-0.5 pe-1">
          {visibleFavorites.map((app) => {
            const isMindful = distractingAppIds.includes(app.id);
            const label = d.isArabic && app.nameAr ? app.nameAr : app.name;
            return (
              <div
                key={app.id}
                className="flex items-center justify-between gap-2 py-1"
              >
                <button
                  type="button"
                  disabled={editing}
                  onClick={() => openApp(app)}
                  className="flex items-center gap-3 py-1.5 text-start group disabled:pointer-events-none min-w-0"
                >
                  <AppIconBadge app={app} style={appDisplayStyle} size="md" />
                  <span
                    className={`leading-snug font-light text-ink group-hover:text-accent transition-colors truncate ${
                      appDisplayStyle === 'text' ? 'text-[24px]' : 'text-[18px]'
                    }`}
                  >
                    {label}
                  </span>
                  {isMindful && !editing && (
                    <span className="px-2 py-0.5 rounded-pill border border-border text-[10px] text-subtle shrink-0">
                      {d.mindfulBadge}
                    </span>
                  )}
                </button>

                {editing && (
                  <button
                    type="button"
                    onClick={() => toggleFavoriteApp(app.id)}
                    className="min-h-[32px] px-2.5 rounded-sm bg-[var(--color-danger-soft)] text-danger text-[12px]"
                  >
                    {d.remove}
                  </button>
                )}
              </div>
            );
          })}

          {hiddenCount > 0 && (
            <button
              type="button"
              onClick={() => toggleFocusMode(false)}
              className="mt-2 text-[12px] text-subtle hover:text-muted text-start"
            >
              {d.hiddenAppsNote(hiddenCount)}
            </button>
          )}
        </div>
      </div>

      {/* Bottom Edge Navigation Hints */}
      <div className="flex items-center justify-between pt-2 border-t border-border/60 text-[13px]">
        <button
          type="button"
          disabled={!prevTabId}
          onClick={() => prevTabId && onNavigateTab(prevTabId)}
          className="min-h-[40px] px-1 text-muted hover:text-ink disabled:opacity-0 transition-colors"
        >
          {prevTabId
            ? `‹ ${d.isArabic ? TAB_META[prevTabId].shortAr : TAB_META[prevTabId].shortEn}`
            : ''}
        </button>

        <button
          type="button"
          onClick={() => setActiveOverlay('app-drawer')}
          className="min-h-[40px] px-3 text-muted hover:text-ink transition-colors"
        >
          {d.allApps} ↑
        </button>

        <button
          type="button"
          disabled={!nextTabId}
          onClick={() => nextTabId && onNavigateTab(nextTabId)}
          className="min-h-[40px] px-1 text-accent hover:opacity-80 disabled:opacity-0 transition-colors"
        >
          {nextTabId
            ? `${d.isArabic ? TAB_META[nextTabId].shortAr : TAB_META[nextTabId].shortEn} ›`
            : ''}
        </button>
      </div>
    </div>
  );
};
