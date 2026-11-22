import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { formatDigits, getDict } from '../../design-system/i18n';
import { loadQuranPages } from '../../services/quranService';
import { Chip, Input, LoadingState } from '../../design-system/components';
import type { QuranPage } from '../../types';

const LINE_HEIGHT_PRESETS = [
  { value: 1.85, key: 'spacingCompact' as const },
  { value: 2.15, key: 'spacingComfortable' as const },
  { value: 2.45, key: 'spacingWide' as const },
  { value: 2.75, key: 'spacingExtraWide' as const },
];

export const QuranScreen: React.FC = () => {
  const language = useSukoonStore((s) => s.language);
  const quranPage = useSukoonStore((s) => s.quranPage);
  const quranFontOffset = useSukoonStore((s) => s.quranFontOffset);
  const quranLineHeight = useSukoonStore((s) => s.quranLineHeight);
  const quranPaperMode = useSukoonStore((s) => s.quranPaperMode);
  const goToQuranPage = useSukoonStore((s) => s.goToQuranPage);
  const adjustQuranFontOffset = useSukoonStore((s) => s.adjustQuranFontOffset);
  const setQuranLineHeight = useSukoonStore((s) => s.setQuranLineHeight);
  const toggleQuranPaperMode = useSukoonStore((s) => s.toggleQuranPaperMode);

  const d = getDict(language);
  const [pages, setPages] = useState<QuranPage[] | null>(null);
  const [error, setError] = useState(false);
  const [showControls, setShowControls] = useState(false);
  const [jumpValue, setJumpValue] = useState('');
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let active = true;
    loadQuranPages()
      .then((data) => {
        if (active) setPages(data);
      })
      .catch(() => {
        if (active) setError(true);
      });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: 0 });
  }, [quranPage]);

  const currentPage = useMemo(() => {
    if (!pages || pages.length === 0) return null;
    const idx = Math.max(0, Math.min(pages.length - 1, quranPage - 1));
    return pages[idx];
  }, [pages, quranPage]);

  const effectiveFontPx = useMemo(() => {
    const chars = currentPage?.a.reduce((acc, a) => acc + a.t.length, 0) ?? 600;
    const base =
      chars <= 420 ? 23 : chars <= 700 ? 20 : chars <= 950 ? 18 : chars <= 1180 ? 16.5 : 15.5;
    return Math.max(13, Math.min(30, base + quranFontOffset));
  }, [currentPage, quranFontOffset]);

  const blocks = useMemo(() => {
    if (!currentPage) return [];
    const result: { banner: string; basmala: boolean; ayahs: { n: number; t: string }[] }[] = [];
    for (const ayah of currentPage.a) {
      if ((ayah.sn && ayah.sn.length > 0) || result.length === 0) {
        result.push({
          banner: ayah.sn ?? '',
          basmala: Boolean(ayah.sb),
          ayahs: [],
        });
      }
      result[result.length - 1].ayahs.push({ n: ayah.n, t: ayah.t });
    }
    return result;
  }, [currentPage]);

  const handleJump = (e: React.FormEvent) => {
    e.preventDefault();
    const num = Number(jumpValue);
    if (num >= 1 && num <= 604) {
      goToQuranPage(num);
      setShowControls(false);
    }
  };

  return (
    <div className="h-full flex flex-col px-page-x pt-4 pb-3 gap-3">
      {/* Slim Header */}
      <div className="flex items-center justify-between gap-2">
        <button
          type="button"
          onClick={() => {
            setJumpValue(String(quranPage));
            setShowControls((v) => !v);
          }}
          className="text-start min-w-0"
        >
          <p className="text-[16px] font-medium text-ink truncate">
            {currentPage?.s ?? d.holyQuran}
          </p>
          <p className="text-[12px] text-muted">
            {d.juzPage(currentPage?.j ?? 1, quranPage)}
          </p>
        </button>

        <div className="flex items-center gap-1.5 shrink-0">
          <Chip onClick={() => adjustQuranFontOffset(-1.5)} className="!px-2.5 !min-h-[34px]">
            {d.isArabic ? 'أ-' : 'A-'}
          </Chip>
          <Chip onClick={() => adjustQuranFontOffset(1.5)} className="!px-2.5 !min-h-[34px]">
            {d.isArabic ? 'أ+' : 'A+'}
          </Chip>
          <Chip
            active={quranPaperMode}
            onClick={toggleQuranPaperMode}
            className="!px-3 !min-h-[34px]"
          >
            {d.paperMode}
          </Chip>
        </div>
      </div>

      {/* Expandable Jump + Line-Spacing Bar */}
      {showControls && (
        <div className="p-3 rounded-lg bg-surface border border-border space-y-3 animate-fade-in">
          <form onSubmit={handleJump} className="flex items-center gap-2">
            <Input
              type="number"
              min={1}
              max={604}
              value={jumpValue}
              onChange={(e) => setJumpValue(e.target.value)}
              placeholder={d.jumpToPage}
              className="!min-h-[38px] !py-1.5 text-center"
            />
            <Chip active type="submit" className="!min-h-[38px] !px-4">
              {d.go}
            </Chip>
          </form>

          <div className="space-y-1.5">
            <p className="text-[11px] text-muted">{d.lineSpacing}</p>
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
        </div>
      )}

      {/* Full-Height Mushaf Sheet (ALWAYS RTL, Amiri) */}
      <div
        ref={scrollRef}
        dir="rtl"
        className={`flex-1 min-h-0 overflow-y-auto rounded-xl border p-5 transition-colors ${
          quranPaperMode
            ? 'mushaf-paper'
            : 'bg-mushaf text-mushaf-ink border-[var(--color-mushaf-border)]'
        }`}
      >
        {error ? (
          <div className="h-full flex items-center justify-center text-[14px] text-muted">
            {d.loadingMushaf}
          </div>
        ) : !currentPage ? (
          <div className="h-full flex items-center justify-center">
            <LoadingState message={d.loadingMushaf} />
          </div>
        ) : (
          <div className="min-h-full flex flex-col justify-center space-y-3">
            {blocks.map((block, bIdx) => (
              <div key={bIdx} className="space-y-2.5">
                {block.banner && (
                  <div className="py-1.5 px-3 rounded-sm border border-[var(--color-mushaf-border)] bg-accent-soft text-center">
                    <span className="font-amiri text-[18px] font-bold text-accent">
                      {block.banner}
                    </span>
                  </div>
                )}
                {block.basmala && (
                  <p
                    className="font-amiri text-center text-accent"
                    style={{
                      fontSize: `${Math.max(14, effectiveFontPx - 1)}px`,
                      lineHeight: quranLineHeight,
                    }}
                  >
                    بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
                  </p>
                )}
                <p
                  className="font-amiri text-justify"
                  style={{
                    fontSize: `${effectiveFontPx}px`,
                    lineHeight: quranLineHeight,
                  }}
                >
                  {block.ayahs.map((ayah) => (
                    <React.Fragment key={ayah.n}>
                      {ayah.t}{' '}
                      <span className="text-accent select-none">
                        ﴿{formatDigits(ayah.n, true)}﴾{' '}
                      </span>
                    </React.Fragment>
                  ))}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Bottom Page Navigation */}
      <div className="flex items-center gap-2">
        <button
          type="button"
          disabled={quranPage <= 1}
          onClick={() => goToQuranPage(quranPage - 1)}
          className="flex-1 min-h-[42px] rounded-md bg-surface border border-border text-[13px] text-ink hover:bg-elevated disabled:opacity-35 transition-colors"
        >
          {d.prevPage}
        </button>

        <button
          type="button"
          onClick={() => {
            setJumpValue(String(quranPage));
            setShowControls((v) => !v);
          }}
          className="min-h-[42px] px-4 rounded-md bg-accent-soft border border-accent/45 text-[13px] font-medium text-accent tabular-nums"
        >
          {formatDigits(quranPage, d.isArabic)} / {formatDigits(604, d.isArabic)}
        </button>

        <button
          type="button"
          disabled={quranPage >= 604}
          onClick={() => goToQuranPage(quranPage + 1)}
          className="flex-1 min-h-[42px] rounded-md bg-surface border border-border text-[13px] text-ink hover:bg-elevated disabled:opacity-35 transition-colors"
        >
          {d.nextPage}
        </button>
      </div>
    </div>
  );
};
