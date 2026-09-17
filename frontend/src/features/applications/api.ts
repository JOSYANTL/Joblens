import { getJson, sendJson } from '../../shared/api/client';
import type { ApplicationStatistics, ApplicationStatus, ApplicationStatusHistory, AvailableApplicationStatuses, JobApplication, Page } from '../../shared/api/types';

export const applicationApi = {
  list: (params: { page: number; keyword?: string; status?: ApplicationStatus }) =>
    getJson<Page<JobApplication>>('/api/applications', { ...params, size: 10 }),
  statistics: () => getJson<ApplicationStatistics>('/api/applications/statistics'),
  get: (id: number) => getJson<JobApplication>(`/api/applications/${id}`),
  availableStatuses: (id: number) => getJson<AvailableApplicationStatuses>(`/api/applications/${id}/available-statuses`),
  statusHistory: (id: number) => getJson<ApplicationStatusHistory[]>(`/api/applications/${id}/status-history`),
  create: (body: { company: string; position: string; description: string }) =>
    sendJson<JobApplication>('/api/applications', 'POST', body),
  updateStatus: (id: number, status: ApplicationStatus, version: number) =>
    sendJson<JobApplication>(`/api/applications/${id}/status`, 'PATCH', { status, version }),
};
