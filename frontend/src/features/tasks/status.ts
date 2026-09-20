import type { FollowUpTaskStatus } from '../../shared/api/types';

export const taskStatusLabels: Record<FollowUpTaskStatus, string> = {
  TODO: '待办', DONE: '已完成', CANCELLED: '已取消',
};

export const taskStatusColors: Record<FollowUpTaskStatus, string> = {
  TODO: 'blue', DONE: 'green', CANCELLED: 'gray',
};
