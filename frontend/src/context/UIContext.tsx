/* oxlint-disable react/only-export-components */
import React, { createContext, useState, useCallback, useMemo } from 'react';
import type { DocumentVersion } from '../types';

export interface UIContextValue {
  isMobileSidebarOpen: boolean;
  setIsMobileSidebarOpen: (open: boolean) => void;
  toggleMobileSidebar: () => void;
  selectedVersion: DocumentVersion | null;
  setSelectedVersion: (version: DocumentVersion | null) => void;
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  selectedDepartment: string;
  setSelectedDepartment: (dept: string) => void;
}

export const UIContext = createContext<UIContextValue | undefined>(undefined);

export const UIProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);
  const [selectedVersion, setSelectedVersion] = useState<DocumentVersion | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDepartment, setSelectedDepartment] = useState('all');

  const toggleMobileSidebar = useCallback(() => {
    setIsMobileSidebarOpen((prev) => !prev);
  }, []);

  const value = useMemo(
    () => ({
      isMobileSidebarOpen,
      setIsMobileSidebarOpen,
      toggleMobileSidebar,
      selectedVersion,
      setSelectedVersion,
      searchQuery,
      setSearchQuery,
      selectedDepartment,
      setSelectedDepartment,
    }),
    [
      isMobileSidebarOpen,
      toggleMobileSidebar,
      selectedVersion,
      searchQuery,
      selectedDepartment,
    ]
  );

  return <UIContext.Provider value={value}>{children}</UIContext.Provider>;
};

// Re-export useUI from hooks
export { useUI } from '../hooks/useUI';
