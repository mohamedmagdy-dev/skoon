import React, { useEffect, useState } from 'react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { formatDigits, getDict } from '../../design-system/i18n';
import { Button, CircularProgress } from '../../design-system/components';

export const MindfulPauseModal: React.FC = () => {
  const targetApp = useSukoonStore((s) => s.mindfulTargetApp);
  const pauseDuration = useSukoonStore((s) => s.mindfulPauseSeconds);
  const dismissMindfulPause = useSukoonStore((s) => s.dismissMindfulPause);
  const proceedMindfulApp = useSukoonStore((s) => s.proceedMindfulApp);
  const language = useSukoonStore((s) => s.language);

  const d = getDict(language);
  const [secondsLeft, setSecondsLeft] = useState(pauseDuration);

  useEffect(() => {
    if (!targetApp) return;
    setSecondsLeft(pauseDuration);
    const endAt = Date.now() + pauseDuration * 1000;
    const id = setInterval(() => {
      const rem = Math.max(0, Math.ceil((endAt - Date.now()) / 1000));
      setSecondsLeft(rem);
      if (rem <= 0) clearInterval(id);
    }, 200);
    return () => clearInterval(id);
  }, [targetApp, pauseDuration]);

  if (!targetApp) return null;

  const appLabel = d.isArabic && targetApp.nameAr ? targetApp.nameAr : targetApp.name;
  const progress = pauseDuration > 0 ? 1 - secondsLeft / pauseDuration : 1;

  return (
    <div
      role="dialog"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-[var(--color-overlay)] animate-fade-in"
    >
      <div className="w-full max-w-[340px] flex flex-col items-center text-center gap-5 animate-slide-up">
        <CircularProgress size={108} strokeWidth={3} progress={progress}>
          <span className="text-[32px] font-extralight text-accent tabular-nums">
            {secondsLeft > 0 ? formatDigits(secondsLeft, d.isArabic) : '✓'}
          </span>
        </CircularProgress>

        <div className="space-y-2">
          <h2 className="text-[22px] font-normal text-ink">
            {d.beforeOpening(appLabel)}
          </h2>
          <p className="text-[14px] leading-relaxed text-muted">
            {d.mindfulPrompt(appLabel)}
          </p>
        </div>

        <div className="w-full space-y-2.5 pt-2">
          <Button variant="primary" size="lg" fullWidth onClick={dismissMindfulPause}>
            {d.returnToCalm}
          </Button>
          <Button
            variant="secondary"
            size="lg"
            fullWidth
            disabled={secondsLeft > 0}
            onClick={proceedMindfulApp}
          >
            {secondsLeft > 0 ? d.waitSeconds(secondsLeft) : d.openApp(appLabel)}
          </Button>
        </div>
      </div>
    </div>
  );
};
