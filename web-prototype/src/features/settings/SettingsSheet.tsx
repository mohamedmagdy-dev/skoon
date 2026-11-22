import React, { useMemo, useState } from 'react';
import {
  ArrowDown,
  ArrowUp,
  ChevronDown,
  ChevronUp,
  GripVertical,
  MessageCircle,
  Play,
  RotateCcw,
  Trash2,
} from 'lucide-react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { formatDigits, getDict, TAB_META } from '../../design-system/i18n';
import { FONT_OPTIONS, getFontOption } from '../../design-system/fonts';
import { findCity, PRAYER_CITIES } from '../../services/prayerTimes';
import {
  BottomSheet,
  Card,
  Chip,
  ConfirmDialog,
  SearchInput,
  Toggle,
} from '../../design-system/components';
import { VolumePreviewCard } from '../search/UniversalSearchSheet';
import type { AppDisplayStyle } from '../../types';

type SettingsCategory = 'all' | 'display' | 'spiritual' | 'focus' | 'system';

const LINE_HEIGHT_PRESETS = [
  { value: 1.85, key: 'spacingCompact' as const },
  { value: 2.15, key: 'spacingComfortable' as const },
  { value: 2.45, key: 'spacingWide' as const },
  { value: 2.75, key: 'spacingExtraWide' as const },
];

