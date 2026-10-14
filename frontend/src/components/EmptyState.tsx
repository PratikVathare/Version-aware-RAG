import React, { memo } from 'react';
import { FileQuestion, UploadCloud, Sparkles, ShieldCheck, ArrowRight, BookOpen } from 'lucide-react';
import { motion } from 'framer-motion';

interface EmptyStateProps {
  hasDocuments: boolean;
  onUploadClick: () => void;
  onSamplePromptClick?: (prompt: string) => void;
}

const SAMPLE_PROMPTS = [
  'What is the current remote work policy for engineering in 2026?',
  'How many days of annual paid time off are provided in the current version?',
  'What is the standard expense limit for business travel without manager approval?',
  'What are the mandatory data classification tiers in the latest InfoSec policy?',
];

export const EmptyState: React.FC<EmptyStateProps> = memo(({
  hasDocuments,
  onUploadClick,
  onSamplePromptClick,
}) => {
  if (!hasDocuments) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center p-8 text-center max-w-md mx-auto my-auto">
        <motion.div
          initial={{ scale: 0.8, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ duration: 0.3 }}
          className="relative mb-6"
        >
          <div className="w-20 h-20 rounded-3xl bg-gradient-to-tr from-indigo-500/20 via-violet-500/20 to-emerald-500/10 border border-indigo-500/30 flex items-center justify-center text-indigo-400 shadow-2xl shadow-indigo-500/10">
            <FileQuestion className="w-10 h-10 animate-pulse" />
          </div>
          <div className="absolute -bottom-1 -right-1 p-2 rounded-xl bg-slate-900 border border-slate-700 text-emerald-400">
            <ShieldCheck className="w-4 h-4" />
          </div>
        </motion.div>

        <h3 className="text-lg font-bold text-slate-100 tracking-tight mb-2">
          No documents uploaded
        </h3>
        <p className="text-xs text-slate-400 leading-relaxed mb-6">
          Upload your first enterprise document to begin building your AI knowledge base.
          The platform guarantees version-aware isolation so only active policies are cited.
        </p>

        <button
          type="button"
          onClick={onUploadClick}
          aria-label="Upload your first enterprise document"
          className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 text-white text-xs font-semibold shadow-lg shadow-indigo-500/25 transition-all transform hover:-translate-y-0.5 cursor-pointer"
        >
          <UploadCloud className="w-4 h-4" />
          Upload Document
        </button>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col items-center justify-center p-6 text-center max-w-xl mx-auto my-auto space-y-6">
      <motion.div
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        className="space-y-3"
      >
        <div className="inline-flex p-3 rounded-2xl bg-gradient-to-tr from-indigo-500/20 to-violet-500/20 border border-indigo-500/30 text-indigo-400 mb-1">
          <Sparkles className="w-7 h-7" />
        </div>
        <h2 className="text-xl font-bold tracking-tight text-slate-100">
          Enterprise Version-aware Knowledge Engine
        </h2>
        <p className="text-xs text-slate-400 max-w-md mx-auto leading-relaxed">
          Ask questions against indexed corporate policies. Outdated clauses are automatically
          filtered out, and policy amendments or contradictory guidelines are highlighted via conflict detection.
        </p>
      </motion.div>

      {/* Suggested prompts grid */}
      <div className="w-full space-y-2 text-left">
        <div className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 px-1 flex items-center gap-1.5">
          <BookOpen className="w-3.5 h-3.5 text-indigo-400" />
          <span>Recommended Queries</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
          {SAMPLE_PROMPTS.map((prompt, idx) => (
            <button
              type="button"
              key={idx}
              onClick={() => onSamplePromptClick?.(prompt)}
              aria-label={`Ask query: ${prompt}`}
              className="p-3 rounded-xl bg-slate-900/60 border border-slate-800/80 hover:border-indigo-500/50 hover:bg-slate-900/90 text-left text-xs text-slate-300 transition-all group flex items-start justify-between gap-2 shadow-sm cursor-pointer"
            >
              <span className="line-clamp-2 group-hover:text-white transition-colors">
                {prompt}
              </span>
              <ArrowRight className="w-3.5 h-3.5 text-slate-400 group-hover:text-indigo-400 shrink-0 mt-0.5 transition-colors" />
            </button>
          ))}
        </div>
      </div>
    </div>
  );
});

EmptyState.displayName = 'EmptyState';
