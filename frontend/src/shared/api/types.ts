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
  type: string;
  startsAt: string;
  status: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';
}

export interface FollowUpTask {
  id: number;
  applicationId: number;
  title: string;
  dueAt: string;
  status: string;
  overdue: boolean;
}
