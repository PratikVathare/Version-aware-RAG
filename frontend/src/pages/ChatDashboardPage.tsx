import React, { useCallback } from 'react';
import { MainLayout } from '../layouts/MainLayout';
import { ChatWindow } from '../components/ChatWindow';
import { useDocuments } from '../hooks/useDocuments';
import { useChat } from '../hooks/useChat';
import { useUI } from '../context/UIContext';

export const ChatDashboardPage: React.FC = () => {
  const { toggleMobileSidebar, setIsMobileSidebarOpen } = useUI();

  const {
    documents,
    status,
    refetchStatus,
    isStatusLoading,
  } = useDocuments();

  const {
    messages,
    isLoading: isChatLoading,
    sendMessage,
    regenerateLast,
  } = useChat();

  const handleSendMessage = useCallback(
    (query: string, attachedFile?: File | null) => {
      sendMessage(query, attachedFile);
    },
    [sendMessage]
  );

  const handleOpenUpload = useCallback(() => {
    setIsMobileSidebarOpen(true);
  }, [setIsMobileSidebarOpen]);

  return (
    <MainLayout>
      <ChatWindow
        messages={messages}
        isLoading={isChatLoading}
        onSendMessage={handleSendMessage}
        onRegenerateLast={regenerateLast}
        status={status}
        onRefreshStatus={refetchStatus}
        isRefreshingStatus={isStatusLoading}
        hasDocuments={documents.length > 0}
        onOpenUpload={handleOpenUpload}
        onToggleSidebar={toggleMobileSidebar}
      />
    </MainLayout>
  );
};
