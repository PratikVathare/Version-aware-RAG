import React, { useState, memo } from 'react';
import {
  FileText,
  Calendar,
  Layers,
  Clock,
  Copy,
  Check,
  RefreshCw,
  ThumbsUp,
  ThumbsDown,
  Building2,
  ChevronDown,
  ChevronUp,
  Zap,
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { StatusBadge } from './StatusBadge';
import { ConflictCard } from './ConflictCard';
import { formatDate } from '../utils/cn';
import type { ChatResponse } from '../types';

interface AnswerCardProps {
  response: ChatResponse;
  onRegenerate?: () => void;
}

export const AnswerCard: React.FC<AnswerCardProps> = memo(({
  response,
  onRegenerate,
}) => {
  const [copied, setCopied] = useState(false);
  const [showChunks, setShowChunks] = useState(false);
  const [feedback, setFeedback] = useState<'liked' | 'disliked' | null>(null);

  const handleCopy = () => {
    navigator.clipboard.writeText(response.answer);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-3 w-full">
      {/* Policy Conflict Alert if present */}
      {response.conflict && response.conflictDetails && (
        <ConflictCard conflictDetails={response.conflictDetails} />
      )}

      {/* Main Answer Container */}
      <article
        aria-label={`Response from ${response.documentName} version ${response.version}`}
        className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4 sm:p-5 shadow-xl backdrop-blur-md space-y-4 hover:border-slate-700/80 transition-all"
      >
        {/* Source metadata header */}
        <div className="flex flex-wrap items-center justify-between gap-2 pb-3 border-b border-slate-800/80 text-xs">
          <div className="flex flex-wrap items-center gap-2">
            <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-indigo-500/10 text-indigo-300 border border-indigo-500/20 font-medium">
              <FileText className="w-3.5 h-3.5 text-indigo-400" />
              <span className="font-semibold">{response.documentName}</span>
            </div>

            <StatusBadge
              status="connected"
              label={`v${response.version}`}
              className="font-mono text-[11px]"
            />

            <span className="flex items-center gap-1 text-slate-400 text-[11px]">
              <Building2 className="w-3 h-3 text-slate-400" />
              {response.department}
            </span>
          </div>

          <div className="flex items-center gap-3 text-slate-400 text-[11px]">
            <span className="flex items-center gap-1">
              <Calendar className="w-3 h-3 text-slate-400" />
              Effective: {formatDate(response.effectiveDate)}
            </span>
            <span className="flex items-center gap-1 font-mono text-emerald-400 font-medium">
              <Zap className="w-3 h-3" />
              {Math.round((response.confidence || 0.95) * 100)}% match
            </span>
          </div>
        </div>

        {/* Formatted Answer */}
        <div className="text-slate-100 text-sm leading-relaxed whitespace-pre-wrap font-sans selection:bg-indigo-500/30">
          {response.answer}
        </div>

        {/* Retrieved Chunks collapsible drawer */}
        {response.retrievedChunks && response.retrievedChunks.length > 0 && (
          <div className="pt-2 border-t border-slate-800/70">
            <button
              type="button"
              onClick={() => setShowChunks(!showChunks)}
              aria-expanded={showChunks}
              aria-controls={`chunks-${response.documentName}`}
              className="flex items-center gap-1.5 text-xs text-indigo-400 hover:text-indigo-300 font-medium transition-colors cursor-pointer"
            >
              <Layers className="w-3.5 h-3.5" />
              <span>
                {showChunks ? 'Hide' : 'Inspect'} {response.retrievedChunks.length} Retrieved Semantic{' '}
                {response.retrievedChunks.length === 1 ? 'Chunk' : 'Chunks'}
              </span>
              {showChunks ? (
                <ChevronUp className="w-3.5 h-3.5" />
              ) : (
                <ChevronDown className="w-3.5 h-3.5" />
              )}
            </button>

            <AnimatePresence>
              {showChunks && (
                <motion.div
                  id={`chunks-${response.documentName}`}
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  exit={{ opacity: 0, height: 0 }}
                  className="mt-2.5 space-y-2 overflow-hidden"
                >
                  {response.retrievedChunks.map((chunk, idx) => (
                    <div
                      key={chunk.id || idx}
                      className="p-3 rounded-xl bg-slate-950/70 border border-slate-800/90 text-xs text-slate-300 font-mono space-y-1.5"
                    >
                      <div className="flex items-center justify-between text-[10px] text-slate-400 pb-1 border-b border-slate-800">
                        <span className="text-indigo-400 font-semibold">Chunk #{idx + 1}</span>
                        <span>
                          Similarity: {Math.round(((chunk.similarityScore ?? chunk.score) || 0.95) * 100)}%
                        </span>
                      </div>
                      <p className="text-[11px] leading-relaxed text-slate-300 font-sans">
                        "{chunk.chunkText || chunk.content || ''}"
                      </p>
                    </div>
                  ))}
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        )}

        {/* Card Footer: Latency & Action buttons */}
        <div className="flex flex-wrap items-center justify-between gap-2 pt-3 border-t border-slate-800/80 text-xs">
          <div className="flex items-center gap-1.5 text-slate-400 text-[11px] font-mono">
            <Clock className="w-3.5 h-3.5 text-slate-400" />
            <span>Response generated in {response.responseTime || '420ms'}</span>
          </div>

          <div className="flex items-center gap-1">
            <button
              type="button"
              onClick={handleCopy}
              aria-label="Copy answer to clipboard"
              className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors cursor-pointer"
              title="Copy answer to clipboard"
            >
              {copied ? (
                <>
                  <Check className="w-3.5 h-3.5 text-emerald-400" />
                  <span className="text-emerald-400 text-xs">Copied</span>
                </>
              ) : (
                <>
                  <Copy className="w-3.5 h-3.5" />
                  <span className="text-xs">Copy</span>
                </>
              )}
            </button>

            {onRegenerate && (
              <button
                type="button"
                onClick={onRegenerate}
                aria-label="Regenerate this response"
                className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors cursor-pointer"
                title="Regenerate answer"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span className="text-xs">Regenerate</span>
              </button>
            )}

            <button
              type="button"
              onClick={() => setFeedback(feedback === 'liked' ? null : 'liked')}
              aria-label="Mark response as helpful"
              aria-pressed={feedback === 'liked'}
              className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                feedback === 'liked'
                  ? 'bg-emerald-500/20 text-emerald-400'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
              title="Helpful response"
            >
              <ThumbsUp className="w-3.5 h-3.5" />
            </button>

            <button
              type="button"
              onClick={() => setFeedback(feedback === 'disliked' ? null : 'disliked')}
              aria-label="Mark response as unhelpful"
              aria-pressed={feedback === 'disliked'}
              className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                feedback === 'disliked'
                  ? 'bg-rose-500/20 text-rose-400'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
              title="Unhelpful response"
            >
              <ThumbsDown className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </article>
    </div>
  );
});

AnswerCard.displayName = 'AnswerCard';
