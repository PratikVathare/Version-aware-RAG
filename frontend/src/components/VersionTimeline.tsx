import React, { memo } from 'react';
import { Calendar, Hash, Layers } from 'lucide-react';
import { StatusBadge } from './StatusBadge';
import { formatDate } from '../utils/cn';
import type { DocumentVersion } from '../types';

interface VersionTimelineProps {
  versions: DocumentVersion[];
  onSelectVersion?: (version: DocumentVersion) => void;
  selectedVersionId?: string;
}

export const VersionTimeline: React.FC<VersionTimelineProps> = memo(({
  versions,
  onSelectVersion,
  selectedVersionId,
}) => {
  return (
    <div className="mt-2.5 pt-2.5 border-t border-slate-800/80 space-y-2">
      <div className="text-[11px] font-medium uppercase tracking-wider text-slate-400 px-1 flex items-center justify-between">
        <span>Version History</span>
        <span className="text-[10px] text-slate-400">Chronological</span>
      </div>

      <div className="relative pl-3 space-y-2 before:absolute before:left-1 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-800">
        {versions.map((ver) => {
          const isSelected = selectedVersionId === ver.id;
          return (
            <div
              key={ver.id}
              role="button"
              tabIndex={0}
              aria-label={`Select Version ${ver.versionNumber}, effective ${ver.effectiveDate}`}
              onClick={() => onSelectVersion?.(ver)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  onSelectVersion?.(ver);
                }
              }}
              className={`group relative flex flex-col p-2 rounded-lg cursor-pointer border transition-all text-xs focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 ${
                isSelected
                  ? 'bg-indigo-950/40 border-indigo-500/50 shadow-[0_0_12px_rgba(99,102,241,0.15)]'
                  : 'bg-slate-900/60 border-slate-800/70 hover:bg-slate-800/60 hover:border-slate-700'
              }`}
            >
              {/* Timeline marker */}
              <span
                aria-hidden="true"
                className={`absolute -left-[15px] top-3 w-2 h-2 rounded-full ring-2 ring-slate-950 ${
                  ver.isCurrent ? 'bg-indigo-400 ring-indigo-950' : 'bg-slate-600'
                }`}
              />

              <div className="flex items-center justify-between gap-1.5">
                <div className="flex items-center gap-1.5 font-semibold text-slate-200">
                  <Hash className="w-3.5 h-3.5 text-indigo-400" />
                  <span>Version {ver.versionNumber}</span>
                </div>
                {ver.isCurrent ? (
                  <StatusBadge
                    status="connected"
                    label="Current"
                    className="text-[10px] py-0 px-2"
                  />
                ) : (
                  <StatusBadge
                    status="archived"
                    label="Archived"
                    className="text-[10px] py-0 px-2"
                  />
                )}
              </div>

              <div className="mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1 text-[11px] text-slate-400">
                <span className="flex items-center gap-1">
                  <Calendar className="w-3 h-3 text-slate-400" />
                  Effective: {formatDate(ver.effectiveDate)}
                </span>
                <span className="flex items-center gap-1">
                  <Layers className="w-3 h-3 text-slate-400" />
                  {ver.chunksCount ?? ver.totalChunks ?? 16} chunks
                </span>
              </div>

              {ver.changeSummary && (
                <p className="mt-1 text-[11px] text-slate-400 line-clamp-1 italic">
                  "{ver.changeSummary}"
                </p>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
});

VersionTimeline.displayName = 'VersionTimeline';
