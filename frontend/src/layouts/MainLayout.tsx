import React, { useEffect, useCallback } from 'react';
import { Sidebar } from '../components/Sidebar';
import { useUI } from '../context/UIContext';
import { useDocuments } from '../hooks/useDocuments';
import type { DocumentItem, DocumentVersion } from '../types';

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const { isMobileSidebarOpen, setIsMobileSidebarOpen, selectedVersion, setSelectedVersion } = useUI();

  const {
    documents,
    isDocumentsLoading,
    uploadDocument,
    isUploading,
    uploadProgress,
    uploadStage,
  } = useDocuments();

  // Close mobile sidebar on Escape key
  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isMobileSidebarOpen) {
        setIsMobileSidebarOpen(false);
      }
    },
    [isMobileSidebarOpen, setIsMobileSidebarOpen]
  );

  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);

  const handleSelectVersion = useCallback(
    (_doc: DocumentItem, version: DocumentVersion) => {
      setSelectedVersion(version);
    },
    [setSelectedVersion]
  );

  const handleCloseMobile = useCallback(() => {
    setIsMobileSidebarOpen(false);
  }, [setIsMobileSidebarOpen]);

  return (
    <div className="flex h-screen h-[100dvh] w-full max-w-full overflow-hidden bg-slate-950 font-sans text-slate-100 antialiased selection:bg-indigo-500/30">
      {/* 
        Single Sidebar Instance:
        - Desktop: 30% width sticky panel (lg:static lg:w-[30%] lg:min-w-[340px] lg:max-w-[420px])
        - Mobile/Tablet: Slide-out drawer with blur backdrop
      */}
      <Sidebar
        documents={documents}
        isLoading={isDocumentsLoading}
        onUpload={uploadDocument}
        isUploading={isUploading}
        uploadProgress={uploadProgress}
        uploadStage={uploadStage}
        onSelectVersion={handleSelectVersion}
        selectedVersionId={selectedVersion?.id}
        isOpenMobile={isMobileSidebarOpen}
        onCloseMobile={handleCloseMobile}
      />

      {/* 70% Chat Workspace Container */}
      <div className="flex-1 h-full min-w-0 flex flex-col relative overflow-hidden">
        {children}
      </div>
    </div>
  );
};
