import React from 'react';
import { Layers, X, BookOpen, ShieldCheck } from 'lucide-react';
import { UploadCard } from './UploadCard';
import { SearchBox } from './SearchBox';
import { DocumentCard } from './DocumentCard';
import { useUI } from '../context/UIContext';
import type { DocumentItem, DocumentVersion, UploadStage } from '../types';

interface SidebarProps {
  documents: DocumentItem[];
  isLoading?: boolean;
  onUpload: (
    file: File,
    metadata: { version: string; department: string; effectiveDate: string }
  ) => Promise<void>;
  isUploading: boolean;
  uploadProgress: number;
  uploadStage: UploadStage;
  onSelectVersion?: (document: DocumentItem, version: DocumentVersion) => void;
  selectedVersionId?: string;
  isOpenMobile?: boolean;
  onCloseMobile?: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  documents,
  isLoading,
  onUpload,
  isUploading,
  uploadProgress,
  uploadStage,
  onSelectVersion,
  selectedVersionId,
  isOpenMobile = false,
  onCloseMobile,
}) => {
  const { searchQuery, setSearchQuery, selectedDepartment } = useUI();

  const filteredDocuments = documents.filter((doc) => {
    const docTitle = doc.name || doc.documentName || '';
    const matchesSearch =
      docTitle.toLowerCase().includes(searchQuery.toLowerCase()) ||
      doc.department.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesDept =
      selectedDepartment === 'all' ||
      doc.department.toLowerCase() === selectedDepartment.toLowerCase();
    return matchesSearch && matchesDept;
  });

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpenMobile && (
        <div
          onClick={onCloseMobile}
          role="presentation"
          aria-hidden="true"
          className="fixed inset-0 bg-black/70 backdrop-blur-sm z-40 lg:hidden transition-opacity"
        />
      )}

      {/* Sidebar container */}
      <aside
        aria-label="Enterprise Document Ingestion and Version Navigator"
        className={`fixed top-0 bottom-0 left-0 z-50 w-80 sm:w-96 lg:static lg:z-auto lg:w-[30%] lg:min-w-[340px] lg:max-w-[420px] lg:shrink-0 h-full flex flex-col bg-slate-950 border-r border-slate-800/80 transition-transform duration-300 ease-in-out shrink-0 ${
          isOpenMobile ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        {/* Brand / Logo header */}
        <div className="flex items-center justify-between px-4 py-3.5 border-b border-slate-800/80 bg-slate-950/60 backdrop-blur-md">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-indigo-500 to-violet-600 flex items-center justify-center text-white shadow-lg shadow-indigo-500/25">
              <Layers className="w-4 h-4" />
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="font-bold text-sm tracking-tight text-slate-100">
                  Version-aware RAG
                </span>
                <span className="text-[10px] px-1.5 py-0.2 rounded-md bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 font-medium">
                  v2.0
                </span>
              </div>
              <p className="text-[10px] text-slate-400 font-medium">Enterprise Knowledge Engine</p>
            </div>
          </div>

          {onCloseMobile && (
            <button
              type="button"
              onClick={onCloseMobile}
              aria-label="Close navigation sidebar"
              className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 lg:hidden transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          )}
        </div>

        {/* Scrollable sidebar contents */}
        <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4 custom-scrollbar">
          {/* Document Ingestion & Upload Card */}
          <UploadCard
            onUpload={onUpload}
            isUploading={isUploading}
            uploadProgress={uploadProgress}
            uploadStage={uploadStage}
          />

          {/* Search Box */}
          <div className="space-y-1.5">
            <div className="flex items-center justify-between text-xs px-1">
              <span className="font-semibold text-slate-300 flex items-center gap-1.5">
                <BookOpen className="w-3.5 h-3.5 text-indigo-400" />
                Knowledge Repositories
              </span>
              <span className="text-[10px] text-slate-400">
                {filteredDocuments.length} of {documents.length}
              </span>
            </div>
            <SearchBox
              value={searchQuery}
              onChange={setSearchQuery}
              placeholder="Filter by name or department..."
            />
          </div>

          {/* Document Cards List */}
          <div className="space-y-2.5 pb-4">
            {isLoading ? (
              <div className="space-y-3 py-2">
                {[1, 2, 3].map((i) => (
                  <div
                    key={i}
                    className="p-3.5 rounded-xl bg-slate-900/40 border border-slate-800 animate-pulse space-y-2.5"
                  >
                    <div className="h-4 bg-slate-800 rounded w-3/4" />
                    <div className="h-3 bg-slate-800/60 rounded w-1/2" />
                    <div className="h-2 bg-slate-800/40 rounded w-full" />
                  </div>
                ))}
              </div>
            ) : filteredDocuments.length === 0 ? (
              <div className="p-6 text-center rounded-xl bg-slate-900/30 border border-slate-800/60 text-slate-400 space-y-1.5">
                <p className="text-xs font-medium text-slate-300">No documents found</p>
                <p className="text-[11px] text-slate-400">
                  {searchQuery ? 'Try clearing your search query' : 'Upload a document above'}
                </p>
              </div>
            ) : (
              filteredDocuments.map((doc, idx) => (
                <DocumentCard
                  key={doc.id}
                  document={doc}
                  isInitiallyExpanded={idx === 0}
                  selectedVersionId={selectedVersionId}
                  onSelectVersion={onSelectVersion}
                />
              ))
            )}
          </div>
        </div>

        {/* Sidebar Footer with system note */}
        <div className="p-3 border-t border-slate-800/80 bg-slate-950/80 text-[11px] text-slate-400 flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <ShieldCheck className="w-3.5 h-3.5 text-indigo-400" />
            <span>Strict Temporal Isolation</span>
          </div>
          <span className="font-mono text-[10px] text-slate-400">REST API</span>
        </div>
      </aside>
    </>
  );
};
