import React, { useMemo, useState } from 'react';
import { Star } from 'lucide-react';
import { useSukoonStore } from '../../stores/useSukoonStore';
import { getDict } from '../../design-system/i18n';
import { webPlatformAdapter } from '../../platform/platformAdapter';
import { AppIconBadge } from '../../design-system/AppIconBadge';
import { BottomSheet, Chip, SearchInput } from '../../design-system/components';

export const AppDrawerSheet: React.FC = () => {
  const activeOverlay = useSukoonStore((s) => s.activeOverlay);
  const setActiveOverlay = useSukoonStore((s) => s.setActiveOverlay);
  const language = useSukoonStore((s) => s.language);
  const appDisplayStyle = useSukoonStore((s) => s.appDisplayStyle);
  const favoriteAppIds = useSukoonStore((s) => s.favoriteAppIds);
  const distractingAppIds = useSukoonStore((s) => s.distractingAppIds);
  const openApp = useSukoonStore((s) => s.openApp);
  const toggleFavoriteApp = useSukoonStore((s) => s.toggleFavoriteApp);
  const toggleDistractingApp = useSukoonStore((s) => s.toggleDistractingApp);

  const d = getDict(language);
  const [query, setQuery] = useState('');

  const apps = useMemo(() => {
    const list = webPlatformAdapter.getInstalledApps();
    const sorted = [...list].sort((a, b) => {
      const la = d.isArabic && a.nameAr ? a.nameAr : a.name;
      const lb = d.isArabic && b.nameAr ? b.nameAr : b.name;
      return la.localeCompare(lb, d.isArabic ? 'ar' : 'en');
    });
    const q = query.trim().toLowerCase();
    if (!q) return sorted;
    return sorted.filter(
      (a) =>
        a.name.toLowerCase().includes(q) ||
        (a.nameAr && a.nameAr.includes(q))
    );
  }, [query, d.isArabic]);

  return (
    <BottomSheet
      open={activeOverlay === 'app-drawer'}
      onClose={() => setActiveOverlay(null)}
      title={d.appDrawerTitle}
      headerRight={
        <Chip onClick={() => setActiveOverlay(null)}>{d.close}</Chip>
      }
    >
      <div className="space-y-3">
        <SearchInput
          value={query}
          onChange={setQuery}
          placeholder={d.searchAppsPlaceholder}
          autoFocus
        />

        <div className="flex flex-col gap-1 text-[11px] text-subtle">
          <p>{d.drawerLegend}</p>
          <p className="opacity-80">{d.appDrawerWebNote}</p>
        </div>

        <div className="divide-y divide-border/60 pt-1">
          {apps.map((app) => {
            const isFav = favoriteAppIds.includes(app.id);
            const isDist = distractingAppIds.includes(app.id);
            const label = d.isArabic && app.nameAr ? app.nameAr : app.name;

            return (
              <div
                key={app.id}
                className="flex items-center justify-between gap-2 py-2.5"
              >
                <button
                  type="button"
                  onClick={() => openApp(app)}
                  className="flex-1 flex items-center gap-3 text-start py-1 text-[17px] font-light text-ink hover:text-accent transition-colors min-w-0"
                >
                  <AppIconBadge app={app} style={appDisplayStyle} size="sm" />
                  <span className="truncate">{label}</span>
                </button>

                <div className="flex items-center gap-2 shrink-0">
                  <button
                    type="button"
                    onClick={() => toggleDistractingApp(app.id)}
                    className={`min-h-[32px] px-2.5 rounded-pill border text-[11px] transition-colors ${
                      isDist
                        ? 'bg-accent-soft border-accent/55 text-accent'
                        : 'border-border text-subtle hover:text-ink'
                    }`}
                  >
                    {d.mindfulBadge}
                    {isDist ? ' ✓' : ''}
                  </button>

                  <button
                    type="button"
                    aria-label="Favorite"
                    onClick={() => toggleFavoriteApp(app.id)}
                    className={`p-2 rounded-sm transition-colors ${
                      isFav ? 'text-accent' : 'text-subtle hover:text-ink'
                    }`}
                  >
                    <Star
                      className="w-4 h-4"
                      strokeWidth={1.6}
                      fill={isFav ? 'currentColor' : 'none'}
                    />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </BottomSheet>
  );
};