export const SettingsSheet: React.FC = () => {
  const activeOverlay = useSukoonStore((s) => s.activeOverlay);
  const setActiveOverlay = useSukoonStore((s) => s.setActiveOverlay);
  const language = useSukoonStore((s) => s.language);
  const setLanguage = useSukoonStore((s) => s.setLanguage);
  const theme = useSukoonStore((s) => s.theme);
  const setTheme = useSukoonStore((s) => s.setTheme);
  const fontId = useSukoonStore((s) => s.fontId);
  const setFontId = useSukoonStore((s) => s.setFontId);
  const appDisplayStyle = useSukoonStore((s) => s.appDisplayStyle);
  const setAppDisplayStyle = useSukoonStore((s) => s.setAppDisplayStyle);
  const prayerCityId = useSukoonStore((s) => s.prayerCityId);
  const setPrayerCityId = useSukoonStore((s) => s.setPrayerCityId);
  const quranLineHeight = useSukoonStore((s) => s.quranLineHeight);
  const setQuranLineHeight = useSukoonStore((s) => s.setQuranLineHeight);
  const tabs = useSukoonStore((s) => s.tabs);
  const toggleTabEnabled = useSukoonStore((s) => s.toggleTabEnabled);
  const reorderTabs = useSukoonStore((s) => s.reorderTabs);
  const focusMode = useSukoonStore((s) => s.focusMode);
  const toggleFocusMode = useSukoonStore((s) => s.toggleFocusMode);
  const mindfulPauseSeconds = useSukoonStore((s) => s.mindfulPauseSeconds);
  const setMindfulPauseSeconds = useSukoonStore((s) => s.setMindfulPauseSeconds);
  const hapticsEnabled = useSukoonStore((s) => s.hapticsEnabled);
  const setHapticsEnabled = useSukoonStore((s) => s.setHapticsEnabled);
  const volumes = useSukoonStore((s) => s.volumes);
  const restartSetup = useSukoonStore((s) => s.restartSetup);
  const resetApp = useSukoonStore((s) => s.resetApp);

  const d = getDict(language);
  const [category, setCategory] = useState<SettingsCategory>('all');
  const [openPanel, setOpenPanel] = useState<null | 'fonts' | 'city' | 'tabs' | 'volume'>(null);
  const [cityQuery, setCityQuery] = useState('');
  const [confirmResetApp, setConfirmResetApp] = useState(false);
  const [dragIdx, setDragIdx] = useState<number | null>(null);

  const togglePanel = (panel: 'fonts' | 'city' | 'tabs' | 'volume') => {
    setOpenPanel((cur) => (cur === panel ? null : panel));
  };

  const selectedCity = useMemo(() => findCity(prayerCityId), [prayerCityId]);
  const selectedFont = useMemo(() => getFontOption(fontId), [fontId]);
  const filteredCities = useMemo(() => {
    const q = cityQuery.trim().toLowerCase();
    if (!q) return PRAYER_CITIES;
    return PRAYER_CITIES.filter(
      (c) =>
        c.nameAr.includes(q) ||
        c.nameEn.toLowerCase().includes(q) ||
        c.countryAr.includes(q) ||
        c.countryEn.toLowerCase().includes(q)
    );
  }, [cityQuery]);

  const enabledIds = useMemo(
    () => tabs.filter((t) => t.enabled || t.id === 'home').map((t) => t.id),
    [tabs]
  );
  const homeEnabledIdx = enabledIds.indexOf('home');

  const showSection = (cat: Exclude<SettingsCategory, 'all'>) =>
    category === 'all' || category === cat;

  const displayStyleCards: {
    id: AppDisplayStyle;
    title: string;
    sub: string;
  }[] = [
    { id: 'text', title: d.styleTextOnly, sub: d.styleTextOnlySub },
    { id: 'monochrome', title: d.styleMonochrome, sub: d.styleMonochromeSub },
    { id: 'original', title: d.styleOriginal, sub: d.styleOriginalSub },
  ];

  return (
    <>
      <BottomSheet
        open={activeOverlay === 'settings'}
        onClose={() => setActiveOverlay(null)}
        eyebrow={d.settingsEyebrow}
        title={d.settingsTitle}
        headerRight={
          <Chip active onClick={() => setActiveOverlay(null)}>
            {d.done}
          </Chip>
        }
      >
        <div className="space-y-5 pb-8">
          {/* Category Filter Strip */}
          <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar pb-1">
            {(
              [
                { id: 'all', label: d.catAll },
                { id: 'display', label: d.catDisplay },
                { id: 'spiritual', label: d.catSpiritual },
                { id: 'focus', label: d.catFocus },
                { id: 'system', label: d.catSystem },
              ] as const
            ).map((item) => (
              <Chip
                key={item.id}
                active={category === item.id}
                onClick={() => setCategory(item.id)}
                className="!min-h-[34px] !px-3 !text-[12px]"
              >
                {item.label}
              </Chip>
            ))}
          </div>

          {/* =============================================
              GROUP 1: DISPLAY & APPEARANCE
             ============================================= */}
          {showSection('display') && (
            <section className="space-y-3">
              <p className="text-[11px] font-medium uppercase tracking-widest text-subtle px-1">
                {d.catDisplay}
              </p>

              {/* Visual App Display Style Selector (3 Cards) */}
              <Card className="p-3.5 space-y-3">
                <div>
                  <p className="text-[14px] font-medium text-ink">{d.secAppDisplay}</p>
                  <p className="text-[12px] text-muted mt-0.5">{d.secAppDisplaySub}</p>
                </div>

                <div className="grid grid-cols-3 gap-2">
                  {displayStyleCards.map((opt) => {
                    const active = appDisplayStyle === opt.id;
                    return (
                      <button
                        key={opt.id}
                        type="button"
                        onClick={() => setAppDisplayStyle(opt.id)}
                        className={`p-2.5 rounded-md border text-center transition-colors flex flex-col items-center gap-2 ${
                          active
                            ? 'bg-accent-soft border-accent text-ink'
                            : 'bg-bg border-border hover:border-border-strong text-muted'
                        }`}
                      >
                        {/* Mini Visual Preview */}
                        <div className="h-9 flex items-center justify-center gap-1.5">
                          {opt.id === 'text' && (
                            <div className="flex flex-col items-center gap-1">
                              <span className="w-10 h-1.5 rounded-pill bg-ink/80" />
                              <span className="w-7 h-1 rounded-pill bg-muted/60" />
                            </div>
                          )}
                          {opt.id === 'monochrome' && (
                            <>
                              <span className="w-6 h-6 rounded-[7px] bg-surface border border-border-strong flex items-center justify-center text-ink">
                                <MessageCircle className="w-3 h-3" strokeWidth={1.7} />
                              </span>
                              <span className="w-6 h-6 rounded-[7px] bg-surface border border-border-strong flex items-center justify-center text-ink">
                                <Play className="w-3 h-3" strokeWidth={1.7} />
                              </span>
                            </>
                          )}
                          {opt.id === 'original' && (
                            <>
                              <span className="w-6 h-6 rounded-[7px] bg-[#25D366] text-white flex items-center justify-center">
                                <MessageCircle className="w-3 h-3" strokeWidth={2} />
                              </span>
                              <span className="w-6 h-6 rounded-[7px] bg-[#EF4444] text-white flex items-center justify-center">
                                <Play className="w-3 h-3" strokeWidth={2} />
                              </span>
                            </>
                          )}
                        </div>
                        <div>
                          <p
                            className={`text-[12px] font-medium leading-tight ${
                              active ? 'text-accent' : 'text-ink'
                            }`}
                          >
                            {opt.title}
                          </p>
                          <p className="text-[10px] text-subtle mt-0.5 leading-tight">
                            {opt.sub}
                          </p>
                        </div>
                      </button>
                    );
                  })}
                </div>
              </Card>

              {/* Theme + Language + Typography Grouped Card */}
              <Card className="divide-y divide-border overflow-hidden">
                {/* Theme Row */}
                <div className="flex items-center justify-between gap-3 px-4 py-3">
                  <span className="text-[14px] text-ink">{d.secAppearance}</span>
                  <div className="flex items-center gap-1.5">
                    <Chip
                      active={theme === 'dark'}
                      onClick={() => setTheme('dark')}
                      className="!min-h-[32px] !px-3 !text-[12px]"
                    >
                      {d.darkOled}
                    </Chip>
                    <Chip
                      active={theme === 'light'}
                      onClick={() => setTheme('light')}
                      className="!min-h-[32px] !px-3 !text-[12px]"
                    >
                      {d.lightPaper}
                    </Chip>
                  </div>
                </div>

                {/* Language Row */}
                <div className="flex items-center justify-between gap-3 px-4 py-3">
                  <span className="text-[14px] text-ink">{d.secLanguage}</span>
                  <div className="flex items-center gap-1.5">
                    <Chip
                      active={language === 'ar'}
                      onClick={() => setLanguage('ar')}
                      className="!min-h-[32px] !px-3 !text-[12px]"
                    >
                      العربية
                    </Chip>
                    <Chip
                      active={language === 'en'}
                      onClick={() => setLanguage('en')}
                      className="!min-h-[32px] !px-3 !text-[12px]"
                    >
                      English
                    </Chip>
                  </div>
                </div>

                {/* Collapsible Font Selector Row */}
                <div>
                  <button
                    type="button"
                    onClick={() => togglePanel('fonts')}
                    className="w-full flex items-center justify-between gap-3 px-4 py-3.5 text-start hover:bg-elevated transition-colors"
                  >
                    <span className="text-[14px] text-ink">{d.secFonts}</span>
                    <span className="inline-flex items-center gap-1.5 text-[13px] text-accent">
                      <span>
                        {d.isArabic ? selectedFont.nameAr : selectedFont.nameEn}
                      </span>
                      {openPanel === 'fonts' ? (
                        <ChevronUp className="w-4 h-4 text-subtle" strokeWidth={1.6} />
                      ) : (
                        <ChevronDown className="w-4 h-4 text-subtle" strokeWidth={1.6} />
                      )}
                    </span>
                  </button>

                  {openPanel === 'fonts' && (
                    <div className="px-4 pb-4 pt-1 grid grid-cols-2 gap-2 animate-fade-in">
                      {FONT_OPTIONS.map((f) => (
                        <Chip
                          key={f.id}
                          active={fontId === f.id}
                          onClick={() => setFontId(f.id)}
                          style={{ fontFamily: f.cssFamily }}
                        >
                          {d.isArabic ? f.nameAr : f.nameEn}
                        </Chip>
                      ))}
                    </div>
                  )}
                </div>
              </Card>
            </section>
          )}

          {/* =============================================
              GROUP 2: SPIRITUAL (PRAYER LOCATION & QURAN)
             ============================================= */}
          {showSection('spiritual') && (
            <section className="space-y-3">
              <p className="text-[11px] font-medium uppercase tracking-widest text-subtle px-1">
                {d.catSpiritual}
              </p>

              <Card className="divide-y divide-border overflow-hidden">
                {/* Prayer City Row */}
                <div>
                  <button
                    type="button"
                    onClick={() => togglePanel('city')}
                    className="w-full flex items-center justify-between gap-3 px-4 py-3.5 text-start hover:bg-elevated transition-colors"
                  >
                    <div className="min-w-0">
                      <p className="text-[14px] text-ink">{d.secLocation}</p>
                      <p className="text-[11px] text-muted mt-0.5">
                        {d.isArabic
                          ? `${selectedCity.nameAr}، ${selectedCity.countryAr}`
                          : `${selectedCity.nameEn}, ${selectedCity.countryEn}`}
                      </p>
                    </div>
                    <span className="inline-flex items-center gap-1 text-[12px] text-accent shrink-0">
                      <span>{openPanel === 'city' ? d.close : d.changeCity}</span>
                      {openPanel === 'city' ? (
                        <ChevronUp className="w-4 h-4 text-subtle" strokeWidth={1.6} />
                      ) : (
                        <ChevronDown className="w-4 h-4 text-subtle" strokeWidth={1.6} />
                      )}
                    </span>
                  </button>

                  {openPanel === 'city' && (
                    <div className="px-4 pb-4 pt-1 space-y-2.5 animate-fade-in">
                      <SearchInput
                        value={cityQuery}
                        onChange={setCityQuery}
                        placeholder={d.searchCityPlaceholder}
                      />
                      <div className="max-h-[200px] overflow-y-auto rounded-md border border-border bg-bg divide-y divide-border/60">
                        {filteredCities.map((c) => {
                          const active = c.id === prayerCityId;
                          return (
                            <button
                              key={c.id}
                              type="button"
                              onClick={() => {
                                setPrayerCityId(c.id);
                                setCityQuery('');
                                setOpenPanel(null);
                              }}
                              className={`w-full px-3 py-2.5 flex items-center justify-between text-start ${
                                active
                                  ? 'bg-accent-soft text-accent'
                                  : 'hover:bg-surface'
                              }`}
                            >
                              <span className="text-[13px]">
                                {d.isArabic ? c.nameAr : c.nameEn}
                              </span>
                              <span className="text-[11px] text-muted">
                                {d.isArabic ? c.countryAr : c.countryEn}
                                {active ? ' ✓' : ''}
                              </span>
                            </button>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </div>

                {/* Quran Line Spacing Row */}
                <div className="px-4 py-3.5 space-y-2.5">
                  <div className="flex items-center justify-between">
                    <span className="text-[14px] text-ink">
                      {d.secQuran} — {d.lineSpacing}
                    </span>
                    <span className="text-[12px] text-accent tabular-nums">
                      {quranLineHeight.toFixed(2)}×
                    </span>
                  </div>
                  <div className="grid grid-cols-4 gap-1.5">
                    {LINE_HEIGHT_PRESETS.map((p) => (
                      <Chip
                        key={p.value}
                        active={Math.abs(quranLineHeight - p.value) < 0.12}
                        onClick={() => setQuranLineHeight(p.value)}
                        className="!min-h-[32px] !px-1.5 !text-[11px]"
                      >
                        {d[p.key]}
                      </Chip>
                    ))}
                  </div>
                </div>
              </Card>
            </section>
          )}

          {/* =============================================
              GROUP 3: FOCUS & NAVIGATION
             ============================================= */}
          {showSection('focus') && (
            <section className="space-y-3">
              <p className="text-[11px] font-medium uppercase tracking-widest text-subtle px-1">
                {d.catFocus}
              </p>

              <Card className="divide-y divide-border overflow-hidden">
                <div className="flex items-center justify-between px-4 py-3">
                  <span className="text-[14px] text-ink">{d.focusInactive}</span>
                  <Toggle checked={focusMode} onChange={(v) => toggleFocusMode(v)} />
                </div>

                <div className="flex items-center justify-between px-4 py-3">
                  <span className="text-[14px] text-ink">{d.mindfulDurationLabel}</span>
                  <div className="flex items-center gap-1.5">
                    {[3, 5, 10].map((sec) => (
                      <Chip
                        key={sec}
                        active={mindfulPauseSeconds === sec}
                        onClick={() => setMindfulPauseSeconds(sec)}
                        className="!min-h-[30px] !px-2.5 !text-[12px]"
                      >
                        {formatDigits(sec, d.isArabic)}s
                      </Chip>
                    ))}
                  </div>
                </div>

                <div className="flex items-center justify-between px-4 py-3">
                  <span className="text-[14px] text-ink">{d.hapticsLabel}</span>
                  <Toggle checked={hapticsEnabled} onChange={setHapticsEnabled} />
                </div>

                {/* Collapsible Navigation & Tabs Order */}
                <div>
                  <button
                    type="button"
                    onClick={() => togglePanel('tabs')}
                    className="w-full flex items-center justify-between gap-3 px-4 py-3.5 text-start hover:bg-elevated transition-colors"
                  >
                    <span className="text-[14px] text-ink">{d.secNavigation}</span>
                    <span className="inline-flex items-center gap-1.5 text-[12px] text-accent">
                      <span>
                        {formatDigits(enabledIds.length, d.isArabic)} /{' '}
                        {formatDigits(tabs.length, d.isArabic)}
                      </span>
                      {openPanel === 'tabs' ? (
                        <ChevronUp className="w-4 h-4 text-subtle" strokeWidth={1.6} />
                      ) : (
                        <ChevronDown className="w-4 h-4 text-subtle" strokeWidth={1.6} />
                      )}
                    </span>
                  </button>

                  {openPanel === 'tabs' && (
                    <div className="px-3.5 pb-4 pt-1 space-y-1.5 animate-fade-in">
                      {tabs.map((cfg, idx) => {
                        const meta = TAB_META[cfg.id];
                        const isHome = cfg.id === 'home';
                        const posIdx = enabledIds.indexOf(cfg.id);
                        const posText =
                          !cfg.enabled && !isHome
                            ? d.hidden
                            : isHome
                              ? d.centerAnchor
                              : posIdx > homeEnabledIdx
                                ? d.rightOfHome(posIdx - homeEnabledIdx)
                                : d.leftOfHome(homeEnabledIdx - posIdx);

                        return (
                          <div
                            key={cfg.id}
                            draggable
                            onDragStart={() => setDragIdx(idx)}
                            onDragOver={(e) => e.preventDefault()}
                            onDrop={() => {
                              if (dragIdx !== null && dragIdx !== idx) {
                                reorderTabs(dragIdx, idx);
                              }
                              setDragIdx(null);
                            }}
                            onDragEnd={() => setDragIdx(null)}
                            className={`flex items-center justify-between gap-2 px-3 py-2 rounded-md border ${
                              isHome
                                ? 'bg-accent-soft border-accent/50'
                                : 'bg-bg border-border'
                            }`}
                          >
                            <div className="flex items-center gap-2 min-w-0">
                              <GripVertical
                                className="w-3.5 h-3.5 text-subtle shrink-0 cursor-grab"
                                strokeWidth={1.6}
                              />
                              <div className="min-w-0">
                                <p className="text-[13px] font-medium text-ink truncate">
                                  {d.isArabic ? meta.labelAr : meta.labelEn}
                                </p>
                                <p className="text-[10px] text-muted">{posText}</p>
                              </div>
                            </div>

                            <div className="flex items-center gap-0.5 shrink-0">
                              <button
                                type="button"
                                aria-label="Move up"
                                disabled={idx === 0}
                                onClick={() => reorderTabs(idx, idx - 1)}
                                className="p-1.5 text-muted hover:text-ink disabled:opacity-25"
                              >
                                <ArrowUp className="w-3.5 h-3.5" strokeWidth={1.6} />
                              </button>
                              <button
                                type="button"
                                aria-label="Move down"
                                disabled={idx === tabs.length - 1}
                                onClick={() => reorderTabs(idx, idx + 1)}
                                className="p-1.5 text-muted hover:text-ink disabled:opacity-25"
                              >
                                <ArrowDown className="w-3.5 h-3.5" strokeWidth={1.6} />
                              </button>
                              {!isHome && (
                                <Chip
                                  active={cfg.enabled}
                                  onClick={() => toggleTabEnabled(cfg.id)}
                                  className="!min-h-[26px] !px-2 !text-[10px]"
                                >
                                  {cfg.enabled ? d.visible : d.hidden}
                                </Chip>
                              )}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              </Card>
            </section>
          )}

          {/* =============================================
              GROUP 4: SYSTEM & VOLUME
             ============================================= */}
          {showSection('system') && (
            <section className="space-y-3">
              <p className="text-[11px] font-medium uppercase tracking-widest text-subtle px-1">
                {d.catSystem}
              </p>

              <Card className="divide-y divide-border overflow-hidden">
                {/* Collapsible Volume Preview */}
                <div>
                  <button
                    type="button"
                    onClick={() => togglePanel('volume')}
                    className="w-full flex items-center justify-between gap-3 px-4 py-3.5 text-start hover:bg-elevated transition-colors"
                  >
                    <span className="text-[14px] text-ink">{d.secVolume}</span>
                    <span className="inline-flex items-center gap-1.5 text-[12px] text-muted">
                      <span>
                        {volumes.muted
                          ? d.mute
                          : `${formatDigits(volumes.media, d.isArabic)}${d.percent}`}
                      </span>
                      {openPanel === 'volume' ? (
                        <ChevronUp className="w-4 h-4 text-subtle" strokeWidth={1.6} />
                      ) : (
                        <ChevronDown className="w-4 h-4 text-subtle" strokeWidth={1.6} />
                      )}
                    </span>
                  </button>
                  {openPanel === 'volume' && (
                    <div className="px-3.5 pb-4 pt-1 animate-fade-in">
                      <VolumePreviewCard />
                    </div>
                  )}
                </div>

                {/* Restart Setup */}
                <button
                  type="button"
                  onClick={restartSetup}
                  className="w-full flex items-center justify-between gap-3 px-4 py-3.5 text-start hover:bg-elevated transition-colors"
                >
                  <div>
                    <p className="text-[14px] font-medium text-ink">{d.restartSetup}</p>
                    <p className="text-[11px] text-muted mt-0.5">{d.restartSetupDesc}</p>
                  </div>
                  <RotateCcw className="w-4 h-4 text-subtle shrink-0" strokeWidth={1.6} />
                </button>

                {/* Reset App */}
                <button
                  type="button"
                  onClick={() => setConfirmResetApp(true)}
                  className="w-full flex items-center justify-between gap-3 px-4 py-3.5 text-start hover:bg-elevated transition-colors"
                >
                  <div>
                    <p className="text-[14px] font-medium text-danger">{d.resetApp}</p>
                    <p className="text-[11px] text-muted mt-0.5">{d.resetAppDesc}</p>
                  </div>
                  <Trash2 className="w-4 h-4 text-danger/80 shrink-0" strokeWidth={1.6} />
                </button>
              </Card>

              <p className="text-[11px] leading-relaxed text-subtle px-1">
                {d.webPlatformNote}
              </p>
            </section>
          )}
        </div>
      </BottomSheet>

      <ConfirmDialog
        open={confirmResetApp}
        title={d.confirmResetAppTitle}
        description={d.confirmResetAppDesc}
        confirmLabel={d.reset}
        cancelLabel={d.cancel}
        onConfirm={() => {
          resetApp();
          setConfirmResetApp(false);
        }}
        onCancel={() => setConfirmResetApp(false)}
      />
    </>
  );
};
