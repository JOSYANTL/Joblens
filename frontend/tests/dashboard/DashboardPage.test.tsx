import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { DashboardPage } from '../../src/features/dashboard/DashboardPage';
import { applicationApi } from '../../src/features/applications/api';
import { activityApi } from '../../src/features/activity/api';
import { interviewApi } from '../../src/features/interviews/api';
import { taskApi } from '../../src/features/tasks/api';

vi.mock('../../src/features/applications/api', () => ({ applicationApi: { statistics: vi.fn() } }));
vi.mock('../../src/features/activity/api', () => ({ activityApi: { recent: vi.fn() } }));
vi.mock('../../src/features/interviews/api', () => ({ interviewApi: { upcoming: vi.fn() } }));
vi.mock('../../src/features/tasks/api', () => ({ taskApi: { list: vi.fn() } }));

beforeEach(() => {
  vi.clearAllMocks();
  vi.mocked(applicationApi.statistics).mockResolvedValue({
    total: 3,
    byStatus: { SAVED: 1, APPLIED: 2, INTERVIEW_SCHEDULED: 0, OFFERED: 0, REJECTED: 0 },
  });
  vi.mocked(interviewApi.upcoming).mockResolvedValue({
    content: [], page: 0, size: 3, totalElements: 0, totalPages: 0,
  });
  vi.mocked(taskApi.list).mockResolvedValue({
    content: [], page: 0, size: 3, totalElements: 0, totalPages: 0,
  });
  vi.mocked(activityApi.recent).mockResolvedValue([{
    id: 11,
    applicationId: 7,
    type: 'APPLICATION_STATUS_CHANGED',
    subjectType: 'APPLICATION',
    subjectId: 7,
    summary: '申请状态从 SAVED 更新为 APPLIED',
    occurredAt: '2030-01-01T00:00:00Z',
  }]);
});

describe('DashboardPage', () => {
  it('shows application statistics and recent cross-application activity', async () => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <MantineProvider env="test">
        <QueryClientProvider client={queryClient}>
          <MemoryRouter><DashboardPage /></MemoryRouter>
        </QueryClientProvider>
      </MantineProvider>,
    );

    expect(screen.getByText('最近活动')).toBeInTheDocument();
    expect(await screen.findByText('全部申请')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('申请状态从 SAVED 更新为 APPLIED')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '申请 #7' })).toHaveAttribute('href', '/applications/7');
    expect(activityApi.recent).toHaveBeenCalledWith(8);
  });
});
