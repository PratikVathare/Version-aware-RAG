import React, { useState, memo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown, ChevronUp, FileText, Layers, Clock, Building2 } from 'lucide-react';
import { StatusBadge } from './StatusBadge';
import { VersionTimeline } from './VersionTimeline';
import { formatTimeAgo } from '../utils/cn';
import type { DocumentItem, DocumentVersion } from '../types';

interface DocumentCardProps {
  document: DocumentItem;
  onSelectVersion?: (document: DocumentItem, version: DocumentVersion) => void;
  selectedVersionId?: string;
  isInitiallyExpanded?: boolean;
}

export const DocumentCard: React.FC<DocumentCardProps> = memo(({
  document,
  onSelectVersion,
  selectedVersionId,
  isInitiallyExpanded = false,
}) => {
  const [isExpanded, setIsExpanded] = useState(isInitiallyExpanded);

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.95 }}
      transition={{ duration: 0.2 }}
      className={`rounded-xl border transition-all ${
        isExpanded
          ? 'bg-slate-900/80 border-slate-700/80 shadow-lg shadow-black/30'
          : 'bg-slate-900/40 border-slate-800/80 hover:bg-slate-900/70 hover:border-slate-700/60'
      }`}
    >
      <div className="p-3">
        {/* Top row: Name & Expand toggle */}
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-start gap-2.5 min-w-0">
            <div className="p-2 rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 shrink-0 mt-0.5">
              <FileText className="w-4 h-4" />
            </div>
            <div className="min-w-0">
              <h3 className="text-xs font-semibold text-slate-100 truncate group-hover:text-indigo-300 transition-colors">
                {document.name || document.documentName || 'Untitled Document'}
              </h3>
              <div className="flex items-center gap-1.5 mt-0.5 text-[11px] text-slate-400">
                <Building2 className="w-3 h-3 text-slate-400" />
                <span className="truncate">{document.department}</span>
              </div>
            </div>
          </div>

          <button
            type="button"
            onClick={() => setIsExpanded(!isExpanded)}
            aria-expanded={isExpanded}
            aria-label={
              isExpanded
                ? `Collapse versions for ${document.name || document.documentName}`
                : `Expand versions for ${document.name || document.documentName}`
            }
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors shrink-0 cursor-pointer"
          >
            {isExpanded ? (
              <ChevronUp className="w-4 h-4 text-indigo-400" />
            ) : (
              <ChevronDown className="w-4 h-4" />
            )}
          </button>
        </div>

        {/* Metadata row */}
        <div className="mt-2.5 flex items-center justify-between text-[11px] text-slate-400 pt-2 border-t border-slate-800/50">
          <div className="flex items-center gap-1.5">
            <StatusBadge
              status="connected"
              label={`v${document.currentVersion}`}
              className="py-0 px-2 text-[10px]"
            />
            <span className="text-slate-400 flex items-center gap-1">
              <Layers className="w-3 h-3" />
              {document.totalVersions} {document.totalVersions === 1 ? 'version' : 'versions'}
            </span>
          </div>

          <div className="flex items-center gap-1 text-[10px] text-slate-400">
            <Clock className="w-3 h-3" />
            <span>{formatTimeAgo(document.lastUpdated)}</span>
          </div>
        </div>

        {/* Expandable Version Timeline */}
        <AnimatePresence>
          {isExpanded && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              transition={{ duration: 0.2 }}
            >
              <VersionTimeline
                versions={document.versions || []}
                selectedVersionId={selectedVersionId}
                onSelectVersion={(ver) => onSelectVersion?.(document, ver)}
              />
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </motion.div>
  );
});

DocumentCard.displayName = 'DocumentCard';
