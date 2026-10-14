import { apiClient } from "./api";
import { DocumentItem, DocumentVersion, KnowledgeBaseStatus } from "../types";
import { MOCK_DOCUMENTS, INITIAL_KNOWLEDGE_STATUS } from "../constants/mockData";

export const documentService = {
  /**
   * Retrieves all documents with version summaries
   */
  async getDocuments(): Promise<DocumentItem[]> {
    try {
      const response = await apiClient.get<any>("/documents");
      const list = Array.isArray(response.data) ? response.data : response.data?.data;
      if (Array.isArray(list) && list.length > 0) {
        return list;
      }
      const stored = localStorage.getItem("enterprise_rag_documents");
      if (stored) {
        try {
          return JSON.parse(stored);
        } catch {
          // ignore
        }
      }
      return MOCK_DOCUMENTS;
    } catch {
      // Fallback to local storage or mock dataset when backend is not running
      const stored = localStorage.getItem("enterprise_rag_documents");
      if (stored) {
        try {
          return JSON.parse(stored);
        } catch {
          // ignore
        }
      }
      return MOCK_DOCUMENTS;
    }
  },

  /**
   * Retrieves all versions for a given document id
   */
  async getDocumentVersions(documentId: string): Promise<DocumentVersion[]> {
    try {
      const response = await apiClient.get<DocumentVersion[]>(`/documents/${documentId}/versions`);
      return response.data;
    } catch {
      const docs = await documentService.getDocuments();
      const match = docs.find((d) => d.id === documentId);
      return match?.versions || [];
    }
  },

  /**
   * Retrieves knowledge base system statistics (Supabase, Chunks, Groq status)
   */
  async getKnowledgeStatus(): Promise<KnowledgeBaseStatus> {
    try {
      const response = await apiClient.get<KnowledgeBaseStatus>("/system/status");
      return response.data;
    } catch {
      const docs = await documentService.getDocuments();
      const totalVersions = docs.reduce((acc, d) => acc + (d.versions?.length || d.totalVersions || 1), 0);
      const totalChunks = docs.reduce(
        (acc, d) => acc + (d.versions?.reduce((vAcc, v) => vAcc + (v.totalChunks ?? v.chunksCount ?? 15), 0) || 15),
        0
      );

      return {
        ...INITIAL_KNOWLEDGE_STATUS,
        indexedDocumentsCount: docs.length,
        currentVersionsCount: totalVersions,
        indexedChunksCount: totalChunks || 142,
      };
    }
  },
};
