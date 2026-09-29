import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { notificationApi } from '../../src/features/notifications/api';
import { NotificationsPage } from '../../src/features/notifications/NotificationsPage';
import type { Page, UserNotification } from '../../src/shared/api/types';

vi.mock('../../src/features/notifications/api', () => ({
  notificationApi: {
    list: vi.fn(),
    markRead: vi.fn(),
    markAllRead: vi.fn(),
    remove: vi.fn(),
  },
}));

const notification: UserNotification = {
  id: 9,
  applicationId: 7,
  type: 'INTERVIEW_UPCOMING',
  sourceType: 'INTERVIEW',
  sourceId: 11,
  title: '面试即将开始',
  message: 'Acme · Backend Engineer 的面试将在 24 小时内开始。',
  eventAt: '2026-09-23T10:00:00Z',
  createdAt: '2026-09-22T10:00:00Z',
  readAt: null,
  targetUrl: '/applications/7/interviews/11',
};

beforeEach(() => {
  vi.clearAllMocks();
  vi.mocked(notificationApi.list).mockResolvedValue({
    content: [notification], page: 0, size: 20, totalElements: 1, totalPages: 1,
  } as Page<UserNotification>);
  vi.mocked(notificationApi.markRead).mockResolvedValue({ ...notification, readAt: '2026-09-22T10:01:00Z' });
  vi.mocked(notificationApi.markAllRead).mockResolvedValue(undefined);
  vi.mocked(notificationApi.remove).mockResolvedValue(undefined);
});

describe('NotificationsPage', () => {
  it('marks an unread notification before opening its target', async () => {
    const user = userEvent.setup();
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <MantineProvider env="test">
        <QueryClientProvider client={queryClient}>
          <MemoryRouter initialEntries={['/notifications']}>
            <Routes>
              <Route path="/notifications" element={<NotificationsPage />} />
              <Route path="/applications/:applicationId/interviews/:interviewId" element={<div>面试目标页</div>} />
            </Routes>
          </MemoryRouter>
        </QueryClientProvider>
      </MantineProvider>,
    );

    expect(await screen.findByText('面试即将开始')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: '查看' }));

    await waitFor(() => expect(notificationApi.markRead).toHaveBeenCalledWith(9, expect.anything()));
    expect(await screen.findByText('面试目标页')).toBeInTheDocument();
  });

  it('supports unread filtering, mark-all and deletion', async () => {
    const user = userEvent.setup();
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(<MantineProvider env="test"><QueryClientProvider client={queryClient}><MemoryRouter><NotificationsPage /></MemoryRouter></QueryClientProvider></MantineProvider>);

    expect(await screen.findByText('面试即将开始')).toBeInTheDocument();
    await user.click(screen.getByRole('switch', { name: '只看未读' }));
    await waitFor(() => expect(notificationApi.list).toHaveBeenLastCalledWith({ page: 0, unreadOnly: true }));
    await user.click(screen.getByRole('button', { name: '全部标为已读' }));
    await user.click(screen.getByRole('button', { name: '删除' }));

    expect(notificationApi.markAllRead).toHaveBeenCalledOnce();
    expect(notificationApi.remove).toHaveBeenCalledWith(9, expect.anything());
  });
});
