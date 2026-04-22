import api from '../../../shared/api/axiosConfig';
import type { ImportExecuteResponse, ImportPreviewResponse } from '../types/import.types';

export const importApi = {
  preview: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return api.post<{ data: ImportPreviewResponse }>('/import/preview', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data.data);
  },

  execute: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return api.post<{ data: ImportExecuteResponse }>('/import/execute', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data.data);
  },
};
