import React, { useState } from 'react';
import { Moon, RotateCcw } from 'lucide-react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { formatDigits, getDict } from '../../design-system/i18n';
import { isCurrentlyMorning } from '../../services/azkarData';
import {
  Chip,
  ProgressBar,
  SectionHeader,
  SegmentedControl,
} from '../../design-system/components';

export const AzkarScreen: React.FC = () => {
  const language = useSukoonStore((s) => s.language);
  const morningAzkar = useSukoonStore((s) => s.morningAzkar);
  const eveningAzkar = useSukoonStore((s) => s.eveningAzkar);
  const morningIdx = useSukoonStore((s) => s.morningAzkarIndex);
  const eveningIdx = useSukoonStore((s) => s.eveningAzkarIndex);
  const selectAzkarIndex = useSukoonStore((s) => s.selectAzkarIndex);
  const incrementAzkar = useSukoonStore((s) => s.incrementAzkar);
  const resetAzkar = useSukoonStore((s) => s.resetAzkar);
  const setBlackScreenSource = useSukoonStore((s) => s.setBlackScreenSource);

  const d = getDict(language);
  const [period, setPeriod] = useState<'morning' | 'evening'>(() =>
    isCurrentlyMorning() ? 'morning' : 'evening'
  );

  const isMorning = period === 'morning';
  const list = isMorning ? morningAzkar : eveningAzkar;
  const idx = Math.max(0, Math.min(list.length - 1, isMorning ? morningIdx : eveningIdx));
  const current = list[idx];
  const completedCount = list.filter((z) => z.count >= z.target).length;

  return (
    <div className="h-full flex flex-col px-page-x pt-5 pb-3 gap-3.5">
      {/* Header + Switcher */}
      <SectionHeader
        eyebrow={isMorning ? d.autoMorning : d.autoEvening}
        title={isMorning ? d.morningAzkar : d.eveningAzkar}
      />

      <SegmentedControl
        value={period}
        onChange={setPeriod}
        options={[
          { value: 'morning', label: d.morning },
          { value: 'evening', label: d.evening },
        ]}
      />

      {/* Progress & Actions */}
      <div className="space-y-2">
        <div className="flex items-center justify-between gap-2">
          <span className="text-[12px] text-muted">
            {d.azkarCompleted(completedCount, list.length)}
          </span>
          <div className="flex items-center gap-1.5">
            <Chip
              active
              onClick={() => setBlackScreenSource('azkar')}
              className="!min-h-[32px] !px-2.5 !text-[12px]"
            >
              <Moon className="w-3.5 h-3.5" strokeWidth={1.7} />
              <span>{d.blackScreen}</span>
            </Chip>
            <Chip
              onClick={() => resetAzkar(isMorning)}
              className="!min-h-[32px] !px-2.5 !text-[12px]"
            >
              <RotateCcw className="w-3.5 h-3.5" strokeWidth={1.7} />
              <span>{d.reset}</span>
            </Chip>
          </div>
        </div>
        <ProgressBar progress={list.length ? completedCount / list.length : 0} />
      </div>

      {/* Main Zikr Card (Large Tap Target) */}
      {current && (
        <button
          type="button"
          onClick={() => incrementAzkar(isMorning)}
          className={`flex-1 min-h-0 w-full flex flex-col justify-between rounded-xl border p-5 text-center transition-colors active:scale-[0.995] ${
            current.count >= current.target
              ? 'bg-mushaf border-success/50'
              : 'bg-mushaf border-[var(--color-mushaf-border)]'
          }`}
        >
          <div className="w-full flex items-center justify-between text-[12px]">
            <span className="font-medium text-accent">{current.title}</span>
            <span className="text-muted tabular-nums">
              {formatDigits(idx + 1, d.isArabic)} / {formatDigits(list.length, d.isArabic)}
            </span>
          </div>

          <div
            dir="rtl"
            className="flex-1 w-full overflow-y-auto flex items-center justify-center py-4"
          >
            <p
              className="font-amiri text-mushaf-ink whitespace-pre-line"
              style={{
                fontSize: current.text.length > 260 ? '20px' : '25px',
                lineHeight: 2.05,
              }}
            >
              {current.text}
            </p>
          </div>

          <div className="w-full flex justify-center pt-1">
            <span
              className={`inline-flex items-center gap-2 px-5 py-2.5 rounded-pill border text-[13px] font-medium transition-colors ${
                current.count >= current.target
                  ? 'bg-[var(--color-success-soft)] border-success text-success'
                  : 'bg-accent-soft border-accent/60 text-accent'
              }`}
            >
              <span
                className={`w-2 h-2 rounded-pill ${
                  current.count >= current.target ? 'bg-success' : 'bg-accent'
                }`}
              />
              {current.count >= current.target
                ? d.completedCheck
                : `${formatDigits(current.count, d.isArabic)} / ${formatDigits(
                    current.target,
                    d.isArabic
                  )}  •  ${d.tapToCount}`}
            </span>
          </div>
        </button>
      )}

      {/* Prev / Next */}
      <div className="flex items-center gap-2">
        <button
          type="button"
          disabled={idx <= 0}
          onClick={() => selectAzkarIndex(isMorning, idx - 1)}
          className="flex-1 min-h-[42px] rounded-md bg-surface border border-border text-[13px] text-ink hover:bg-elevated disabled:opacity-35 transition-colors"
        >
          {d.prevZikr}
        </button>
        <button
          type="button"
          disabled={idx >= list.length - 1}
          onClick={() => selectAzkarIndex(isMorning, idx + 1)}
          className="flex-1 min-h-[42px] rounded-md bg-surface border border-border text-[13px] text-ink hover:bg-elevated disabled:opacity-35 transition-colors"
        >
          {d.nextZikr}
        </button>
      </div>
    </div>
  );
};
