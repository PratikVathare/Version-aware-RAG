import { useState, useCallback } from 'react';
import { useMutation } from '@tanstack/react-query';
import { chatService } from '../services/chatService';
import type { ChatMessage, ChatResponse } from '../types';

export function useChat() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);

  // TanStack Mutation for chat queries
  const chatMutation = useMutation({
    mutationFn: async (question: string) => {
      return await chatService.sendMessage(question);
    },
    onSuccess: (response: ChatResponse) => {
      const assistantMessage: ChatMessage = {
        id: `msg-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`,
        role: 'assistant',
        content: response.answer,
        timestamp: new Date().toISOString(),
        responseMetadata: response,
      };

      setMessages((prev) => [...prev, assistantMessage]);
    },
    onError: (err: unknown) => {
      const errorMsg =
        err instanceof Error ? err.message : 'Unable to connect to RAG backend.';
      const errorMessage: ChatMessage = {
        id: `msg-err-${Date.now()}`,
        role: 'assistant',
        content: `Error retrieving version-aware response: ${errorMsg}`,
        timestamp: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, errorMessage]);
    },
  });

  const sendMessage = useCallback(
    async (text: string, _attachedFile?: File | null) => {
      if (!text.trim() || chatMutation.isPending) return;

      const userMessage: ChatMessage = {
        id: `user-${Date.now()}`,
        role: 'user',
        content: text,
        timestamp: new Date().toISOString(),
      };

      setMessages((prev) => [...prev, userMessage]);
      await chatMutation.mutateAsync(text);
    },
    [chatMutation]
  );

  const regenerateLast = useCallback(async () => {
    // Find the last user message
    for (let i = messages.length - 1; i >= 0; i--) {
      if (messages[i].role === 'user') {
        const lastQuery = messages[i].content;
        // Remove trailing assistant message if present
        if (i < messages.length - 1) {
          setMessages((prev) => prev.slice(0, i + 1));
        }
        await chatMutation.mutateAsync(lastQuery);
        break;
      }
    }
  }, [messages, chatMutation]);

  return {
    messages,
    isLoading: chatMutation.isPending,
    sendMessage,
    regenerateLast,
    clearMessages: () => setMessages([]),
  };
}
