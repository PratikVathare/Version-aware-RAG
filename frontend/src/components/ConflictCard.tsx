import React, { memo } from 'react';
import { AlertTriangle, ArrowRight, ShieldCheck } from 'lucide-react';
import { motion } from 'framer-motion';
import type { ConflictDetails } from '../types';

interface ConflictCardProps {
  conflictDetails: ConflictDetails;
}

export const ConflictCard: React.FC<ConflictCardProps> = memo(({ conflictDetails }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: -6, scale: 0.98 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      role="alert"
      aria-live="polite"
      className="rounded-xl border border-amber-500/40 bg-gradient-to-r from-amber-950/30 via-amber-900/10 to-slate-900/60 p-4 shadow-lg shadow-amber-950/20 backdrop-blur-sm"
    >
      <div className="flex items-start gap-3">
        <div className="p-2 rounded-xl bg-amber-500/20 text-amber-400 border border-amber-500/30 shrink-0 mt-0.5">
          <AlertTriangle className="w-5 h-5 animate-pulse" />
        </div>

        <div className="flex-1 min-w-0 space-y-2.5">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <h4 className="text-xs font-bold text-amber-300 uppercase tracking-wider flex items-center gap-1.5">
                Version Conflict Detected
              </h4>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30 font-medium">
                Policy Divergence
              </span>
            </div>

            {/* Version comparison tag */}
            <div className="flex items-center gap-1.5 text-xs font-mono font-medium bg-slate-900/90 px-2.5 py-1 rounded-lg border border-slate-800">
              <span className="text-slate-400 line-through">
                v{conflictDetails.olderVersion}
              </span>
              <ArrowRight className="w-3 h-3 text-amber-400" />
              <span className="text-emerald-400 font-bold">
                v{conflictDetails.currentVersion}
              </span>
            </div>
          </div>

          {/* Conflict Summary description */}
          <div className="p-3 rounded-lg bg-amber-950/40 border border-amber-500/20 text-xs text-amber-100/90 leading-relaxed font-sans">
            <span className="font-semibold text-amber-300">Contradiction: </span>
            {conflictDetails.conflictSummary || conflictDetails.conflictDescription}
          </div>

          {/* Reason latest was selected */}
          <div className="flex items-start gap-2 text-xs text-slate-300 bg-slate-900/80 p-2.5 rounded-lg border border-slate-800/80">
            <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
            <div className="text-[11px] leading-relaxed">
              <span className="font-semibold text-emerald-400">Resolution Applied: </span>
              {conflictDetails.resolutionReason ||
                conflictDetails.reasonSelected ||
                `Filtered out legacy v${conflictDetails.olderVersion} in semantic pre-retrieval. Only latest active version v${conflictDetails.currentVersion} was presented to the LLM.`}
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  );
});

ConflictCard.displayName = 'ConflictCard';
