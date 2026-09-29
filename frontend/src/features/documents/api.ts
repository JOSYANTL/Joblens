import { deleteResource, getJson, sendFormData } from '../../shared/api/client';
import type { ApplicationDocument, ApplicationDocumentType } from '../../shared/api/types';

export const documentApi = {
  list: (applicationId: number) =>
    getJson<ApplicationDocument[]>(`/api/applications/${applicationId}/documents`),
  upload: (applicationId: number, type: ApplicationDocumentType, file: File) => {
    const form = new FormData();
    form.set('type', type);
    form.set('file', file);
    return sendFormData<ApplicationDocument>(`/api/applications/${applicationId}/documents`, form);
  },
  remove: (applicationId: number, documentId: number) =>
    deleteResource(`/api/applications/${applicationId}/documents/${documentId}`),
};
