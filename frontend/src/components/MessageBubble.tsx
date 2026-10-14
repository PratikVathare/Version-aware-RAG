import React, { memo } from 'react';
import { User, Sparkles } from 'lucide-react';
import { motion } from 'framer-motion';
import { AnswerCard } from './AnswerCard';
import type { ChatMessage } from '../types';

interface MessageBubbleProps {
  message: ChatMessage;
  onRegenerate?: () => void;
}

export const MessageBubble: React.FC<MessageBubbleProps> = memo(({
  message,
  onRegenerate,
}) => {
  const isUser = message.role === 'user';

  if (isUser) {
    return (
      <motion.div
        initial={{ opacity: 0, y: 10, scale: 0.98 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        role="region"
        aria-label="User inquiry"
        className="flex items-start justify-end gap-3 max-w-3xl ml-auto"
      >
        <div className="rounded-2xl rounded-tr-sm bg-gradient-to-r from-indigo-600 to-violet-600 px-4 py-3 text-sm text-white shadow-lg shadow-indigo-600/20 max-w-[85%] leading-relaxed font-sans">
          {message.content}
        </div>
        <div
          aria-hidden="true"
          className="w-8 h-8 rounded-xl bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-300 text-xs font-semibold shrink-0 shadow-sm mt-0.5"
        >
          <User className="w-4 h-4" />
        </div>
      </motion.div>
    );
  }

  // Assistant response
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      role="region"
      aria-label="Enterprise Assistant Response"
      className="flex items-start gap-3 max-w-4xl"
    >
      <div
        aria-hidden="true"
        className="w-8 h-8 rounded-xl bg-gradient-to-br from-indigo-500 via-indigo-600 to-violet-600 flex items-center justify-center text-white text-xs font-bold shrink-0 shadow-md shadow-indigo-500/25 mt-1"
      >
        <Sparkles className="w-4 h-4" />
      </div>

      <div className="flex-1 min-w-0">
        {message.responseMetadata ? (
          <AnswerCard
            response={message.responseMetadata}
            onRegenerate={onRegenerate}
          />
        ) : (
          <div className="rounded-2xl rounded-tl-sm bg-slate-900/80 border border-slate-800 p-4 text-sm text-slate-100 shadow-md">
            {message.content}
          </div>
        )}
      </div>
    </motion.div>
  );
});

MessageBubble.displayName = 'MessageBubble';
