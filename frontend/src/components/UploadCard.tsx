import React, { useState, useRef } from 'react';
import { UploadCloud, CheckCircle2, FileUp, Sparkles, AlertCircle } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { DEPARTMENTS } from '../constants/mockData';
import type { UploadStage } from '../types';

interface UploadCardProps {
  onUpload: (
    file: File,
    metadata: { version: string; department: string; effectiveDate: string }
  ) => Promise<void>;
  isUploading: boolean;
  uploadProgress: number;
  uploadStage: UploadStage;
}

export const UploadCard: React.FC<UploadCardProps> = ({
  onUpload,
  isUploading,
  uploadProgress,
  uploadStage,
}) => {
  const [dragActive, setDragActive] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [version, setVersion] = useState('2.0');
  const [department, setDepartment] = useState('Human Resources');
  const [effectiveDate, setEffectiveDate] = useState(() =>
    new Date().toISOString().split('T')[0]
  );
  const [isSuccess, setIsSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      setSelectedFile(file);
      setErrorMessage(null);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
      setErrorMessage(null);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) {
      setErrorMessage('Please select a PDF document first.');
      return;
    }

    try {
      setErrorMessage(null);
      await onUpload(selectedFile, { version, department, effectiveDate });
      setIsSuccess(true);
      setTimeout(() => {
        setIsSuccess(false);
        setSelectedFile(null);
      }, 2500);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to index document';
      setErrorMessage(message);
    }
  };

  return (
    <div className="rounded-2xl border border-indigo-500/20 bg-gradient-to-b from-slate-900/90 to-slate-950/90 p-3.5 shadow-xl shadow-black/40 relative overflow-hidden backdrop-blur-md">
      {/* Decorative subtle ambient glow */}
      <div className="absolute -top-12 -right-12 w-28 h-28 bg-indigo-500/10 rounded-full blur-2xl pointer-events-none" />

      <div className="flex items-center justify-between mb-2.5">
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-lg bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">
            <UploadCloud className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-xs font-semibold text-slate-100">Upload Document</h3>
            <p className="text-[10px] text-slate-400">Ingest new enterprise version</p>
          </div>
        </div>
      </div>

      <AnimatePresence mode="wait">
        {isSuccess ? (
          <motion.div
            key="success"
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.9 }}
            className="py-6 flex flex-col items-center justify-center text-center space-y-2 bg-emerald-950/20 rounded-xl border border-emerald-500/30"
          >
            <div className="p-3 rounded-full bg-emerald-500/20 text-emerald-400 shadow-[0_0_20px_rgba(52,211,153,0.3)]">
              <CheckCircle2 className="w-6 h-6 animate-bounce" />
            </div>
            <h4 className="text-xs font-semibold text-emerald-300">
              Document Indexed Successfully!
            </h4>
            <p className="text-[11px] text-slate-400 max-w-[220px]">
              Version {version} is now current in Supabase vector store.
            </p>
          </motion.div>
        ) : isUploading ? (
          <motion.div
            key="uploading"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="py-5 px-3 rounded-xl bg-slate-950/80 border border-indigo-500/30 space-y-3"
          >
            <div className="flex items-center justify-between text-xs">
              <span className="flex items-center gap-2 font-medium text-slate-200">
                <Sparkles className="w-3.5 h-3.5 text-indigo-400 animate-spin" />
                {uploadStage}
              </span>
              <span className="font-mono text-indigo-400 font-semibold">
                {uploadProgress}%
              </span>
            </div>

            {/* Progress bar */}
            <div
              role="progressbar"
              aria-valuenow={uploadProgress}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-label={uploadStage}
              className="w-full bg-slate-800 rounded-full h-2 overflow-hidden border border-slate-700"
            >
              <motion.div
                className="h-full bg-gradient-to-r from-indigo-500 via-violet-500 to-emerald-400"
                style={{ width: `${uploadProgress}%` }}
                initial={{ width: '0%' }}
                animate={{ width: `${uploadProgress}%` }}
                transition={{ ease: 'easeOut' }}
              />
            </div>

            <div className="flex items-center justify-between text-[10px] text-slate-400">
              <span>Extracting chunks</span>
              <span>Vectorizing (Groq/Nomic)</span>
            </div>
          </motion.div>
        ) : (
          <motion.form
            key="form"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onSubmit={handleSubmit}
            className="space-y-2.5"
          >
            {/* Drag & drop area */}
            <div
              role="button"
              tabIndex={0}
              aria-label="Click or drag files here to upload"
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  fileInputRef.current?.click();
                }
              }}
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={handleDrop}
              onClick={() => fileInputRef.current?.click()}
              className={`p-3.5 rounded-xl border border-dashed text-center cursor-pointer transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 ${
                dragActive
                  ? 'border-indigo-400 bg-indigo-500/10'
                  : selectedFile
                  ? 'border-emerald-500/50 bg-emerald-950/10'
                  : 'border-slate-700/80 hover:border-indigo-500/50 bg-slate-950/40 hover:bg-slate-950/60'
              }`}
            >
              <input
                ref={fileInputRef}
                id="upload-doc-file"
                type="file"
                accept=".pdf,.txt,.docx"
                aria-label="Upload document file"
                className="hidden"
                onChange={handleChange}
              />

              {selectedFile ? (
                <div className="flex items-center justify-center gap-2 text-emerald-300">
                  <FileUp className="w-4 h-4 shrink-0" />
                  <span className="text-xs font-medium truncate max-w-[180px]">
                    {selectedFile.name}
                  </span>
                </div>
              ) : (
                <div className="space-y-1">
                  <p className="text-xs font-medium text-slate-300">
                    Drag & drop PDF here, or{' '}
                    <span className="text-indigo-400 underline font-semibold">browse</span>
                  </p>
                  <p className="text-[10px] text-slate-500">PDF, TXT, DOCX up to 25MB</p>
                </div>
              )}
            </div>

            {/* Metadata fields: Version, Department, Date */}
            <div className="grid grid-cols-2 gap-2 text-xs">
              <div>
                <label htmlFor="upload-doc-version" className="block text-[10px] font-medium text-slate-400 mb-1">
                  Version
                </label>
                <input
                  id="upload-doc-version"
                  type="text"
                  value={version}
                  onChange={(e) => setVersion(e.target.value)}
                  placeholder="e.g. 2.0"
                  className="w-full px-2.5 py-1.5 rounded-lg bg-slate-950/70 border border-slate-800 text-slate-200 text-xs focus:outline-none focus:border-indigo-500"
                  required
                />
              </div>

              <div>
                <label htmlFor="upload-doc-date" className="block text-[10px] font-medium text-slate-400 mb-1">
                  Effective Date
                </label>
                <input
                  id="upload-doc-date"
                  type="date"
                  value={effectiveDate}
                  onChange={(e) => setEffectiveDate(e.target.value)}
                  className="w-full px-2.5 py-1.5 rounded-lg bg-slate-950/70 border border-slate-800 text-slate-200 text-xs focus:outline-none focus:border-indigo-500"
                  required
                />
              </div>
            </div>

            <div>
              <label htmlFor="upload-doc-department" className="block text-[10px] font-medium text-slate-400 mb-1">
                Department
              </label>
              <select
                id="upload-doc-department"
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                className="w-full px-2.5 py-1.5 rounded-lg bg-slate-950/70 border border-slate-800 text-slate-200 text-xs focus:outline-none focus:border-indigo-500"
              >
                {DEPARTMENTS.map((dept: string) => (
                  <option key={dept} value={dept} className="bg-slate-900 text-slate-200">
                    {dept}
                  </option>
                ))}
              </select>
            </div>

            {errorMessage && (
              <div role="alert" className="p-2 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-300 text-[11px] flex items-center gap-1.5">
                <AlertCircle className="w-3.5 h-3.5 shrink-0" />
                <span>{errorMessage}</span>
              </div>
            )}

            <button
              type="submit"
              disabled={!selectedFile || isUploading}
              className="w-full mt-1 py-2 px-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 disabled:pointer-events-none text-white text-xs font-semibold shadow-lg shadow-indigo-600/30 transition-all flex items-center justify-center gap-2 cursor-pointer"
            >
              <FileUp className="w-3.5 h-3.5" />
              Ingest & Index Version
            </button>
          </motion.form>
        )}
      </AnimatePresence>
    </div>
  );
};
