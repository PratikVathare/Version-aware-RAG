import React from 'react';
import { cn } from '../utils/cn';

interface MetricCardProps {
  label: string;
  value: string | number;
  icon?: React.ReactNode;
  subtitle?: string;
  className?: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  label,
  value,
  icon,
  subtitle,
  className,
}) => {
  return (
    <div
      className={cn(
        'flex items-center gap-3 px-3.5 py-2 rounded-xl bg-slate-900/60 border border-slate-800/80 hover:border-slate-700/80 transition-all',
        className
      )}
    >
      {icon && (
        <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
          {icon}
        </div>
      )}
      <div className="min-w-0">
        <div className="text-[11px] font-medium uppercase tracking-wider text-slate-400">
          {label}
        </div>
        <div className="flex items-baseline gap-1.5">
          <span className="text-sm font-semibold text-slate-100 tracking-tight">
            {value}
          </span>
          {subtitle && (
            <span className="text-[10px] text-slate-400">{subtitle}</span>
          )}
        </div>
      </div>
    </div>
  );
};
