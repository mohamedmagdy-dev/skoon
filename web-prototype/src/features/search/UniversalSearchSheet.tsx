import React, { useMemo, useState } from 'react';
import { Volume2, VolumeX } from 'lucide-react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { formatDigits, getDict, TAB_META } from '../../design-system/i18n';
import { evaluateSimpleMath } from '../../services/mathEval';
import { webPlatformAdapter } from '../../platform/platformAdapter';
import { BottomSheet, Card, Chip, SearchInput } from '../../design-system/components';
import type { TabId } from '../../types';

export const VolumePreviewCard: React.FC = () => {
  const language = useSukoonStore((s) => s.language);
  const volumes = useSukoonStore((s) => s.volumes);
  const setVolume = useSukoonStore((s) => s.setVolume);
  const toggleMuteAll = useSukoonStore((s) => s.toggleMuteAll);
  const d = getDict(language);

  const channels: { key: 'media' | 'ring' | 'alarm'; label: string }[] = [
    { key: 'media', label: d.media },
    { key: 'ring', label: d.ring },
    { key: 'alarm', label: d.alarm },
  ];

  return (
    <Card className="p-4 space-y-3">
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          {volumes.muted ? (
            <VolumeX className="w-4 h-4 text-subtle" strokeWidth={1.6} />
          ) : (
            <Volume2 className="w-4 h-4 text-accent" strokeWidth={1.6} />
          )}
          <span className="text-[13px] font-medium text-ink">{d.volumePreviewTitle}</span>
        </div>
        <Chip
          active={volumes.muted}
          onClick={toggleMuteAll}
          className="!min-h-[30px] !px-2.5 !text-[11px]"
        >
          {volumes.muted ? d.unmute : d.mute}
        </Chip>
      </div>

      <div className="space-y-2.5">
        {channels.map((ch) => {
          const val = volumes.muted ? 0 : volumes[ch.key];
          return (
            <div key={ch.key} className="flex items-center gap-3">
              <span className="text-[12px] text-muted w-14 shrink-0">{ch.label}</span>
              <input
                type="range"
                min={0}
                max={100}
                value={val}
                aria-label={ch.label}
                onChange={(e) => setVolume(ch.key, Number(e.target.value))}
                className="flex-1 accent-[var(--color-accent)] h-1.5 bg-border rounded-pill cursor-pointer"
              />
              <span className="text-[11px] text-subtle w-9 text-end tabular-nums">
                {formatDigits(val, d.isArabic)}
                {d.percent}
              </span>
            </div>
          );
        })}
      </div>

      <p className="text-[11px] text-subtle">{d.volumeWebNote}</p>
    </Card>
  );
};

