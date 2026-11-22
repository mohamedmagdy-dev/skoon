import React, { useEffect, useRef } from 'react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { formatDigits, getDict } from '../../design-system/i18n';
import { isCurrentlyMorning } from '../../services/azkarData';

export const BlackScreenModal: React.FC = () => {
  const source = useSukoonStore((s) => s.blackScreenSource);
  const setBlackScreenSource = useSukoonStore((s) => s.setBlackScreenSource);
  const language = useSukoonStore((s) => s.language);
  const adhkar = useSukoonStore((s) => s.adhkar);
  const activeDhikrId = useSukoonStore((s) => s.activeDhikrId);
  const incrementDhikr = useSukoonStore((s) => s.incrementDhikr);
  const morningAzkar = useSukoonStore((s) => s.morningAzkar);
  const eveningAzkar = useSukoonStore((s) => s.eveningAzkar);
  const morningIdx = useSukoonStore((s) => s.morningAzkarIndex);
  const eveningIdx = useSukoonStore((s) => s.eveningAzkarIndex);
  const incrementAzkar = useSukoonStore((s) => s.incrementAzkar);

  const d = getDict(language);
  const longPressTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const longPressedRef = useRef(false);

  useEffect(() => {
    if (!source) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setBlackScreenSource(null);
      if (e.key === ' ' || e.key === 'Enter') {
        e.preventDefault();
        if (source === 'azkar') incrementAzkar(isCurrentlyMorning());
        else incrementDhikr();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [source, setBlackScreenSource, incrementAzkar, incrementDhikr]);

  if (!source) return null;

  const isMorning = isCurrentlyMorning();
  let summary = '';
  if (source === 'azkar') {
    const list = isMorning ? morningAzkar : eveningAzkar;
    const cur = list[isMorning ? morningIdx : eveningIdx];
    if (cur) {
      summary = `${cur.title} • ${formatDigits(cur.count, d.isArabic)} / ${formatDigits(
        cur.target,
        d.isArabic
      )}`;
    }
  } else {
    const active = adhkar.find((a) => a.id === activeDhikrId) ?? adhkar[0];
    if (active) {
      summary = `${active.short} • ${formatDigits(active.count, d.isArabic)} / ${formatDigits(
        active.target,
        d.isArabic
      )}`;
    }
  }

  const handlePointerDown = () => {
    longPressedRef.current = false;
    longPressTimer.current = setTimeout(() => {
      longPressedRef.current = true;
      setBlackScreenSource(null);
    }, 650);
  };

  const handlePointerUp = () => {
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current);
      longPressTimer.current = null;
    }
    if (!longPressedRef.current) {
      if (source === 'azkar') incrementAzkar(isMorning);
      else incrementDhikr();
    }
  };

  const handlePointerCancel = () => {
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current);
      longPressTimer.current = null;
    }
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      onPointerDown={handlePointerDown}
      onPointerUp={handlePointerUp}
      onPointerLeave={handlePointerCancel}
      onPointerCancel={handlePointerCancel}
      className="fixed inset-0 z-50 bg-[#000000] flex flex-col items-center justify-end pb-10 px-6 select-none cursor-pointer animate-fade-in"
    >
      <div className="flex flex-col items-center gap-1.5 text-center pointer-events-none">
        {summary && (
          <p className="text-[13px] text-white/25 tracking-wide">{summary}</p>
        )}
        <p className="text-[11px] text-white/18">{d.longPressToExit}</p>
        <p className="text-[10px] text-white/12 max-w-[300px] mt-1">
          {d.blackScreenWebNote}
        </p>
      </div>
    </div>
  );
};
