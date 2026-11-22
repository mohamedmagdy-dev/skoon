import React, { useEffect, useMemo, useState } from 'react';
import {
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  ArrowUp,
  Check,
  GripVertical,
  MapPin,
} from 'lucide-react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { getDict, TAB_META } from '../../design-system/i18n';
import { FONT_OPTIONS, getFontOption } from '../../design-system/fonts';
import {
  Button,
  Card,
  Checkbox,
  Chip,
  SearchInput,
} from '../../design-system/components';
import {
  findCity,
  findNearestCity,
  formatPrayerTime12h,
  getNextPrayer,
  PRAYER_CITIES,
} from '../../services/prayerTimes';
import { webPlatformAdapter } from '../../platform/platformAdapter';
import type { TabId } from '../../types';

const TOTAL_VISIBLE_STEPS = 7; // Steps 1..7 (Step 0 is Splash)

export const OnboardingFlow: React.FC = () => {
  const step = useSukoonStore((s) => s.onboardingStep);
  const setStep = useSukoonStore((s) => s.setOnboardingStep);
  const completeOnboarding = useSukoonStore((s) => s.completeOnboarding);
  const language = useSukoonStore((s) => s.language);
  const setLanguage = useSukoonStore((s) => s.setLanguage);
  const theme = useSukoonStore((s) => s.theme);
  const setTheme = useSukoonStore((s) => s.setTheme);
  const fontId = useSukoonStore((s) => s.fontId);
  const setFontId = useSukoonStore((s) => s.setFontId);
  const prayerCityId = useSukoonStore((s) => s.prayerCityId);
  const setPrayerCityId = useSukoonStore((s) => s.setPrayerCityId);
  const tabs = useSukoonStore((s) => s.tabs);
  const toggleTabEnabled = useSukoonStore((s) => s.toggleTabEnabled);
  const reorderTabs = useSukoonStore((s) => s.reorderTabs);

  const d = getDict(language);
  const [cityQuery, setCityQuery] = useState('');
  const [locating, setLocating] = useState(false);
  const [locError, setLocError] = useState(false);
  const [dragIdx, setDragIdx] = useState<number | null>(null);

  // Screen 01 — Splash auto-advance (short, calm, never artificial)
  useEffect(() => {
    if (step !== 0) return;
    const t = setTimeout(() => setStep(1), 1350);
    return () => clearTimeout(t);
  }, [step, setStep]);

  const selectedCity = useMemo(() => findCity(prayerCityId), [prayerCityId]);
  const nextPrayer = useMemo(() => getNextPrayer(prayerCityId), [prayerCityId]);
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

  const handleAutoLocate = async () => {
    setLocating(true);
    setLocError(false);
    const coords = await webPlatformAdapter.requestCoordinates();
    setLocating(false);
    if (coords) {
      const nearest = findNearestCity(coords.lat, coords.lng);
      setPrayerCityId(nearest.id, true);
    } else {
      setLocError(true);
    }
  };

  // Screen 01 — Minimalist OLED Splash
  if (step === 0) {
    return (
      <div
        onClick={() => setStep(1)}
        className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-[#09090B] text-[#F4F0E8] cursor-pointer select-none"
      >
        <div className="flex flex-col items-center gap-3 animate-splash">
          <div className="w-2.5 h-2.5 rounded-pill bg-[#D9B88C] mb-1" />
          <span className="font-amiri text-[44px] leading-none tracking-normal text-[#F4F0E8]">
            سكون
          </span>
          <span className="text-[12px] tracking-[0.34em] uppercase text-[#A19D95] font-system">
            SUKOON
          </span>
        </div>
      </div>
    );
  }

  const BackIcon = d.isArabic ? ArrowRight : ArrowLeft;
  const enabledTabs = tabs.filter((t) => t.enabled || t.id === 'home');
  const currentFont = getFontOption(fontId);

  return (
    <div className="min-h-[100dvh] w-full flex flex-col items-center bg-bg text-ink">
      <div className="w-full max-w-[460px] min-h-[100dvh] flex flex-col px-page-x pt-6 pb-8">
        {/* Top Progress Header (Steps 2..6) */}
        <div className="flex items-center justify-between min-h-[44px] mb-4">
          {step > 1 ? (
            <button
              type="button"
              onClick={() => setStep(step - 1)}
              className="inline-flex items-center gap-1.5 min-h-[44px] px-2 -ms-2 text-[14px] text-muted hover:text-ink transition-colors"
            >
              <BackIcon className="w-4 h-4" strokeWidth={1.6} />
              <span>{d.back}</span>
            </button>
          ) : (
            <span className="font-amiri text-[20px] text-accent">سكون</span>
          )}

          {step >= 1 && step <= TOTAL_VISIBLE_STEPS && (
            <div className="flex items-center gap-1.5" aria-label={d.obStepOf(step, TOTAL_VISIBLE_STEPS)}>
              {Array.from({ length: TOTAL_VISIBLE_STEPS }).map((_, idx) => {
                const active = idx + 1 === step;
                const passed = idx + 1 < step;
                return (
                  <span
                    key={idx}
                    className={`h-[3px] rounded-pill transition-all duration-200 ${
                      active
                        ? 'w-5 bg-accent'
                        : passed
                          ? 'w-2 bg-accent/45'
                          : 'w-2 bg-border'
                    }`}
                  />
                );
              })}
            </div>
          )}

          {step === 5 ? (
            <button
              type="button"
              onClick={() => setStep(6)}
              className="min-h-[44px] px-2 -me-2 text-[14px] text-muted hover:text-ink transition-colors"
            >
              {d.skip}
            </button>
          ) : (
            <span className="w-10" />
          )}
        </div>

        {/* Step Body */}
        <div className="flex-1 flex flex-col justify-between gap-6 animate-fade-in" key={step}>
          {/* Screen 02 — Welcome */}
          {step === 1 && (
            <>
              <div className="flex-1 flex flex-col justify-center gap-5 py-8">
                <p className="text-[12px] tracking-[0.2em] uppercase text-accent">
                  {d.obWelcomeEyebrow}
                </p>
                <h1 className="text-[32px] leading-[1.28] font-light tracking-tight text-ink">
                  {d.obWelcomeHeading}
                </h1>
                <p className="text-[16px] leading-relaxed text-muted max-w-[360px]">
                  {d.obWelcomeDesc}
                </p>
              </div>
              <div className="space-y-3">
                <Button variant="primary" size="lg" fullWidth onClick={() => setStep(2)}>
                  {d.obGetStarted}
                </Button>
              </div>
            </>
          )}

          {/* Screen 03 — Language Selection */}
          {step === 2 && (
            <>
              <div className="space-y-6">
                <div className="space-y-1.5">
                  <h1 className="text-[28px] font-light text-ink">{d.obLangTitle}</h1>
                  <p className="text-[14px] text-muted">{d.obLangSubtitle}</p>
                </div>

                <div className="space-y-3">
                  {(
                    [
                      {
                        code: 'ar' as const,
                        title: 'العربية',
                        sub: 'واجهة عربية كاملة من اليمين إلى اليسار (RTL)',
                      },
                      {
                        code: 'en' as const,
                        title: 'English',
                        sub: 'Clean left-to-right (LTR) interface',
                      },
                    ] as const
                  ).map((opt) => {
                    const selected = language === opt.code;
                    return (
                      <button
                        key={opt.code}
                        type="button"
                        onClick={() => setLanguage(opt.code)}
                        className={`w-full text-start p-4 rounded-lg border transition-colors flex items-center justify-between gap-3 ${
                          selected
                            ? 'bg-accent-soft border-accent'
                            : 'bg-surface border-border hover:border-border-strong'
                        }`}
                      >
                        <div>
                          <p className="text-[18px] font-medium text-ink">{opt.title}</p>
                          <p className="text-[13px] text-muted mt-0.5">{opt.sub}</p>
                        </div>
                        <span
                          className={`w-5 h-5 rounded-pill border flex items-center justify-center shrink-0 ${
                            selected
                              ? 'bg-accent border-accent text-bg'
                              : 'border-border-strong'
                          }`}
                        >
                          {selected && <Check className="w-3.5 h-3.5" strokeWidth={2.5} />}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>

              <Button variant="primary" size="lg" fullWidth onClick={() => setStep(3)}>
                {d.next}
              </Button>
            </>
          )}

          {/* Screen 04 — Choose Your Experience */}
          {step === 3 && (
            <>
              <div className="space-y-5">
                <div className="space-y-1.5">
                  <h1 className="text-[28px] font-light text-ink">{d.obExpTitle}</h1>
                  <p className="text-[14px] text-muted">{d.obExpSubtitle}</p>
                </div>

                {/* Productivity */}
                <div className="space-y-2">
                  <p className="text-[12px] tracking-wide uppercase text-muted">
                    {d.obProductivity}
                  </p>
                  <div className="space-y-2">
                    {(['pomodoro', 'notes', 'habits'] as TabId[]).map((id) => {
                      const meta = TAB_META[id];
                      const cfg = tabs.find((t) => t.id === id);
                      const checked = cfg?.enabled ?? true;
                      return (
                        <button
                          key={id}
                          type="button"
                          onClick={() => toggleTabEnabled(id)}
                          className={`w-full text-start p-3.5 rounded-lg border transition-colors flex items-center justify-between gap-3 ${
                            checked
                              ? 'bg-surface border-accent/55'
                              : 'bg-bg border-border opacity-70'
                          }`}
                        >
                          <div className="min-w-0">
                            <p className="text-[15px] font-medium text-ink">
                              {d.isArabic ? meta.labelAr : meta.labelEn}
                            </p>
                            <p className="text-[12px] text-muted mt-0.5">
                              {d.isArabic ? meta.descAr : meta.descEn}
                            </p>
                          </div>
                          <Checkbox checked={checked} />
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Spiritual */}
                <div className="space-y-2">
                  <p className="text-[12px] tracking-wide uppercase text-muted">
                    {d.obSpiritual}
                  </p>
                  <div className="space-y-2">
                    {(['quran', 'azkar', 'tasbih'] as TabId[]).map((id) => {
                      const meta = TAB_META[id];
                      const cfg = tabs.find((t) => t.id === id);
                      const checked = cfg?.enabled ?? true;
                      return (
                        <button
                          key={id}
                          type="button"
                          onClick={() => toggleTabEnabled(id)}
                          className={`w-full text-start p-3.5 rounded-lg border transition-colors flex items-center justify-between gap-3 ${
                            checked
                              ? 'bg-surface border-accent/55'
                              : 'bg-bg border-border opacity-70'
                          }`}
                        >
                          <div className="min-w-0">
                            <p className="text-[15px] font-medium text-ink">
                              {d.isArabic ? meta.labelAr : meta.labelEn}
                            </p>
                            <p className="text-[12px] text-muted mt-0.5">
                              {d.isArabic ? meta.descAr : meta.descEn}
                            </p>
                          </div>
                          <Checkbox checked={checked} />
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Selected Preview Strip */}
                <Card className="p-3 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-[12px] text-muted">{d.obSelectedPreview}</span>
                    <span className="text-[11px] text-accent">{d.obHomeAlwaysOn}</span>
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    {enabledTabs.map((t) => {
                      const m = TAB_META[t.id];
                      return (
                        <span
                          key={t.id}
                          className={`px-2.5 py-1 rounded-pill text-[12px] border ${
                            t.id === 'home'
                              ? 'bg-accent-soft text-accent border-accent/50'
                              : 'bg-elevated text-ink border-border'
                          }`}
                        >
                          {d.isArabic ? m.shortAr : m.shortEn}
                        </span>
                      );
                    })}
                  </div>
                </Card>
              </div>

              <Button variant="primary" size="lg" fullWidth onClick={() => setStep(4)}>
                {d.next}
              </Button>
            </>
          )}

          {/* Screen 05 — Appearance & Typography */}
          {step === 4 && (
            <>
              <div className="space-y-6">
                <div className="space-y-1.5">
                  <h1 className="text-[28px] font-light text-ink">{d.obAppearanceTitle}</h1>
                  <p className="text-[14px] text-muted">{d.obAppearanceSubtitle}</p>
                </div>

                {/* Live Sample Preview */}
                <Card className="p-5 space-y-2">
                  <p className="text-[11px] tracking-widest uppercase text-accent">
                    {d.isArabic ? currentFont.nameAr : currentFont.nameEn}
                  </p>
                  <p className="text-[22px] font-light text-ink leading-snug">
                    {currentFont.sampleAr}
                  </p>
                  <p className="text-[14px] text-muted">{currentFont.sampleEn}</p>
                </Card>

                {/* Theme */}
                <div className="space-y-2.5">
                  <p className="text-[12px] uppercase tracking-wide text-muted">
                    {d.obThemeLabel}
                  </p>
                  <div className="grid grid-cols-2 gap-2.5">
                    {(
                      [
                        { id: 'dark' as const, label: d.darkOled, bg: '#09090B', fg: '#F4F0E8' },
                        { id: 'light' as const, label: d.lightPaper, bg: '#F7F5F0', fg: '#242321' },
                      ] as const
                    ).map((t) => {
                      const active = theme === t.id;
                      return (
                        <button
                          key={t.id}
                          type="button"
                          onClick={() => setTheme(t.id)}
                          className={`p-3.5 rounded-lg border text-start transition-colors space-y-3 ${
                            active
                              ? 'border-accent bg-accent-soft'
                              : 'border-border bg-surface hover:border-border-strong'
                          }`}
                        >
                          <div
                            className="w-full h-8 rounded-sm border border-black/15 flex items-center px-2.5 gap-1.5"
                            style={{ backgroundColor: t.bg }}
                          >
                            <span
                              className="w-2 h-2 rounded-pill"
                              style={{ backgroundColor: '#D9B88C' }}
                            />
                            <span
                              className="w-10 h-1.5 rounded-pill opacity-70"
                              style={{ backgroundColor: t.fg }}
                            />
                          </div>
                          <span className="block text-[14px] font-medium text-ink">
                            {t.label}
                          </span>
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Fonts */}
                <div className="space-y-2.5">
                  <p className="text-[12px] uppercase tracking-wide text-muted">
                    {d.obFontLabel}
                  </p>
                  <div className="grid grid-cols-2 gap-2">
                    {FONT_OPTIONS.map((f) => {
                      const active = fontId === f.id;
                      return (
                        <button
                          key={f.id}
                          type="button"
                          onClick={() => setFontId(f.id)}
                          style={{ fontFamily: f.cssFamily }}
                          className={`min-h-[44px] px-3.5 py-2.5 rounded-md border text-[14px] transition-colors ${
                            active
                              ? 'bg-accent-soft border-accent text-accent font-medium'
                              : 'bg-surface border-border text-ink hover:border-border-strong'
                          }`}
                        >
                          {d.isArabic ? f.nameAr : f.nameEn}
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>

              <Button variant="primary" size="lg" fullWidth onClick={() => setStep(5)}>
                {d.next}
              </Button>
            </>
          )}

          {/* Screen 06 — Location */}
          {step === 5 && (
            <>
              <div className="space-y-4 flex-1 flex flex-col min-h-0">
                <div className="space-y-1.5">
                  <h1 className="text-[28px] font-light text-ink">{d.obLocationTitle}</h1>
                  <p className="text-[14px] text-muted">{d.obLocationSubtitle}</p>
                </div>

                {/* Selected City Preview */}
                <Card className="p-3.5 flex items-center justify-between gap-3 border-accent/40">
                  <div>
                    <p className="text-[15px] font-medium text-ink">
                      {d.isArabic
                        ? `${selectedCity.nameAr}، ${selectedCity.countryAr}`
                        : `${selectedCity.nameEn}, ${selectedCity.countryEn}`}
                    </p>
                    <p className="text-[12px] text-accent mt-0.5">
                      {d.isArabic
                        ? nextPrayer.nextPrayer.nameAr
                        : nextPrayer.nextPrayer.nameEn}{' '}
                      • {formatPrayerTime12h(nextPrayer.nextPrayer, d.isArabic)}
                    </p>
                  </div>
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={handleAutoLocate}
                    disabled={locating}
                  >
                    <MapPin className="w-3.5 h-3.5" strokeWidth={1.6} />
                    <span>{locating ? d.obLocating : d.obUseCurrentLocation}</span>
                  </Button>
                </Card>

                {locError && (
                  <p className="text-[12px] text-danger">{d.obLocationFailed}</p>
                )}

                <SearchInput
                  value={cityQuery}
                  onChange={setCityQuery}
                  placeholder={d.searchCityPlaceholder}
                />

                <div className="flex-1 max-h-[260px] overflow-y-auto rounded-lg border border-border bg-surface divide-y divide-border">
                  {filteredCities.map((c) => {
                    const active = c.id === prayerCityId;
                    return (
                      <button
                        key={c.id}
                        type="button"
                        onClick={() => setPrayerCityId(c.id, true)}
                        className={`w-full px-4 py-3 flex items-center justify-between text-start transition-colors ${
                          active ? 'bg-accent-soft text-accent' : 'hover:bg-elevated'
                        }`}
                      >
                        <span className="text-[14px] font-medium">
                          {d.isArabic ? c.nameAr : c.nameEn}
                        </span>
                        <span className="text-[12px] text-muted">
                          {d.isArabic ? c.countryAr : c.countryEn}
                          {active ? ' ✓' : ''}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>

              <Button variant="primary" size="lg" fullWidth onClick={() => setStep(6)}>
                {d.next}
              </Button>
            </>
          )}

          {/* Screen 07 — Customize Navigation (Drag & Drop + Arrows) */}
          {step === 6 && (
            <>
              <div className="space-y-4">
                <div className="space-y-1.5">
                  <h1 className="text-[28px] font-light text-ink">{d.obNavTitle}</h1>
                  <p className="text-[14px] text-muted">{d.obNavSubtitle}</p>
                </div>

                <div className="space-y-2">
                  {tabs.map((cfg, idx) => {
                    const meta = TAB_META[cfg.id];
                    const isHome = cfg.id === 'home';
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
                        className={`flex items-center justify-between gap-2 px-3 py-2.5 rounded-lg border transition-colors ${
                          isHome
                            ? 'bg-accent-soft border-accent/55'
                            : 'bg-surface border-border'
                        } ${dragIdx === idx ? 'opacity-50' : ''}`}
                      >
                        <div className="flex items-center gap-2.5 min-w-0">
                          <GripVertical
                            className="w-4 h-4 text-subtle shrink-0 cursor-grab"
                            strokeWidth={1.6}
                          />
                          <div className="min-w-0">
                            <p className="text-[14px] font-medium text-ink truncate">
                              {d.isArabic ? meta.labelAr : meta.labelEn}
                            </p>
                            {isHome && (
                              <p className="text-[11px] text-accent">{d.centerAnchor}</p>
                            )}
                          </div>
                        </div>

                        <div className="flex items-center gap-1 shrink-0">
                          <button
                            type="button"
                            aria-label="Move up"
                            disabled={idx === 0}
                            onClick={() => reorderTabs(idx, idx - 1)}
                            className="p-2 text-muted hover:text-ink disabled:opacity-25"
                          >
                            <ArrowUp className="w-4 h-4" strokeWidth={1.6} />
                          </button>
                          <button
                            type="button"
                            aria-label="Move down"
                            disabled={idx === tabs.length - 1}
                            onClick={() => reorderTabs(idx, idx + 1)}
                            className="p-2 text-muted hover:text-ink disabled:opacity-25"
                          >
                            <ArrowDown className="w-4 h-4" strokeWidth={1.6} />
                          </button>
                          {!isHome && (
                            <Chip
                              active={cfg.enabled}
                              onClick={() => toggleTabEnabled(cfg.id)}
                              className="!min-h-[30px] !px-2.5 !text-[11px]"
                            >
                              {cfg.enabled ? d.visible : d.hidden}
                            </Chip>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              <Button variant="primary" size="lg" fullWidth onClick={() => setStep(7)}>
                {d.next}
              </Button>
            </>
          )}

          {/* Screen 08 — Ready */}
          {step === 7 && (
            <>
              <div className="flex-1 flex flex-col justify-center gap-5 py-8">
                <div className="w-10 h-10 rounded-pill bg-accent-soft border border-accent/50 flex items-center justify-center text-accent">
                  <Check className="w-5 h-5" strokeWidth={1.8} />
                </div>
                <h1 className="text-[32px] leading-[1.25] font-light text-ink">
                  {d.obReadyHeading}
                </h1>
                <p className="text-[16px] leading-relaxed text-muted max-w-[340px]">
                  {d.obReadyDesc}
                </p>

                <Card className="p-4 space-y-2 mt-2">
                  <div className="flex items-center justify-between text-[13px]">
                    <span className="text-muted">{d.secAppearance}</span>
                    <span className="text-ink">
                      {theme === 'dark' ? d.darkOled : d.lightPaper} •{' '}
                      {d.isArabic ? currentFont.nameAr : currentFont.nameEn}
                    </span>
                  </div>
                  <div className="flex items-center justify-between text-[13px]">
                    <span className="text-muted">{d.secLocation}</span>
                    <span className="text-ink">
                      {d.isArabic ? selectedCity.nameAr : selectedCity.nameEn}
                    </span>
                  </div>
                </Card>
              </div>

              <Button
                variant="primary"
                size="lg"
                fullWidth
                onClick={completeOnboarding}
              >
                {d.obEnterSukoon}
              </Button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
