import { getJson } from '../../shared/api/client';
import type { ApplicationStatistics, ApplicationStatus, JobApplication, Page } from '../../shared/api/types';

export const applicationApi = {
  list: (params: { page: number; keyword?: string; status?: ApplicationStatus }) =>
    getJson<Page<JobApplication>>('/api/applications', { ...params, size: 10 }),
  statistics: () => getJson<ApplicationStatistics>('/api/applications/statistics'),
};
