import type { ApplicationStatus } from '../../shared/api/types';

export const statusLabels: Record<ApplicationStatus, string> = {
  SAVED: '已收藏', APPLIED: '已投递', INTERVIEW_SCHEDULED: '面试中', OFFERED: '已录用', REJECTED: '未通过',
};

export const statusColors: Record<ApplicationStatus, string> = {
  SAVED: 'gray', APPLIED: 'blue', INTERVIEW_SCHEDULED: 'violet', OFFERED: 'green', REJECTED: 'red',
};
