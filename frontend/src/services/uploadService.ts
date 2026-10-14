import { apiClient } from './api';
import {
  DocumentUploadRequest,
  DocumentUploadResponse,
  DocumentItem,
  UploadStage,
} from '../types';
import { documentService } from './documentService';

export const uploadService = {
  /**
   * Ingests a new document or new version via multipart/form-data
   */
  async uploadDocument(
    fileOrPayload:
      | File
      | DocumentUploadRequest,
    metadataOrProgress?:
      | { version: string; department: string; effectiveDate: string }
      | ((progress: number, stage: UploadStage) => void),
    onProgressCallback?: (progress: number, stage: UploadStage) => void
  ): Promise<DocumentUploadResponse> {
    let file: File;
    let docName: string;
    let versionStr: string;
    let effectiveDate: string;
    let department: string;
    let onProgress: ((progress: number, stage: UploadStage) => void) | undefined;

    if (fileOrPayload instanceof File) {
      file = fileOrPayload;
      const meta = metadataOrProgress as {
        version: string;
        department: string;
        effectiveDate: string;
      };
      docName = file.name.replace(/\.[^/.]+$/, '');
      versionStr = meta?.version || '1.0';
      effectiveDate = meta?.effectiveDate || new Date().toISOString().split('T')[0];
      department = meta?.department || 'Operations';
      onProgress = onProgressCallback;
    } else {
      file = fileOrPayload.file;
      docName = fileOrPayload.documentName;
      versionStr = String(fileOrPayload.versionNumber);
      effectiveDate = fileOrPayload.effectiveDate;
      department = fileOrPayload.department;
      onProgress = metadataOrProgress as (progress: number, stage: UploadStage) => void;
    }

    const versionInt = parseInt(String(versionStr).replace(/[^0-9]/g, '') || '1', 10);

    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentName', docName);
    formData.append('versionNumber', String(versionInt));
    formData.append('effectiveDate', effectiveDate);
    formData.append('department', department);

    try {
      onProgress?.(25, 'Uploading Document');
      const response = await apiClient.post<{
        status: string;
        message: string;
        data: Array<{
          id: string;
          documentName: string;
          versionNumber: number;
          effectiveDate: string;
          department: string;
          chunkText: string;
        }>;
      }>(
        '/documents/ingest-pdf',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
          timeout: 180000, // 3 minutes for PDF text extraction, local model embeddings, and cloud pgvector indexing
          onUploadProgress: (progressEvent) => {
            if (progressEvent.total) {
              const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
              onProgress?.(Math.min(percent, 45), 'Uploading Document');
            }
          },
        }
      );

      onProgress?.(70, 'Generating Embeddings');
      await new Promise((r) => setTimeout(r, 300));
      onProgress?.(100, 'Indexing into Supabase');

      const savedChunks = response.data?.data || [];
      const totalChunksCount = savedChunks.length || 1;

      // Update local storage documents so sidebar updates with the real document
      const currentDocs = await documentService.getDocuments();
      const existingDoc = currentDocs.find(
        (d) =>
          d.name.toLowerCase() === docName.toLowerCase() ||
          d.documentName?.toLowerCase() === docName.toLowerCase()
      );

      const newVersionEntry = {
        id: `v-${Date.now()}`,
        versionNumber: versionInt,
        effectiveDate: effectiveDate,
        department: department,
        isCurrent: true,
        totalChunks: totalChunksCount,
        chunksCount: totalChunksCount,
        createdAt: new Date().toISOString(),
        uploadedBy: 'Current User',
        changeSummary: `Ingested ${totalChunksCount} chunks into Supabase pgvector.`,
      };

      let updatedDocs: DocumentItem[];
      if (existingDoc) {
        const updatedVersions = (existingDoc.versions || []).map((v) => ({
          ...v,
          isCurrent: false,
        }));
        updatedVersions.unshift(newVersionEntry);

        updatedDocs = currentDocs.map((d) =>
          d.id === existingDoc.id
            ? {
                ...d,
                currentVersion: versionInt,
                department: department,
                lastUpdated: new Date().toISOString(),
                totalVersions: updatedVersions.length,
                versions: updatedVersions,
              }
            : d
        );
      } else {
        const newDoc: DocumentItem = {
          id: `doc-${Date.now()}`,
          name: docName,
          documentName: docName,
          department: department,
          currentVersion: versionInt,
          lastUpdated: new Date().toISOString(),
          totalVersions: 1,
          versions: [newVersionEntry],
        };
        updatedDocs = [newDoc, ...currentDocs];
      }

      localStorage.setItem('enterprise_rag_documents', JSON.stringify(updatedDocs));

      return {
        documentId: docName,
        documentName: docName,
        version: versionInt,
        department: department,
        status: 'SUCCESS',
        message: `Successfully indexed ${totalChunksCount} chunks into Supabase`,
      };
    } catch (err: any) {
      console.error('[UploadService] Upload failed:', err);
      const errorMsg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        err?.message ||
        'Failed to connect to backend server on port 8081';
      throw new Error(`PDF Ingestion Failed: ${errorMsg}`);
    }
  },
};

