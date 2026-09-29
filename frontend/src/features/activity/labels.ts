import type { ApplicationActivityType } from '../../shared/api/types';

export const activityTypeLabels: Record<ApplicationActivityType, string> = {
  APPLICATION_CREATED: '创建申请', APPLICATION_UPDATED: '更新申请', APPLICATION_STATUS_CHANGED: '更新申请状态',
  INTERVIEW_SCHEDULED: '预约面试', INTERVIEW_RESCHEDULED: '调整面试', INTERVIEW_STATUS_CHANGED: '更新面试状态',
  INTERVIEW_FEEDBACK_UPDATED: '更新面试反馈', TASK_CREATED: '创建任务', TASK_UPDATED: '更新任务',
  TASK_STATUS_CHANGED: '更新任务状态', TASK_DELETED: '删除任务', DOCUMENT_UPLOADED: '上传文档',
  DOCUMENT_DELETED: '删除文档', NOTE_CREATED: '添加备注', NOTE_UPDATED: '更新备注', NOTE_DELETED: '删除备注',
};
