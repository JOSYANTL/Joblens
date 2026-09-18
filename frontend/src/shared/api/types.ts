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
