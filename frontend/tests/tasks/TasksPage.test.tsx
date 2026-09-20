import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { FollowUpTask, Page } from '../../src/shared/api/types';
import { taskApi } from '../../src/features/tasks/api';
import { TasksPage } from '../../src/features/tasks/TasksPage';

vi.mock('../../src/features/tasks/api', () => ({ taskApi: { list: vi.fn() } }));

const task: FollowUpTask = {
  id: 9, applicationId: 7, title: 'Email HR', notes: '', dueAt: '2026-09-20T06:30:00Z',
  status: 'TODO', createdAt: '2026-09-18T10:00:00Z', updatedAt: '2026-09-18T10:00:00Z',
  completedAt: null, version: 0, overdue: true,
};

beforeEach(() => vi.clearAllMocks());

describe('TasksPage', () => {
  it('filters by application, status and overdue flag', async () => {
    const user = userEvent.setup();
    vi.mocked(taskApi.list).mockResolvedValue({ content: [task], page: 0, size: 10, totalElements: 1, totalPages: 1 } as Page<FollowUpTask>);
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(<MantineProvider env="test"><QueryClientProvider client={queryClient}><MemoryRouter initialEntries={['/tasks?applicationId=7']}><TasksPage /></MemoryRouter></QueryClientProvider></MantineProvider>);

    expect(await screen.findByText('Email HR')).toBeInTheDocument();
    expect(taskApi.list).toHaveBeenCalledWith(expect.objectContaining({ applicationId: 7, page: 0 }));
    await user.selectOptions(screen.getByLabelText('状态'), 'TODO');
    await user.click(screen.getByRole('checkbox', { name: '只看逾期' }));
    await waitFor(() => expect(taskApi.list).toHaveBeenCalledWith(expect.objectContaining({ applicationId: 7, status: 'TODO', overdueOnly: true })));
    await user.selectOptions(screen.getByLabelText('状态'), 'DONE');
    await waitFor(() => expect(taskApi.list).toHaveBeenCalledWith(expect.objectContaining({ applicationId: 7, status: 'DONE', overdueOnly: false })));
    expect(screen.getByRole('link', { name: '查看详情' })).toHaveAttribute('href', '/applications/7/tasks/9');
  });
});
