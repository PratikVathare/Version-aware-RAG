import React from 'react';
import { Database, Cpu, Layers, FileText, Menu, RefreshCw } from 'lucide-react';
import { StatusBadge } from './StatusBadge';
import { MetricCard } from './MetricCard';
import type { KnowledgeBaseStatus } from '../types';

interface HeaderProps {
  status: KnowledgeBaseStatus;
  onRefresh?: () => void;
  isRefreshing?: boolean;
  onToggleSidebar?: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  status,
  onRefresh,
  isRefreshing,
  onToggleSidebar,
}) => {
  return (
    <header className="sticky top-0 z-30 flex items-center justify-between px-4 lg:px-6 py-3 border-b border-slate-800/80 bg-slate-950/80 backdrop-blur-xl transition-all">
      <div className="flex items-center gap-3">
        {onToggleSidebar && (
          <button
            type="button"
            onClick={onToggleSidebar}
            aria-label="Toggle Navigation Sidebar"
            className="p-2 rounded-lg bg-slate-900 border border-slate-800 text-slate-300 hover:text-white hover:bg-slate-800 lg:hidden transition-colors cursor-pointer"
          >
            <Menu className="w-5 h-5" />
          </button>
        )}

        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-sm font-semibold tracking-wide text-slate-100 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-indigo-500 animate-pulse" />
              Enterprise Knowledge Base
            </h1>
            <StatusBadge
              status={status.supabaseConnected ? 'connected' : 'disconnected'}
              label={status.supabaseConnected ? 'Supabase Connected' : 'Supabase Offline'}
            />
          </div>
          <p className="text-[11px] text-slate-400 hidden sm:block">
            Strict Temporal Semantic Retrieval & Policy Conflict Resolution
          </p>
        </div>
      </div>

      <div className="flex items-center gap-2 sm:gap-3">
        {/* Metric counters */}
        <div className="hidden xl:flex items-center gap-2">
          <MetricCard
            label="Indexed Docs"
            value={status.indexedDocumentsCount ?? status.indexedDocuments ?? 0}
            icon={<FileText className="w-3.5 h-3.5" />}
          />
          <MetricCard
            label="Vector Chunks"
            value={status.indexedChunksCount ?? status.indexedChunks ?? 0}
            icon={<Layers className="w-3.5 h-3.5" />}
          />
          <MetricCard
            label="Current Versions"
            value={status.currentVersionsCount ?? status.currentVersions ?? 0}
            icon={<Database className="w-3.5 h-3.5" />}
          />
        </div>

        {/* Model status */}
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-900/80 border border-slate-800 text-xs">
          <div className="flex items-center gap-1.5 text-slate-300">
            <Cpu className="w-3.5 h-3.5 text-violet-400" />
            <span className="font-mono text-slate-200 font-medium">
              {status.groqModel || 'Groq LLaMA 3.3'}
            </span>
          </div>
          <span className="inline-block w-1.5 h-1.5 rounded-full bg-emerald-400 shadow-[0_0_8px_#34d399]" />
        </div>

        {/* Refresh button */}
        {onRefresh && (
          <button
            type="button"
            onClick={onRefresh}
            aria-label="Refresh Knowledge Base Status"
            title="Refresh Knowledge Base Status"
            disabled={isRefreshing}
            className="p-2 rounded-xl bg-slate-900/80 border border-slate-800 hover:border-slate-700 text-slate-400 hover:text-slate-200 transition-colors disabled:opacity-50 cursor-pointer"
          >
            <RefreshCw
              className={`w-4 h-4 ${isRefreshing ? 'animate-spin text-indigo-400' : ''}`}
            />
          </button>
        )}
      </div>
    </header>
  );
};
