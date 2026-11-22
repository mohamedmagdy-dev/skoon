import React, { useState } from 'react';
import { Minus, Moon, Plus, RotateCcw, Trash2 } from 'lucide-react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { formatDigits, getDict } from '../../design-system/i18n';
import {
  Button,
  Card,
  Chip,
  CircularProgress,
  ConfirmDialog,
  Input,
  SectionHeader,
} from '../../design-system/components';

export const TasbihScreen: React.FC = () => {
  const language = useSukoonStore((s) => s.language);
  const adhkar = useSukoonStore((s) => s.adhkar);
  const activeDhikrId = useSukoonStore((s) => s.activeDhikrId);
  const selectDhikr = useSukoonStore((s) => s.selectDhikr);
  const incrementDhikr = useSukoonStore((s) => s.incrementDhikr);
  const adjustActiveDhikrTarget = useSukoonStore((s) => s.adjustActiveDhikrTarget);
  const resetActiveDhikr = useSukoonStore((s) => s.resetActiveDhikr);
  const addCustomDhikr = useSukoonStore((s) => s.addCustomDhikr);
  const deleteActiveDhikr = useSukoonStore((s) => s.deleteActiveDhikr);
  const setBlackScreenSource = useSukoonStore((s) => s.setBlackScreenSource);

  const d = getDict(language);
  const active = adhkar.find((item) => item.id === activeDhikrId) ?? adhkar[0];

  const [showAdd, setShowAdd] = useState(false);
  const [newText, setNewText] = useState('');
  const [newTarget, setNewTarget] = useState('33');
  const [confirmReset, setConfirmReset] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const progress = active && active.target > 0 ? active.count / active.target : 0;
  const isDone = active ? active.count >= active.target : false;

  const handleAddSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newText.trim()) return;
    addCustomDhikr(newText, Number(newTarget) || 33);
    setNewText('');
    setNewTarget('33');
    setShowAdd(false);
  };

  return (
    <div className="h-full flex flex-col px-page-x pt-5 pb-3 gap-3.5">
      <SectionHeader
        eyebrow={d.tasbihEyebrow}
        title={d.tasbihTitle}
        action={
          <Chip active onClick={() => setShowAdd((v) => !v)}>
            <Plus className="w-3.5 h-3.5" strokeWidth={1.8} />
            <span>{d.addDhikr}</span>
          </Chip>
        }
      />

      {/* AMOLED Black-Screen Mode Banner */}
      <button
        type="button"
        onClick={() => setBlackScreenSource('tasbih')}
        className="w-full flex items-center justify-between gap-3 px-4 py-3 rounded-lg bg-accent-soft border border-accent/40 text-start hover:border-accent transition-colors"
      >
        <div className="flex items-center gap-3 min-w-0">
          <Moon className="w-4 h-4 text-accent shrink-0" strokeWidth={1.7} />
          <div className="min-w-0">
            <p className="text-[13px] font-medium text-ink truncate">
              {d.blackScreenBannerTitle}
            </p>
            <p className="text-[11px] text-muted truncate">{d.blackScreenBannerSub}</p>
          </div>
        </div>
        <span className="text-[12px] text-accent shrink-0">{d.open} ›</span>
      </button>

      {/* Add Custom Dhikr Form */}
      {showAdd && (
        <Card className="p-3.5 space-y-3 animate-fade-in">
          <form onSubmit={handleAddSubmit} className="space-y-2.5">
            <Input
              value={newText}
              onChange={(e) => setNewText(e.target.value)}
              placeholder={d.dhikrPlaceholder}
              autoFocus
            />
            <div className="flex items-center gap-2">
              <span className="text-[12px] text-muted shrink-0">{d.target}:</span>
              <Input
                type="number"
                min={1}
                max={10000}
                value={newTarget}
                onChange={(e) => setNewTarget(e.target.value)}
                className="!w-24 text-center !min-h-[38px]"
              />
              <div className="flex-1" />
              <Button type="button" variant="ghost" size="sm" onClick={() => setShowAdd(false)}>
                {d.cancel}
              </Button>
              <Button type="submit" variant="primary" size="sm">
                {d.save}
              </Button>
            </div>
          </form>
        </Card>
      )}

      {/* Dhikr Selector Pills */}
      <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar pb-0.5">
        {adhkar.map((item) => {
          const selected = item.id === active?.id;
          const done = item.count >= item.target;
          return (
            <Chip
              key={item.id}
              active={selected}
              success={!selected && done}
              onClick={() => selectDhikr(item.id)}
            >
              {item.short}
              {done ? ' ✓' : ''}
            </Chip>
          );
        })}
      </div>

      {/* Tactile Counter Stage */}
      {active && (
        <>
          <button
            type="button"
            onClick={incrementDhikr}
            className="flex-1 min-h-0 w-full flex flex-col items-center justify-evenly py-4 rounded-xl focus:outline-none active:scale-[0.99] transition-transform select-none"
          >
            <p
              dir="rtl"
              className="font-amiri text-[30px] leading-relaxed text-ink text-center px-3"
            >
              {active.text}
            </p>

            <CircularProgress
              size={184}
              strokeWidth={4}
              progress={progress}
              color={isDone ? 'var(--color-success)' : 'var(--color-accent)'}
            >
              <span
                className={`text-[46px] font-extralight leading-none tabular-nums ${
                  isDone ? 'text-success' : 'text-accent'
                }`}
              >
                {formatDigits(active.count, d.isArabic)}
              </span>
              <span className="text-[13px] text-muted mt-1.5">
                {d.ofTarget(formatDigits(active.target, d.isArabic))}
              </span>
            </CircularProgress>

            <p className="text-[12px] text-subtle">{d.tapAnywhereHint}</p>
          </button>

          {/* Target & Safe Reset Controls */}
          <div className="flex items-center justify-center gap-2 pt-1">
            <Chip
              onClick={() => adjustActiveDhikrTarget(-1)}
              aria-label="Decrease target"
            >
              <Minus className="w-3.5 h-3.5" strokeWidth={1.7} />
            </Chip>
            <Chip
              onClick={() => adjustActiveDhikrTarget(1)}
              aria-label="Increase target"
            >
              <Plus className="w-3.5 h-3.5" strokeWidth={1.7} />
            </Chip>
            <Chip
              onClick={() => {
                if (active.count > 0) setConfirmReset(true);
              }}
            >
              <RotateCcw className="w-3.5 h-3.5" strokeWidth={1.7} />
              <span>{d.reset}</span>
            </Chip>
            {adhkar.length > 1 && (
              <Chip danger onClick={() => setConfirmDelete(true)} aria-label={d.delete}>
                <Trash2 className="w-3.5 h-3.5" strokeWidth={1.7} />
              </Chip>
            )}
          </div>
        </>
      )}

      <ConfirmDialog
        open={confirmReset}
        title={d.confirmResetTitle}
        description={d.confirmResetDesc}
        confirmLabel={d.reset}
        cancelLabel={d.cancel}
        danger={false}
        onConfirm={() => {
          resetActiveDhikr();
          setConfirmReset(false);
        }}
        onCancel={() => setConfirmReset(false)}
      />

      <ConfirmDialog
        open={confirmDelete}
        title={d.confirmDeleteDhikrTitle}
        description={d.confirmDeleteDhikrDesc}
        confirmLabel={d.delete}
        cancelLabel={d.cancel}
        onConfirm={() => {
          deleteActiveDhikr();
          setConfirmDelete(false);
        }}
        onCancel={() => setConfirmDelete(false)}
      />
    </div>
  );
};
