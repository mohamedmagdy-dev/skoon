import React, { useEffect } from 'react';
import { Check, Search, X } from 'lucide-react';

// =========================================
// Button
// =========================================
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'accent' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  fullWidth?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'secondary',
  size = 'md',
  fullWidth = false,
  className = '',
  children,
  ...props
}) => {
  const base =
    'inline-flex items-center justify-center gap-2 font-medium transition-all duration-150 select-none disabled:opacity-40 disabled:pointer-events-none active:scale-[0.98]';

  const variants: Record<NonNullable<ButtonProps['variant']>, string> = {
    primary: 'bg-ink text-bg hover:opacity-90',
    secondary: 'bg-surface text-ink border border-border hover:bg-elevated',
    ghost: 'bg-transparent text-muted hover:text-ink hover:bg-surface',
    accent: 'bg-accent-soft text-accent border border-accent/40 hover:border-accent',
    danger: 'bg-[var(--color-danger-soft)] text-danger border border-danger/30 hover:border-danger/60',
  };

  const sizes: Record<NonNullable<ButtonProps['size']>, string> = {
    sm: 'min-h-[36px] px-3 py-1.5 text-[13px] rounded-sm',
    md: 'min-h-[44px] px-4 py-2.5 text-[14px] rounded-md',
    lg: 'min-h-[50px] px-5 py-3 text-[15px] rounded-md',
  };

  return (
    <button
      className={`${base} ${variants[variant]} ${sizes[size]} ${fullWidth ? 'w-full' : ''} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};

// =========================================
// IconButton (min 44x44 touch target)
// =========================================
interface IconButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  label: string;
  active?: boolean;
}

export const IconButton: React.FC<IconButtonProps> = ({
  label,
  active = false,
  className = '',
  children,
  ...props
}) => (
  <button
    type="button"
    aria-label={label}
    title={label}
    className={`inline-flex items-center justify-center min-w-[44px] min-h-[44px] rounded-md transition-colors duration-150 ${
      active
        ? 'bg-accent-soft text-accent border border-accent/40'
        : 'text-muted hover:text-ink hover:bg-surface'
    } ${className}`}
    {...props}
  >
    {children}
  </button>
);

// =========================================
// Chip / Pill
// =========================================
interface ChipProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  active?: boolean;
  danger?: boolean;
  success?: boolean;
}

export const Chip: React.FC<ChipProps> = ({
  active = false,
  danger = false,
  success = false,
  className = '',
  children,
  ...props
}) => {
  const tone = danger
    ? 'bg-[var(--color-danger-soft)] text-danger border-danger/35'
    : success
      ? 'bg-[var(--color-success-soft)] text-success border-success/40'
      : active
        ? 'bg-accent-soft text-accent border-accent/60'
        : 'bg-surface text-muted border-border hover:text-ink hover:border-border-strong';

  return (
    <button
      type="button"
      className={`inline-flex items-center justify-center gap-1.5 min-h-[36px] px-3.5 py-1.5 text-[13px] rounded-pill border transition-colors duration-150 whitespace-nowrap select-none ${tone} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};

// =========================================
// Input & SearchInput
// =========================================
export const Input = React.forwardRef<
  HTMLInputElement,
  React.InputHTMLAttributes<HTMLInputElement>
>(({ className = '', ...props }, ref) => (
  <input
    ref={ref}
    className={`w-full min-h-[44px] px-3.5 py-2.5 rounded-md bg-surface border border-border text-[15px] text-ink placeholder:text-subtle focus:border-accent focus:outline-none transition-colors ${className}`}
    {...props}
  />
));
Input.displayName = 'Input';

interface SearchInputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'onChange'> {
  value: string;
  onChange: (value: string) => void;
  clearLabel?: string;
}

export const SearchInput: React.FC<SearchInputProps> = ({
  value,
  onChange,
  clearLabel = 'Clear',
  className = '',
  ...props
}) => (
  <div className={`relative flex items-center w-full ${className}`}>
    <Search
      className="w-4 h-4 text-subtle absolute start-3.5 pointer-events-none"
      strokeWidth={1.6}
    />
    <input
      type="text"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      className="w-full min-h-[44px] ps-10 pe-10 py-2.5 rounded-md bg-surface border border-border text-[15px] text-ink placeholder:text-subtle focus:border-accent focus:outline-none transition-colors"
      {...props}
    />
    {value && (
      <button
        type="button"
        aria-label={clearLabel}
        onClick={() => onChange('')}
        className="absolute end-1.5 p-2 text-subtle hover:text-ink rounded-sm"
      >
        <X className="w-4 h-4" strokeWidth={1.6} />
      </button>
    )}
  </div>
);

// =========================================
// Toggle Switch
// =========================================
interface ToggleProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  label?: string;
}

export const Toggle: React.FC<ToggleProps> = ({ checked, onChange, label }) => (
  <button
    type="button"
    role="switch"
    aria-checked={checked}
    aria-label={label}
    onClick={() => onChange(!checked)}
    className={`relative inline-flex items-center shrink-0 w-11 h-6 rounded-pill border transition-colors duration-200 ${
      checked
        ? 'bg-accent border-accent'
        : 'bg-elevated border-border-strong'
    }`}
  >
    <span
      className={`inline-block w-4 h-4 rounded-pill bg-bg transition-transform duration-200 ${
        checked ? 'ltr:translate-x-[22px] rtl:-translate-x-[22px]' : 'ltr:translate-x-[3px] rtl:-translate-x-[3px]'
      }`}
    />
  </button>
);

// =========================================
// Checkbox
// =========================================
interface CheckboxProps {
  checked: boolean;
  onChange?: () => void;
  label?: string;
}

export const Checkbox: React.FC<CheckboxProps> = ({ checked, onChange, label }) => (
  <span
    role={onChange ? 'checkbox' : undefined}
    aria-checked={onChange ? checked : undefined}
    aria-label={label}
    onClick={onChange}
    className={`inline-flex items-center justify-center w-5 h-5 rounded-[6px] border shrink-0 transition-colors duration-150 ${
      checked
        ? 'bg-accent border-accent text-bg'
        : 'bg-transparent border-border-strong'
    }`}
  >
    {checked && <Check className="w-3.5 h-3.5" strokeWidth={2.5} />}
  </span>
);

// =========================================
// SegmentedControl
// =========================================
export interface SegmentedOption<T extends string> {
  value: T;
  label: string;
}

interface SegmentedControlProps<T extends string> {
  options: SegmentedOption<T>[];
  value: T;
  onChange: (value: T) => void;
  className?: string;
}

export function SegmentedControl<T extends string>({
  options,
  value,
  onChange,
  className = '',
}: SegmentedControlProps<T>) {
  return (
    <div
      role="tablist"
      className={`flex items-center p-1 rounded-md bg-surface border border-border gap-1 ${className}`}
    >
      {options.map((opt) => {
        const active = opt.value === value;
        return (
          <button
            key={opt.value}
            type="button"
            role="tab"
            aria-selected={active}
            onClick={() => onChange(opt.value)}
            className={`flex-1 min-h-[36px] px-3 py-1.5 rounded-sm text-[13px] transition-colors duration-150 ${
              active
                ? 'bg-accent-soft text-accent font-medium'
                : 'text-muted hover:text-ink'
            }`}
          >
            {opt.label}
          </button>
        );
      })}
    </div>
  );
}

// =========================================
// Card & SectionHeader
// =========================================
export const Card: React.FC<
  React.HTMLAttributes<HTMLDivElement> & { elevated?: boolean }
> = ({ elevated = false, className = '', children, ...props }) => (
  <div
    className={`rounded-lg border border-border ${
      elevated ? 'bg-elevated' : 'bg-surface'
    } ${className}`}
    {...props}
  >
    {children}
  </div>
);

interface SectionHeaderProps {
  eyebrow?: string;
  title: string;
  action?: React.ReactNode;
}

export const SectionHeader: React.FC<SectionHeaderProps> = ({
  eyebrow,
  title,
  action,
}) => (
  <div className="flex items-end justify-between gap-3">
    <div className="min-w-0">
      {eyebrow && (
        <p className="text-[12px] tracking-wide text-muted mb-0.5">{eyebrow}</p>
      )}
      <h1 className="text-[24px] leading-tight font-normal tracking-tight text-ink truncate">
        {title}
      </h1>
    </div>
    {action && <div className="shrink-0">{action}</div>}
  </div>
);

// =========================================
// ProgressBar & CircularProgress
// =========================================
export const ProgressBar: React.FC<{ progress: number; className?: string }> = ({
  progress,
  className = '',
}) => {
  const clamped = Math.max(0, Math.min(1, progress));
  return (
    <div className={`w-full h-[3px] rounded-pill bg-border overflow-hidden ${className}`}>
      <div
        className="h-full bg-accent transition-all duration-300 ease-out"
        style={{ width: `${clamped * 100}%` }}
      />
    </div>
  );
};

interface CircularProgressProps {
  size: number;
  strokeWidth?: number;
  progress: number; // 0..1
  color?: string;
  children?: React.ReactNode;
}

export const CircularProgress: React.FC<CircularProgressProps> = ({
  size,
  strokeWidth = 3.5,
  progress,
  color = 'var(--color-accent)',
  children,
}) => {
  const r = (size - strokeWidth * 2) / 2;
  const circumference = 2 * Math.PI * r;
  const clamped = Math.max(0, Math.min(1, progress));
  const offset = circumference * (1 - clamped);

  return (
    <div
      className="relative inline-flex items-center justify-center select-none"
      style={{ width: size, height: size }}
    >
      <svg
        width={size}
        height={size}
        viewBox={`0 0 ${size} ${size}`}
        className="-rotate-90"
        aria-hidden="true"
      >
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke="var(--color-border)"
          strokeWidth={strokeWidth}
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth + 0.5}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          style={{ transition: 'stroke-dashoffset 280ms ease-out, stroke 220ms ease' }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        {children}
      </div>
    </div>
  );
};

// =========================================
// BottomSheet / Fullscreen Overlay Sheet
// =========================================
interface BottomSheetProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  eyebrow?: string;
  headerRight?: React.ReactNode;
  children: React.ReactNode;
}

