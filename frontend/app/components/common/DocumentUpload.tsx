'use client';

import { useState } from 'react';
import { Document } from '@/app/types/chat';

interface DocumentUploadProps {
  onUpload: (file: File) => Promise<boolean>;
  documents: Document[];
  onDelete: (id: number) => Promise<void>;
  disabled?: boolean;
}

export const DocumentUpload: React.FC<DocumentUploadProps> = ({
  onUpload,
  documents,
  onDelete,
  disabled = false
}) => {
  const [uploading, setUploading] = useState(false);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file type
    const validTypes = ['.pdf', '.docx', '.txt'];
    const fileExtension = file.name.toLowerCase().slice(-4);
    if (!validTypes.some(type => file.name.toLowerCase().endsWith(type))) {
      alert('Please upload a PDF, DOCX, or TXT file');
      return;
    }

    // Validate file size (10MB)
    if (file.size > 10 * 1024 * 1024) {
      alert('File size must be less than 10MB');
      return;
    }

    setUploading(true);
    const success = await onUpload(file);
    setUploading(false);
    
    if (success) {
      e.target.value = '';
    } else {
      alert('Error uploading document');
    }
  };

  const handleDelete = async (id: number) => {
    if (confirm('Are you sure you want to delete this document?')) {
      await onDelete(id);
    }
  };

  return (
    <div className="space-y-4">
      <div>
        <label className="block text-sm font-medium mb-2">
          Upload Document (PDF, DOCX, TXT)
        </label>
        <input
          type="file"
          accept=".pdf,.docx,.txt"
          onChange={handleFileChange}
          disabled={disabled || uploading}
          className="w-full p-2 border rounded disabled:opacity-50 text-sm"
        />
        {uploading && (
          <div className="flex items-center text-blue-600 text-sm mt-1">
            <div className="flex space-x-1 mr-2">
              <div className="w-2 h-2 bg-blue-600 rounded-full loading-dot"></div>
              <div className="w-2 h-2 bg-blue-600 rounded-full loading-dot"></div>
              <div className="w-2 h-2 bg-blue-600 rounded-full loading-dot"></div>
            </div>
            Uploading...
          </div>
        )}
      </div>

      {documents.length > 0 && (
        <div>
          <h4 className="font-medium mb-2">Uploaded Documents ({documents.length})</h4>
          <div className="border rounded divide-y max-h-48 overflow-y-auto">
            {documents.map(doc => (
              <div key={doc.id} className="p-3 flex justify-between items-center">
                <div className="flex-1 min-w-0">
                  <div className="font-medium truncate text-sm">{doc.fileName}</div>
                  <div className="text-xs text-gray-500">
                    {(doc.fileSize / 1024).toFixed(1)} KB • 
                    {new Date(doc.uploadedAt).toLocaleDateString()}
                  </div>
                </div>
                <button
                  onClick={() => handleDelete(doc.id)}
                  disabled={disabled}
                  className="ml-2 px-3 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600 disabled:opacity-50 transition-colors"
                >
                  Delete
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};