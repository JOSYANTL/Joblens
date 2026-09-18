import { deleteResource, getJson, sendJson } from '../../shared/api/client';
import type { FollowUpTask, FollowUpTaskDetails, FollowUpTaskStatus, Page } from '../../shared/api/types';

const taskPath = (applicationId: number, taskId: number) =>
  `/api/applications/${applicationId}/tasks/${taskId}`;

export const taskApi = {
  list: (params: { page: number; size?: number; applicationId?: number; status?: FollowUpTaskStatus; overdueOnly?: boolean; dueFrom?: string; dueTo?: string }) =>
    getJson<Page<FollowUpTask>>('/api/tasks', { size: 10, ...params, overdueOnly: params.overdueOnly ? 'true' : undefined }),
  get: (applicationId: number, taskId: number) =>
    getJson<FollowUpTask>(taskPath(applicationId, taskId)),
  create: (applicationId: number, details: FollowUpTaskDetails) =>
    sendJson<FollowUpTask>(`/api/applications/${applicationId}/tasks`, 'POST', details),
  update: (applicationId: number, taskId: number, details: FollowUpTaskDetails, version: number) =>
    sendJson<FollowUpTask>(taskPath(applicationId, taskId), 'PUT', { ...details, version }),
  changeStatus: (applicationId: number, taskId: number, status: FollowUpTaskStatus, version: number) =>
    sendJson<FollowUpTask>(`${taskPath(applicationId, taskId)}/status`, 'PATCH', { status, version }),
  remove: (applicationId: number, taskId: number, version: number) =>
    deleteResource(`${taskPath(applicationId, taskId)}?version=${version}`),
};
