import { z } from 'zod';

export const DocumentUploadSchema = z.object({
  documentName: z.string().min(1, 'Document name is required'),
  versionNumber: z.union([z.string().min(1), z.number().positive()]),
  effectiveDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Invalid date format (YYYY-MM-DD)'),
  department: z.string().min(1, 'Department is required'),
});

export const ChatRequestSchema = z.object({
  question: z.string().min(1, 'Question cannot be empty'),
  documentName: z.string().optional(),
  versionNumber: z.number().optional(),
});

export const RetrievedChunkSchema = z.object({
  id: z.string(),
  documentName: z.string(),
  versionNumber: z.union([z.string(), z.number()]),
  effectiveDate: z.string(),
  department: z.string().optional(),
  chunkText: z.string(),
  content: z.string().optional(),
  similarityScore: z.number().optional(),
  score: z.number().optional(),
});

export const ConflictDetailsSchema = z.object({
  currentVersion: z.string(),
  olderVersion: z.string(),
  conflictDescription: z.string(),
  conflictSummary: z.string().optional(),
  reasonSelected: z.string().optional(),
  resolutionReason: z.string().optional(),
});

export const ChatResponseSchema = z.object({
  answer: z.string(),
  documentName: z.string(),
  version: z.union([z.string(), z.number()]),
  effectiveDate: z.string(),
  department: z.string(),
  confidence: z.number().optional(),
  retrievedChunks: z.array(RetrievedChunkSchema),
  responseTime: z.union([z.string(), z.number()]).optional(),
  conflict: z.boolean().optional(),
  conflictDetails: ConflictDetailsSchema.optional(),
  warning: z.string().optional(),
});

export type DocumentUploadFormValues = z.infer<typeof DocumentUploadSchema>;
export type ValidatedChatRequest = z.infer<typeof ChatRequestSchema>;
export type ValidatedChatResponse = z.infer<typeof ChatResponseSchema>;
