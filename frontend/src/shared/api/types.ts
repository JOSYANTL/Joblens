export type ApplicationStatus = 'SAVED' | 'APPLIED' | 'INTERVIEW_SCHEDULED' | 'OFFERED' | 'REJECTED';

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface JobApplication {
  id: number;
  company: string;
  position: string;
  description: string;
  status: ApplicationStatus;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface ApplicationStatistics {
  total: number;
  byStatus: Partial<Record<ApplicationStatus, number>>;
}

export interface AvailableApplicationStatuses {
  currentStatus: ApplicationStatus;
  availableStatuses: ApplicationStatus[];
}

export interface ApplicationStatusHistory {
  id: number;
  fromStatus: ApplicationStatus | null;
  toStatus: ApplicationStatus;
  changedAt: string;
}

export interface Interview {
  id: number;
  applicationId: number;
  round: number;
  type: InterviewType;
  startsAt: string;
  endsAt: string;
  durationMinutes: number;
  contact: string;
  meetingUrl: string;
  location: string;
  status: InterviewStatus;
  feedback: InterviewFeedback;
  createdAt: string;
  updatedAt: string;
  completedAt: string | null;
  version: number;
}

export type InterviewType = 'PHONE' | 'VIDEO' | 'ONSITE' | 'OTHER';
export type InterviewStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';

export interface InterviewDetails {
  round: number;
  type: InterviewType;
  startsAt: string;
  durationMinutes: number;
  contact: string;
  meetingUrl: string;
  location: string;
}

export interface InterviewFeedback {
  questions: string;
  summary: string;
  nextSteps: string;
}

export interface FollowUpTask {
  id: number;
  applicationId: number;
  title: string;
  notes: string;
  dueAt: string;
  status: FollowUpTaskStatus;
  createdAt: string;
  updatedAt: string;
  completedAt: string | null;
  version: number;
  overdue: boolean;
}

export type FollowUpTaskStatus = 'TODO' | 'DONE' | 'CANCELLED';

export interface FollowUpTaskDetails {
  title: string;
  notes: string;
  dueAt: string;
}

export interface AuthUser {
  id: number;
  email: string;
  displayName: string;
}

export type NotificationType = 'INTERVIEW_UPCOMING' | 'TASK_DUE_SOON' | 'TASK_OVERDUE';
export type NotificationSourceType = 'INTERVIEW' | 'TASK';

export interface UserNotification {
  id: number;
  applicationId: number;
  type: NotificationType;
  sourceType: NotificationSourceType;
  sourceId: number;
  title: string;
  message: string;
  eventAt: string;
  createdAt: string;
  readAt: string | null;
  targetUrl: string;
}

export type ApplicationDocumentType = 'RESUME' | 'JOB_DESCRIPTION' | 'OTHER';

export interface ApplicationDocument {
  id: number;
  applicationId: number;
  type: ApplicationDocumentType;
  originalFileName: string;
  contentType: string;
  sizeBytes: number;
  createdAt: string;
  downloadUrl: string;
}

export type ApplicationActivityType =
  | 'APPLICATION_CREATED' | 'APPLICATION_UPDATED' | 'APPLICATION_STATUS_CHANGED'
  | 'INTERVIEW_SCHEDULED' | 'INTERVIEW_RESCHEDULED' | 'INTERVIEW_STATUS_CHANGED'
  | 'INTERVIEW_FEEDBACK_UPDATED' | 'TASK_CREATED' | 'TASK_UPDATED'
  | 'TASK_STATUS_CHANGED' | 'TASK_DELETED' | 'DOCUMENT_UPLOADED'
  | 'DOCUMENT_DELETED' | 'NOTE_CREATED' | 'NOTE_UPDATED' | 'NOTE_DELETED';

export type ApplicationActivitySubjectType = 'APPLICATION' | 'INTERVIEW' | 'TASK' | 'DOCUMENT' | 'NOTE';

export interface ApplicationActivity {
  id: number;
  type: ApplicationActivityType;
  subjectType: ApplicationActivitySubjectType;
  subjectId: number | null;
  summary: string;
  occurredAt: string;
}

export interface ApplicationNote {
  id: number;
  applicationId: number;
  content: string;
  createdAt: string;
  updatedAt: string;
  version: number;
}
