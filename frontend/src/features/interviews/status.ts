import type { InterviewStatus, InterviewType } from '../../shared/api/types';

export const interviewStatusLabels: Record<InterviewStatus, string> = {
  SCHEDULED: '已安排', COMPLETED: '已完成', CANCELLED: '已取消',
};

export const interviewStatusColors: Record<InterviewStatus, string> = {
  SCHEDULED: 'blue', COMPLETED: 'green', CANCELLED: 'gray',
};

export const interviewTypeLabels: Record<InterviewType, string> = {
  PHONE: '电话', VIDEO: '视频', ONSITE: '现场', OTHER: '其他',
};
