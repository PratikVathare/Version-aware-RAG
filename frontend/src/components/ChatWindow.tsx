import React, { useRef, useEffect } from 'react';
import { Header } from './Header';
import { MessageBubble } from './MessageBubble';
import { TypingIndicator } from './TypingIndicator';
import { EmptyState } from './EmptyState';
import { ChatInput } from './ChatInput';
import type { ChatMessage, KnowledgeBaseStatus } from '../types';

interface ChatWindowProps {
  messages: ChatMessage[];
  isLoading: boolean;
  onSendMessage: (query: string, attachedFile?: File | null) => void;
  onRegenerateLast?: () => void;
  status: KnowledgeBaseStatus;
  onRefreshStatus?: () => void;
  isRefreshingStatus?: boolean;
  hasDocuments: boolean;
  onOpenUpload: () => void;
  onToggleSidebar?: () => void;
}

export const ChatWindow: React.FC<ChatWindowProps> = ({
  messages,
  isLoading,
  onSendMessage,
  onRegenerateLast,
  status,
  onRefreshStatus,
  isRefreshingStatus,
  hasDocuments,
  onOpenUpload,
  onToggleSidebar,
}) => {
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const bottomAnchorRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom on messages update or loading state change
  useEffect(() => {
    bottomAnchorRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages.length, isLoading]);

  return (
    <div className="flex-1 flex flex-col h-full bg-slate-950 min-w-0 relative">
      {/* Sticky Chat Header */}
      <Header
        status={status}
        onRefresh={onRefreshStatus}
        isRefreshing={isRefreshingStatus}
        onToggleSidebar={onToggleSidebar}
      />

      {/* Messages Scroll Area */}
      <div
        ref={scrollContainerRef}
        role="log"
        aria-live="polite"
        aria-label="Conversation message history"
        className="flex-1 overflow-y-auto px-4 lg:px-8 py-6 space-y-6 custom-scrollbar flex flex-col"
      >
        {messages.length === 0 ? (
          <EmptyState
            hasDocuments={hasDocuments}
            onUploadClick={onOpenUpload}
            onSamplePromptClick={(prompt) => onSendMessage(prompt)}
          />
        ) : (
          <div className="space-y-6 max-w-4xl w-full mx-auto">
            {messages.map((msg, idx) => (
              <MessageBubble
                key={msg.id}
                message={msg}
                onRegenerate={
                  idx === messages.length - 1 && msg.role === 'assistant'
                    ? onRegenerateLast
                    : undefined
                }
              />
            ))}

            {isLoading && <TypingIndicator />}

            <div ref={bottomAnchorRef} aria-hidden="true" />
          </div>
        )}
      </div>

      {/* Sticky Chat Input */}
      <ChatInput
        onSendMessage={onSendMessage}
        isLoading={isLoading}
        onAttachClick={onOpenUpload}
      />
    </div>
  );
};
