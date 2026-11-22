import React, { useState } from 'react';
import { Plus, RotateCw, X } from 'lucide-react';
import {
  calculateHabitStreak,
  getTodayIso,
  useSukoonStore,
} from '../../stores/useSukoonStore';
import { formatDigits, getDict } from '../../design-system/i18n';
import {
  Button,
  Checkbox,
  Chip,
  EmptyState,
  Input,
  ProgressBar,
  SectionHeader,
} from '../../design-system/components';

export const HabitsScreen: React.FC = () => {
  const language = useSukoonStore((s) => s.language);
  const habits = useSukoonStore((s) => s.habits);
  const toggleHabitToday = useSukoonStore((s) => s.toggleHabitToday);
  const addHabit = useSukoonStore((s) => s.addHabit);
  const deleteHabit = useSukoonStore((s) => s.deleteHabit);
  const triggerNewDay = useSukoonStore((s) => s.triggerNewDay);

  const d = getDict(language);
  const today = getTodayIso();
  const [newText, setNewText] = useState('');

  const total = habits.length;
  const doneCount = habits.filter((h) => h.completedDates.includes(today)).length;
  const pct = total > 0 ? Math.round((doneCount / total) * 100) : 0;

  const handleAdd = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newText.trim()) return;
    addHabit(newText);
    setNewText('');
  };

  return (
    <div className="h-full flex flex-col px-page-x pt-5 pb-3 gap-4">
      <SectionHeader
        eyebrow={d.habitsEyebrow}
        title={d.habitsTitle}
        action={
          <Chip onClick={triggerNewDay}>
            <RotateCw className="w-3.5 h-3.5" strokeWidth={1.7} />
            <span>{d.newDay}</span>
          </Chip>
        }
      />

      {/* Daily Progress */}
      <div className="space-y-2">
        <div className="flex items-center justify-between text-[12px]">
          <span className="text-muted">{d.habitsProgress(doneCount, total)}</span>
          <span className="font-medium text-accent tabular-nums">
            {formatDigits(pct, d.isArabic)}
            {d.percent}
          </span>
        </div>
        <ProgressBar progress={total > 0 ? doneCount / total : 0} />
      </div>

      {/* Checklist */}
      <div className="flex-1 min-h-0 overflow-y-auto space-y-2 pe-0.5">
        {habits.length === 0 ? (
          <EmptyState message={d.noHabitsEmpty} />
        ) : (
          habits.map((h) => {
            const done = h.completedDates.includes(today);
            const streak = calculateHabitStreak(h.completedDates, today);
            return (
              <div
                key={h.id}
                className={`flex items-center justify-between gap-3 px-4 py-3.5 rounded-lg border transition-colors ${
                  done
                    ? 'bg-accent-soft border-accent/35'
                    : 'bg-surface border-border hover:border-border-strong'
                }`}
              >
                <button
                  type="button"
                  onClick={() => toggleHabitToday(h.id)}
                  className="flex-1 flex items-center gap-3 text-start min-w-0"
                >
                  <Checkbox checked={done} />
                  <div className="min-w-0">
                    <p
                      className={`text-[15px] leading-snug truncate ${
                        done ? 'line-through text-muted' : 'text-ink'
                      }`}
                    >
                      {h.text}
                    </p>
                    <p className="text-[11px] text-accent mt-0.5">
                      {d.streakDays(streak)}
                    </p>
                  </div>
                </button>
                <button
                  type="button"
                  aria-label={d.delete}
                  onClick={() => deleteHabit(h.id)}
                  className="p-2 -me-2 text-subtle hover:text-danger transition-colors"
                >
                  <X className="w-4 h-4" strokeWidth={1.6} />
                </button>
              </div>
            );
          })
        )}
      </div>

      {/* Add Habit Bar */}
      <form onSubmit={handleAdd} className="flex items-center gap-2 pt-1">
        <Input
          value={newText}
          onChange={(e) => setNewText(e.target.value)}
          placeholder={d.addHabitPlaceholder}
        />
        <Button type="submit" variant="primary" size="md" className="shrink-0">
          <Plus className="w-4 h-4" strokeWidth={1.8} />
          <span>{d.add}</span>
        </Button>
      </form>
    </div>
  );
};
