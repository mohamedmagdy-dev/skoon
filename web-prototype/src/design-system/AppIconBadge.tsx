import React from 'react';
import {
  AtSign,
  Calculator,
  Calendar,
  Camera,
  Clock,
  FileText,
  Folder,
  Ghost,
  Globe,
  Headphones,
  Instagram,
  Landmark,
  Mail,
  MapPin,
  MessageCircle,
  MessageSquare,
  Music2,
  Phone,
  Play,
  Send,
  Users,
  type LucideIcon,
} from 'lucide-react';
import type { AppDisplayStyle, SimulatedApp } from '../types';

const ICON_MAP: Record<string, LucideIcon> = {
  phone: Phone,
  message: MessageSquare,
  whatsapp: MessageCircle,
  send: Send,
  camera: Camera,
  map: MapPin,
  calendar: Calendar,
  mail: Mail,
  globe: Globe,
  folder: Folder,
  clock: Clock,
  calc: Calculator,
  bank: Landmark,
  file: FileText,
  headphones: Headphones,
  play: Play,
  instagram: Instagram,
  music: Music2,
  users: Users,
  at: AtSign,
  ghost: Ghost,
};

interface AppIconBadgeProps {
  app: SimulatedApp;
  style: AppDisplayStyle;
  size?: 'sm' | 'md';
}

export const AppIconBadge: React.FC<AppIconBadgeProps> = ({
  app,
  style,
  size = 'md',
}) => {
  if (style === 'text') return null;

  const Icon = ICON_MAP[app.iconKey] ?? Globe;
  const dims = size === 'sm' ? 'w-8 h-8 rounded-[10px]' : 'w-10 h-10 rounded-[12px]';
  const iconSize = size === 'sm' ? 'w-4 h-4' : 'w-[18px] h-[18px]';

  if (style === 'monochrome') {
    return (
      <span
        aria-hidden="true"
        className={`inline-flex items-center justify-center shrink-0 bg-surface border border-border text-ink ${dims}`}
      >
        <Icon className={iconSize} strokeWidth={1.6} />
      </span>
    );
  }

  // Original full-color squircle icon
  return (
    <span
      aria-hidden="true"
      className={`inline-flex items-center justify-center shrink-0 text-white ${dims}`}
      style={{ backgroundColor: app.brandColor }}
    >
      <Icon className={iconSize} strokeWidth={1.85} />
    </span>
  );
};
