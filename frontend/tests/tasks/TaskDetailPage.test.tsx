import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError } from '../../src/shared/api/client';
import type { FollowUpTask } from '../../src/shared/api/types';
import { taskApi } from '../../src/features/tasks/api';
import { TaskDetailPage } from '../../src/features/tasks/TaskDetailPage';

vi.mock('../../src/features/tasks/api', () => ({ taskApi: { get: vi.fn(), changeStatus: vi.fn(), remove: vi.fn() } }));

const task: FollowUpTask = {
  id: 9, applicationId: 7, title: 'Email HR', notes: 'Send a thank-you note', dueAt: '2026-09-20T06:30:00Z',
  status: 'TODO', createdAt: '2026-09-18T10:00:00Z', updatedAt: '2026-09-18T10:00:00Z',
  completedAt: null, version: 1, overdue: false,
};

beforeEach(() => vi.clearAllMocks());

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<MantineProvider env="test"><QueryClientProvider client={queryClient}><MemoryRouter initialEntries={['/applications/7/tasks/9']}><Routes><Route path="/applications/:applicationId/tasks/:taskId" element={<TaskDetailPage />} /><Route path="/applications/:id" element={<div>申请详情已打开</div>} /></Routes></MemoryRouter></QueryClientProvider></MantineProvider>);
}

describe('TaskDetailPage', () => {
  it('completes and reopens using the latest version', async () => {
    const user = userEvent.setup();
    vi.mocked(taskApi.get).mockResolvedValue(task);
    vi.mocked(taskApi.changeStatus).mockResolvedValueOnce({ ...task, status: 'DONE', version: 2, completedAt: '2026-09-20T07:00:00Z' }).mockResolvedValueOnce({ ...task, version: 3 });
    renderPage();

    await user.click(await screen.findByRole('button', { name: '标记完成' }));
    await waitFor(() => expect(taskApi.changeStatus).toHaveBeenCalledWith(7, 9, 'DONE', 1));
    await user.click(await screen.findByRole('button', { name: '重新打开' }));
    await waitFor(() => expect(taskApi.changeStatus).toHaveBeenLastCalledWith(7, 9, 'TODO', 2));
  });

  it('reloads a stale version before deleting', async () => {
    const user = userEvent.setup();
    vi.mocked(taskApi.get).mockResolvedValueOnce(task).mockResolvedValue({ ...task, version: 2 });
    vi.mocked(taskApi.remove).mockRejectedValueOnce(new ApiError('版本已过期', 409)).mockResolvedValueOnce(undefined);
    renderPage();

    await user.click(await screen.findByRole('button', { name: '删除任务' }));
    await user.click(screen.getByRole('button', { name: '确认删除' }));
    expect(await screen.findByRole('button', { name: '加载最新数据' })).toBeInTheDocument();
    expect(taskApi.remove).toHaveBeenCalledWith(7, 9, 1);
    await user.click(screen.getByRole('button', { name: '加载最新数据' }));

    await user.click(screen.getByRole('button', { name: '删除任务' }));
    await user.click(screen.getByRole('button', { name: '确认删除' }));
    await waitFor(() => expect(taskApi.remove).toHaveBeenLastCalledWith(7, 9, 2));
    expect(await screen.findByText('申请详情已打开')).toBeInTheDocument();
  });
});
