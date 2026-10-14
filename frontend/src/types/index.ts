export type UploadStage =
  | 'Uploading Document'
  | 'Extracting Text'
  | 'Generating Embeddings'
  | 'Indexing into Supabase';

export interface DocumentVersion {
  id: string;
  versionNumber: number | string;
  effectiveDate: string;
  department: string;
  isCurrent: boolean;
  totalChunks?: number;
  chunksCount?: number;
  createdAt: string;
  uploadedBy?: string;
  changeSummary?: string;
}

export interface DocumentItem {
  id: string;
  name: string;
  documentName?: string;
  department: string;
  currentVersion: number | string;
  lastUpdated: string;
  totalVersions: number;
  versions: DocumentVersion[];
}

export interface DocumentUploadRequest {
  file: File;
  documentName: string;
  versionNumber: number | string;
  effectiveDate: string;
  department: string;
}

export interface DocumentUploadResponse {
  documentId: string;
  documentName: string;
  version: number | string;
  department: string;
  status: string;
  message?: string;
}

export interface RetrievedChunk {
  id: string;
  documentName: string;
  versionNumber: number | string;
  effectiveDate: string;
  department?: string;
  chunkText: string;
  content?: string;
  similarityScore?: number;
  score?: number;
}

export interface ConflictDetails {
  currentVersion: string;
  olderVersion: string;
  conflictDescription: string;
  conflictSummary?: string;
  reasonSelected?: string;
  resolutionReason?: string;
}

export interface ChatRequest {
  question: string;
  documentName?: string;
  versionNumber?: number;
}

export interface ChatResponse {
  answer: string;
  documentName: string;
  version: string | number;
  effectiveDate: string;
  department: string;
  confidence?: number;
  retrievedChunks: RetrievedChunk[];
  responseTime?: number | string;
  conflict?: boolean;
  conflictDetails?: ConflictDetails;
  warning?: string;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
  responseMetadata?: ChatResponse;
  metadata?: {
    documentName?: string;
    version?: string | number;
    effectiveDate?: string;
    department?: string;
    confidence?: number;
    retrievedChunks?: RetrievedChunk[];
    responseTime?: number | string;
    conflict?: boolean;
    conflictDetails?: ConflictDetails;
    warning?: string;
  };
}

export interface KnowledgeBaseStatus {
  supabaseConnected: boolean;
  indexedDocumentsCount: number;
  indexedChunksCount: number;
  currentVersionsCount: number;
  indexedDocuments?: number;
  indexedChunks?: number;
  currentVersions?: number;
  groqStatus: 'active' | 'standby' | 'offline' | 'operational';
  groqModel: string;
}
