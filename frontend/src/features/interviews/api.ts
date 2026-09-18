import { getJson, sendJson } from '../../shared/api/client';
import type { Interview, InterviewDetails, InterviewFeedback, InterviewStatus, Page } from '../../shared/api/types';

const interviewPath = (applicationId: number, interviewId: number) =>
  `/api/applications/${applicationId}/interviews/${interviewId}`;

export const interviewApi = {
  list: (params: { page: number; size?: number; applicationId?: number; status?: InterviewStatus }) =>
    getJson<Page<Interview>>('/api/interviews', { size: 10, ...params }),
  upcoming: (params: { page?: number; size?: number; applicationId?: number } = {}) =>
    getJson<Page<Interview>>('/api/interviews/upcoming', { size: 3, ...params }),
  get: (applicationId: number, interviewId: number) =>
    getJson<Interview>(interviewPath(applicationId, interviewId)),
  schedule: (applicationId: number, details: InterviewDetails) =>
    sendJson<Interview>(`/api/applications/${applicationId}/interviews`, 'POST', details),
  reschedule: (applicationId: number, interviewId: number, details: InterviewDetails, version: number) =>
    sendJson<Interview>(interviewPath(applicationId, interviewId), 'PUT', { details, version }),
  changeStatus: (applicationId: number, interviewId: number, status: InterviewStatus, version: number) =>
    sendJson<Interview>(`${interviewPath(applicationId, interviewId)}/status`, 'PATCH', { status, version }),
  saveFeedback: (applicationId: number, interviewId: number, feedback: InterviewFeedback, version: number) =>
    sendJson<Interview>(`${interviewPath(applicationId, interviewId)}/feedback`, 'PUT', { ...feedback, version }),
};
