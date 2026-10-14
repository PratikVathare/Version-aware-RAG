import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { documentService } from '../services/documentService';
import { uploadService } from '../services/uploadService';
import { INITIAL_KNOWLEDGE_STATUS } from '../constants/mockData';
import type { DocumentItem, UploadStage, KnowledgeBaseStatus } from '../types';

export function useDocuments() {
  const queryClient = useQueryClient();
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadStage, setUploadStage] = useState<UploadStage>('Uploading Document');

  // Query: Documents list
  const {
    data: documents = [],
    isLoading: isDocumentsLoading,
    refetch: refetchDocuments,
    isRefetching: isRefetchingDocuments,
  } = useQuery<DocumentItem[]>({
    queryKey: ['documents'],
    queryFn: documentService.getDocuments,
    staleTime: 1000 * 60 * 5, // 5 mins
  });

  // Query: Knowledge base status
  const {
    data: status = INITIAL_KNOWLEDGE_STATUS,
    isLoading: isStatusLoading,
    refetch: refetchStatus,
  } = useQuery<KnowledgeBaseStatus>({
    queryKey: ['knowledgeStatus'],
    queryFn: documentService.getKnowledgeStatus,
    staleTime: 1000 * 60 * 2,
  });

  // Mutation: Upload document
  const uploadMutation = useMutation({
    mutationFn: async ({
      file,
      metadata,
    }: {
      file: File;
      metadata: { version: string; department: string; effectiveDate: string };
    }) => {
      setUploadProgress(0);
      setUploadStage('Uploading Document');

      return await uploadService.uploadDocument(file, metadata, (progress: number, stage: UploadStage) => {
        setUploadProgress(progress);
        setUploadStage(stage);
      });
    },
    onSuccess: () => {
      // Invalidate queries so TanStack Query refetches fresh data
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      queryClient.invalidateQueries({ queryKey: ['knowledgeStatus'] });
    },
  });

  const uploadDocument = async (
    file: File,
    metadata: { version: string; department: string; effectiveDate: string }
  ): Promise<void> => {
    await uploadMutation.mutateAsync({ file, metadata });
  };

  return {
    documents,
    isDocumentsLoading,
    refetchDocuments,
    isRefetchingDocuments,
    status,
    isStatusLoading,
    refetchStatus,
    uploadDocument,
    isUploading: uploadMutation.isPending,
    uploadProgress,
    uploadStage,
  };
}
