import React, { useState, useRef, useCallback } from 'react';
import { Paperclip, CornerDownLeft, Sparkles, X } from 'lucide-react';

interface ChatInputProps {
  onSendMessage: (text: string, attachedFile?: File | null) => void;
  isLoading: boolean;
  onAttachClick?: () => void;
}

export const ChatInput: React.FC<ChatInputProps> = ({
  onSendMessage,
  isLoading,
}) => {
  const [input, setInput] = useState('');
  const [attachedFile, setAttachedFile] = useState<File | null>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const adjustTextareaHeight = useCallback(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${Math.min(
        textareaRef.current.scrollHeight,
        160
      )}px`;
    }
  }, []);

  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setInput(e.target.value);
    adjustTextareaHeight();
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  const handleSubmit = () => {
    if ((!input.trim() && !attachedFile) || isLoading) return;
    onSendMessage(input.trim(), attachedFile);
    setInput('');
    setAttachedFile(null);
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setAttachedFile(e.target.files[0]);
    }
  };

  return (
    <div className="sticky bottom-0 z-20 px-4 lg:px-6 py-4 bg-gradient-to-t from-slate-950 via-slate-950/95 to-transparent backdrop-blur-md">
      <div className="max-w-4xl mx-auto space-y-2">
        {/* Attached file tag if any */}
        {attachedFile && (
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-lg bg-indigo-950/60 border border-indigo-500/40 text-xs text-indigo-200">
            <Paperclip className="w-3.5 h-3.5 text-indigo-400" />
            <span className="font-medium truncate max-w-[200px]">
              {attachedFile.name}
            </span>
            <button
              type="button"
              onClick={() => setAttachedFile(null)}
              aria-label={`Remove attached file ${attachedFile.name}`}
              className="p-0.5 rounded hover:bg-indigo-800/40 text-slate-400 hover:text-white cursor-pointer"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        )}

        {/* Input box */}
        <div className="relative rounded-2xl border border-slate-800 bg-slate-900/90 shadow-2xl focus-within:border-indigo-500/80 focus-within:ring-2 focus-within:ring-indigo-500/20 transition-all flex flex-col p-2">
          <label htmlFor="chat-query-input" className="sr-only">
            Ask corporate policy or version question
          </label>
          <textarea
            id="chat-query-input"
            ref={textareaRef}
            rows={1}
            value={input}
            onChange={handleInputChange}
            onKeyDown={handleKeyDown}
            aria-label="Ask about corporate policies, version updates, or cross-document amendments"
            placeholder="Ask about corporate policies, version updates, or cross-document amendments..."
            disabled={isLoading}
            className="w-full bg-transparent px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none resize-none min-h-[44px] max-h-[160px] custom-scrollbar font-sans"
          />

          <div className="flex items-center justify-between pt-2 border-t border-slate-800/60 px-2">
            <div className="flex items-center gap-2">
              <input
                ref={fileInputRef}
                id="chat-attachment-input"
                type="file"
                accept=".pdf,.txt,.doc,.docx"
                aria-label="Upload reference attachment"
                className="hidden"
                onChange={handleFileChange}
              />
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                disabled={isLoading}
                aria-label="Attach PDF document to query"
                title="Attach Document"
                className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800/80 text-xs transition-colors disabled:opacity-50 cursor-pointer"
              >
                <Paperclip className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Attach PDF</span>
              </button>

              <span className="text-[11px] text-slate-400 hidden md:inline">
                Shift + Enter for newline
              </span>
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={handleSubmit}
                disabled={(!input.trim() && !attachedFile) || isLoading}
                aria-label="Submit query to RAG engine"
                className="flex items-center gap-1.5 px-4 py-1.5 rounded-xl bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/30 transition-all disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
              >
                <span>Send</span>
                {isLoading ? (
                  <Sparkles className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <CornerDownLeft className="w-3.5 h-3.5" />
                )}
              </button>
            </div>
          </div>
        </div>

        <div className="text-center">
          <p className="text-[10px] text-slate-400">
            Antigravity Enterprise RAG Engine • Powered by Spring AI & Supabase pgvector
          </p>
        </div>
      </div>
    </div>
  );
};
