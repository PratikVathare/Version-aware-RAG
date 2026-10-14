import React, { useEffect, useState } from 'react';
import { Search, Database, AlertCircle, Sparkles, CheckCircle2 } from 'lucide-react';
import { motion } from 'framer-motion';

const STEPS = [
  {
    title: 'Searching Latest Version',
    desc: 'Determining highest version number & recent effective date',
    icon: Search,
  },
  {
    title: 'Semantic Retrieval',
    desc: 'Querying vector embeddings & isolating current chunks',
    icon: Database,
  },
  {
    title: 'Conflict Detection',
    desc: 'Validating policy values across historical amendments',
    icon: AlertCircle,
  },
  {
    title: 'Generating Final Answer',
    desc: 'Synthesizing with Groq LLaMA 3.3 using strict context',
    icon: Sparkles,
  },
];

export const TypingIndicator: React.FC = () => {
  const [currentStepIndex, setCurrentStepIndex] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentStepIndex((prev) => (prev < STEPS.length - 1 ? prev + 1 : prev));
    }, 700);

    return () => clearInterval(timer);
  }, []);

  return (
    <div
      role="status"
      aria-live="polite"
      aria-label="Generating version-aware answer"
      className="flex items-start gap-3 max-w-2xl py-2"
    >
      {/* Bot Avatar */}
      <div
        aria-hidden="true"
        className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 flex items-center justify-center text-white text-xs font-bold shadow-md shadow-indigo-500/20 shrink-0"
      >
        AI
      </div>

      <div className="flex-1 rounded-2xl bg-slate-900/80 border border-indigo-500/30 p-4 shadow-xl backdrop-blur-md space-y-3">
        <div className="flex items-center justify-between border-b border-slate-800 pb-2">
          <div className="flex items-center gap-2">
            <span className="relative flex h-2.5 w-2.5">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75" />
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-indigo-500" />
            </span>
            <span className="text-xs font-semibold text-slate-200">
              Agent Thinking & Temporal Reasoning
            </span>
          </div>
          <span className="text-[11px] font-mono text-indigo-400 font-medium">
            Step {currentStepIndex + 1} of {STEPS.length}
          </span>
        </div>

        {/* Multi-step progress list */}
        <div className="space-y-2">
          {STEPS.map((step, idx) => {
            const Icon = step.icon;
            const isDone = idx < currentStepIndex;
            const isCurrent = idx === currentStepIndex;
            const isPending = idx > currentStepIndex;

            return (
              <motion.div
                key={step.title}
                initial={{ opacity: 0, x: -10 }}
                animate={{ opacity: isPending ? 0.4 : 1, x: 0 }}
                transition={{ duration: 0.2 }}
                className={`flex items-center gap-3 p-2 rounded-lg text-xs transition-colors ${
                  isCurrent
                    ? 'bg-indigo-950/40 border border-indigo-500/30 text-indigo-200 shadow-sm'
                    : isDone
                    ? 'text-slate-300'
                    : 'text-slate-500'
                }`}
              >
                <div
                  className={`w-6 h-6 rounded-md flex items-center justify-center shrink-0 ${
                    isDone
                      ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                      : isCurrent
                      ? 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/40 animate-pulse'
                      : 'bg-slate-800 text-slate-500'
                  }`}
                >
                  {isDone ? (
                    <CheckCircle2 className="w-3.5 h-3.5" />
                  ) : (
                    <Icon className="w-3.5 h-3.5" />
                  )}
                </div>

                <div className="min-w-0 flex-1">
                  <div className="font-medium text-xs flex items-center justify-between">
                    <span>{step.title}</span>
                    {isCurrent && (
                      <span className="text-[10px] text-indigo-400 font-mono animate-pulse">
                        Processing...
                      </span>
                    )}
                  </div>
                  <div className="text-[10px] text-slate-400 truncate">
                    {step.desc}
                  </div>
                </div>
              </motion.div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