export const BottomSheet: React.FC<BottomSheetProps> = ({
  open,
  onClose,
  title,
  eyebrow,
  headerRight,
  children,
}) => {
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-40 flex items-end sm:items-center justify-center bg-bg sm:bg-[var(--color-backdrop)] animate-fade-in"
      role="dialog"
      aria-modal="true"
    >
      <div
        className="w-full max-w-[460px] h-[100dvh] sm:h-[92dvh] sm:max-h-[820px] sm:rounded-xl bg-bg sm:border sm:border-border flex flex-col overflow-hidden animate-slide-up"
      >
        {(title || headerRight) && (
          <div className="flex items-center justify-between gap-3 px-page-x pt-5 pb-3 border-b border-border shrink-0">
            <div className="min-w-0">
              {eyebrow && <p className="text-[12px] text-muted">{eyebrow}</p>}
              {title && (
                <h2 className="text-[20px] font-normal text-ink truncate">{title}</h2>
              )}
            </div>
            {headerRight}
          </div>
        )}
        <div className="flex-1 overflow-y-auto px-page-x py-4">{children}</div>
      </div>
    </div>
  );
};

// =========================================
// ConfirmDialog
// =========================================
interface ConfirmDialogProps {
  open: boolean;
  title: string;
  description: string;
  confirmLabel: string;
  cancelLabel: string;
  danger?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  open,
  title,
  description,
  confirmLabel,
  cancelLabel,
  danger = true,
  onConfirm,
  onCancel,
}) => {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-5 bg-[var(--color-backdrop)] animate-fade-in"
      role="alertdialog"
      aria-modal="true"
    >
      <div className="w-full max-w-[340px] rounded-xl bg-surface border border-border p-5 space-y-4 animate-slide-up">
        <div className="space-y-1.5">
          <h3 className="text-[18px] font-medium text-ink">{title}</h3>
          <p className="text-[14px] leading-relaxed text-muted">{description}</p>
        </div>
        <div className="flex items-center justify-end gap-2 pt-1">
          <Button variant="ghost" size="sm" onClick={onCancel}>
            {cancelLabel}
          </Button>
          <Button
            variant={danger ? 'danger' : 'primary'}
            size="sm"
            onClick={onConfirm}
          >
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  );
};

// =========================================
// EmptyState & LoadingState & Toast
// =========================================
export const EmptyState: React.FC<{ message: string; action?: React.ReactNode }> = ({
  message,
  action,
}) => (
  <div className="flex flex-col items-center justify-center text-center py-14 px-6 gap-4">
    <p className="text-[14px] text-muted max-w-[260px] leading-relaxed">{message}</p>
    {action}
  </div>
);

export const LoadingState: React.FC<{ message: string }> = ({ message }) => (
  <div className="flex flex-col items-center justify-center py-16 gap-3">
    <span className="w-5 h-5 rounded-pill border-2 border-border border-t-accent animate-spin" />
    <p className="text-[13px] text-muted">{message}</p>
  </div>
);

export const Toast: React.FC<{ message: string | null }> = ({ message }) => {
  if (!message) return null;
  return (
    <div
      role="status"
      aria-live="polite"
      className="fixed bottom-16 left-1/2 -translate-x-1/2 z-50 px-4 py-2.5 rounded-pill bg-ink text-bg text-[13px] font-medium shadow-sm whitespace-nowrap max-w-[90vw] truncate animate-fade-in"
    >
      {message}
    </div>
  );
};