export const UniversalSearchSheet: React.FC = () => {
  const activeOverlay = useSukoonStore((s) => s.activeOverlay);
  const setActiveOverlay = useSukoonStore((s) => s.setActiveOverlay);
  const setActiveTab = useSukoonStore((s) => s.setActiveTab);
  const language = useSukoonStore((s) => s.language);
  const quranPage = useSukoonStore((s) => s.quranPage);
  const goToQuranPage = useSukoonStore((s) => s.goToQuranPage);
  const notes = useSukoonStore((s) => s.notes);
  const openApp = useSukoonStore((s) => s.openApp);

  const d = getDict(language);
  const [query, setQuery] = useState('');

  const calcResult = useMemo(() => evaluateSimpleMath(query), [query]);
  const quranPageMatch = useMemo(() => {
    const trimmed = query.trim();
    if (/^\d{1,3}$/.test(trimmed)) {
      const n = Number(trimmed);
      if (n >= 1 && n <= 604) return n;
    }
    return null;
  }, [query]);

  const matchedApps = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return [];
    return webPlatformAdapter
      .getInstalledApps()
      .filter(
        (a) =>
          a.name.toLowerCase().includes(q) ||
          (a.nameAr && a.nameAr.includes(q))
      )
      .slice(0, 6);
  }, [query]);

  const matchedNotes = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return [];
    return notes
      .filter(
        (n) =>
          n.title.toLowerCase().includes(q) || n.body.toLowerCase().includes(q)
      )
      .slice(0, 4);
  }, [query, notes]);

  const navigateTo = (tabId: TabId) => {
    setActiveTab(tabId);
    setActiveOverlay(null);
  };

  return (
    <BottomSheet
      open={activeOverlay === 'search'}
      onClose={() => setActiveOverlay(null)}
      title={d.search}
      headerRight={
        <Chip onClick={() => setActiveOverlay(null)}>{d.close}</Chip>
      }
    >
      <div className="space-y-4">
        <SearchInput
          value={query}
          onChange={setQuery}
          placeholder={d.universalSearchPlaceholder}
          autoFocus
        />

        {/* Live Math Result */}
        {calcResult !== null && (
          <div className="flex items-center justify-between px-4 py-3.5 rounded-lg bg-accent-soft border border-accent/60">
            <span className="text-[14px] text-muted">{query} =</span>
            <span className="text-[24px] font-medium text-accent tabular-nums">
              {calcResult}
            </span>
          </div>
        )}

        {/* Quick Quran Page Jump */}
        {quranPageMatch !== null && (
          <button
            type="button"
            onClick={() => {
              goToQuranPage(quranPageMatch);
              navigateTo('quran');
            }}
            className="w-full flex items-center justify-between px-4 py-3 rounded-lg bg-surface border border-border hover:border-accent text-start transition-colors"
          >
            <span className="text-[14px] text-ink">
              {d.quranJumpResult(quranPageMatch)}
            </span>
            <span className="text-[12px] text-accent">{d.open} ›</span>
          </button>
        )}

        {/* Matched Apps */}
        {matchedApps.length > 0 && (
          <div className="space-y-1">
            <p className="text-[11px] uppercase tracking-wide text-subtle">
              {d.appsSection}
            </p>
            {matchedApps.map((app) => {
              const label = d.isArabic && app.nameAr ? app.nameAr : app.name;
              return (
                <button
                  key={app.id}
                  type="button"
                  onClick={() => openApp(app)}
                  className="w-full flex items-center justify-between py-2.5 px-2 rounded-sm hover:bg-surface text-start"
                >
                  <span className="text-[16px] text-ink">{label}</span>
                  <span className="text-[12px] text-subtle">{d.open}</span>
                </button>
              );
            })}
          </div>
        )}

        {/* Matched Notes */}
        {matchedNotes.length > 0 && (
          <div className="space-y-1">
            <p className="text-[11px] uppercase tracking-wide text-subtle">
              {d.notesResults}
            </p>
            {matchedNotes.map((note) => (
              <button
                key={note.id}
                type="button"
                onClick={() => navigateTo('notes')}
                className="w-full flex flex-col py-2.5 px-2 rounded-sm hover:bg-surface text-start"
              >
                <span className="text-[15px] text-ink">
                  {note.title || d.untitledNote}
                </span>
                <span className="text-[12px] text-muted truncate">
                  {note.body}
                </span>
              </button>
            ))}
          </div>
        )}

        {/* Empty Query: Volume Preview + Quick Shortcuts */}
        {query.trim() === '' && (
          <>
            <VolumePreviewCard />

            <div className="space-y-1">
              <p className="text-[11px] uppercase tracking-wide text-subtle mb-1">
                {d.quickShortcuts}
              </p>
              {(
                [
                  {
                    title: d.isArabic ? TAB_META.quran.labelAr : TAB_META.quran.labelEn,
                    sub: `${d.isArabic ? 'صفحة' : 'Page'} ${formatDigits(quranPage, d.isArabic)}`,
                    action: () => navigateTo('quran'),
                  },
                  {
                    title: d.isArabic ? TAB_META.azkar.labelAr : TAB_META.azkar.labelEn,
                    sub: d.isArabic ? TAB_META.azkar.descAr : TAB_META.azkar.descEn,
                    action: () => navigateTo('azkar'),
                  },
                  {
                    title: d.isArabic ? TAB_META.habits.labelAr : TAB_META.habits.labelEn,
                    sub: d.isArabic ? TAB_META.habits.descAr : TAB_META.habits.descEn,
                    action: () => navigateTo('habits'),
                  },
                  {
                    title: d.settingsTitle,
                    sub: `${d.secLanguage} • ${d.secFonts} • ${d.secLocation}`,
                    action: () => setActiveOverlay('settings'),
                  },
                ] as const
              ).map((item) => (
                <button
                  key={item.title}
                  type="button"
                  onClick={item.action}
                  className="w-full flex items-center justify-between gap-2 py-3 px-2 rounded-sm hover:bg-surface text-start transition-colors"
                >
                  <span className="text-[15px] text-ink">{item.title}</span>
                  <span className="text-[12px] text-subtle truncate max-w-[180px]">
                    {item.sub}
                  </span>
                </button>
              ))}
            </div>
          </>
        )}
      </div>
    </BottomSheet>
  );
};
