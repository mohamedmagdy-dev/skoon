import React, { useEffect } from 'react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { getDict } from '../../design-system/i18n';
import {
  Button,
  CircularProgress,
  Input,
  SectionHeader,
  SegmentedControl,
  Toggle,
} from '../../design-system/components';
import type { PomodoroModeKey } from '../../types';

const MODE_MINUTES: Record<PomodoroModeKey, number> = {
  focus: 25,
  short: 5,
  long: 15,
};

export const PomodoroScreen: React.FC = () => {
  const language = useSukoonStore((s) => s.language);
  const pomodoro = useSukoonStore((s) => s.pomodoro);
  const setPomodoroMode = useSukoonStore((s) => s.setPomodoroMode);
  const startOrResumePomodoro = useSukoonStore((s) => s.startOrResumePomodoro);
  const pausePomodoro = useSukoonStore((s) => s.pausePomodoro);
  const resetPomodoro = useSukoonStore((s) => s.resetPomodoro);
  const syncPomodoroTick = useSukoonStore((s) => s.syncPomodoroTick);
  const setPomodoroTask = useSukoonStore((s) => s.setPomodoroTask);
  const setPomodoroAutoFocus = useSukoonStore((s) => s.setPomodoroAutoFocus);

  const d = getDict(language);

  // Accurate timestamp-based tick + visibilitychange resync
  useEffect(() => {
    if (!pomodoro.isRunning) return;
    syncPomodoroTick();
    const id = setInterval(syncPomodoroTick, 500);
    const onVisibility = () => {
      if (document.visibilityState === 'visible') syncPomodoroTick();
    };
    document.addEventListener('visibilitychange', onVisibility);
    return () => {
      clearInterval(id);
      document.removeEventListener('visibilitychange', onVisibility);
    };
  }, [pomodoro.isRunning, syncPomodoroTick]);

  const mins = Math.floor(pomodoro.remainingSeconds / 60);
  const secs = pomodoro.remainingSeconds % 60;
  const formatted = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  const progress =
    pomodoro.durationSeconds > 0
      ? pomodoro.remainingSeconds / pomodoro.durationSeconds
      : 1;

  const modeName =
    pomodoro.mode === 'short'
      ? d.pomoShort
      : pomodoro.mode === 'long'
        ? d.pomoLong
        : d.pomoFocus;

  const isPaused =
    !pomodoro.isRunning &&
    pomodoro.remainingSeconds > 0 &&
    pomodoro.remainingSeconds < pomodoro.durationSeconds;

  const statusText = pomodoro.isRunning
    ? d.pomoRunning(modeName)
    : isPaused
      ? d.pomoPaused(modeName)
      : d.pomoReady(modeName);

  return (
    <div className="h-full flex flex-col justify-between px-page-x pt-5 pb-3 gap-4">
      <SectionHeader
        eyebrow={d.pomoEyebrow}
        title={d.pomoTitle}
        action={
          <span className="text-[12px] font-medium text-accent">
            {d.sessionsToday(pomodoro.sessionsToday)}
          </span>
        }
      />

      <SegmentedControl
        value={pomodoro.mode}
        onChange={(mode) => setPomodoroMode(mode, MODE_MINUTES[mode])}
        options={[
          { value: 'focus', label: d.pomoFocusChip },
          { value: 'short', label: d.pomoShortChip },
          { value: 'long', label: d.pomoLongChip },
        ]}
      />

      {/* Circular Countdown Ring */}
      <div className="flex-1 flex items-center justify-center py-2">
        <CircularProgress size={220} strokeWidth={3.5} progress={progress}>
          <span className="text-[48px] font-extralight leading-none tracking-[-0.03em] text-ink tabular-nums">
            {formatted}
          </span>
          <span className="text-[12px] text-muted mt-2">{statusText}</span>
        </CircularProgress>
      </div>

      {/* Intention Input */}
      <Input
        value={pomodoro.task}
        onChange={(e) => setPomodoroTask(e.target.value)}
        placeholder={d.pomoTaskPlaceholder}
        className="text-center"
      />

      {/* Primary / Secondary Actions */}
      <div className="flex items-center gap-2.5">
        <Button
          variant="primary"
          size="lg"
          fullWidth
          onClick={pomodoro.isRunning ? pausePomodoro : startOrResumePomodoro}
        >
          {pomodoro.isRunning ? d.pause : isPaused ? d.resume : d.start}
        </Button>
        <Button variant="secondary" size="lg" onClick={resetPomodoro}>
          {d.reset}
        </Button>
      </div>

      {/* Auto-Focus Toggle */}
      <div className="flex items-center justify-between gap-3 px-4 py-3 rounded-lg bg-surface border border-border">
        <span className="text-[13px] text-muted">{d.autoFocusLabel}</span>
        <Toggle
          checked={pomodoro.autoFocus}
          onChange={setPomodoroAutoFocus}
          label={d.autoFocusLabel}
        />
      </div>
    </div>
  );
};
