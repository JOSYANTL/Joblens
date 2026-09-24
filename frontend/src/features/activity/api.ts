import { deleteResource, getJson, sendJson } from '../../shared/api/client';
import type { ApplicationActivity, ApplicationActivityType, ApplicationNote, Page } from '../../shared/api/types';

export const activityApi = {
  recent: (size = 8) =>
    getJson<ApplicationActivity[]>('/api/activities/recent', { size }),
  list: (applicationId: number, params: { page: number; size?: number; type?: ApplicationActivityType }) =>
    getJson<Page<ApplicationActivity>>(`/api/applications/${applicationId}/activities`, {
      page: params.page, size: params.size ?? 10, type: params.type,
    }),
  notes: (applicationId: number) =>
    getJson<ApplicationNote[]>(`/api/applications/${applicationId}/notes`),
  createNote: (applicationId: number, content: string) =>
    sendJson<ApplicationNote>(`/api/applications/${applicationId}/notes`, 'POST', { content }),
  updateNote: (applicationId: number, noteId: number, content: string, version: number) =>
    sendJson<ApplicationNote>(`/api/applications/${applicationId}/notes/${noteId}`, 'PUT', { content, version }),
  deleteNote: (applicationId: number, noteId: number, version: number) =>
    deleteResource(`/api/applications/${applicationId}/notes/${noteId}?version=${version}`),
};
