import React from 'react';
import { cn } from '../utils/cn';

interface StatusBadgeProps {
  status: 'connected' | 'disconnected' | 'syncing' | 'active' | 'warning' | 'archived';
  label: string;
  className?: string;
  showDot?: boolean;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  label,
  className,
  showDot = true,
}) => {
  const styles = {
    connected: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
    disconnected: 'bg-rose-500/10 text-rose-400 border-rose-500/30',
    syncing: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    active: 'bg-violet-500/10 text-violet-400 border-violet-500/30',
    warning: 'bg-amber-500/10 text-amber-300 border-amber-500/30',
    archived: 'bg-slate-500/10 text-slate-400 border-slate-700/50',
  };

  const dotStyles = {
    connected: 'bg-emerald-400 shadow-[0_0_8px_#34d399]',
    disconnected: 'bg-rose-400 shadow-[0_0_8px_#f87171]',
    syncing: 'bg-amber-400 animate-pulse',
    active: 'bg-violet-400 shadow-[0_0_8px_#a78bfa]',
    warning: 'bg-amber-400 shadow-[0_0_8px_#fbbf24]',
    archived: 'bg-slate-500',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium border transition-colors',
        styles[status],
        className
      )}
    >
      {showDot && (
        <span
          className={cn('w-1.5 h-1.5 rounded-full', dotStyles[status])}
        />
      )}
      {label}
    </span>
  );
};
