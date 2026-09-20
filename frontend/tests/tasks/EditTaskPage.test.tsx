import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError } from '../../src/shared/api/client';
import type { FollowUpTask } from '../../src/shared/api/types';
import { taskApi } from '../../src/features/tasks/api';
import { EditTaskPage } from '../../src/features/tasks/EditTaskPage';

vi.mock('../../src/features/tasks/api', () => ({ taskApi: { get: vi.fn(), update: vi.fn() } }));

const original: FollowUpTask = {
  id: 9, applicationId: 7, title: 'Email HR', notes: '', dueAt: '2026-09-20T06:30:00Z',
  status: 'TODO', createdAt: '2026-09-18T10:00:00Z', updatedAt: '2026-09-18T10:00:00Z',
  completedAt: null, version: 1, overdue: false,
};

beforeEach(() => vi.clearAllMocks());

describe('EditTaskPage', () => {
  it('reloads latest data after a version conflict before retrying', async () => {
    const user = userEvent.setup();
    vi.mocked(taskApi.get).mockResolvedValueOnce(original).mockResolvedValue({ ...original, title: 'Latest title', version: 2 });
    vi.mocked(taskApi.update).mockRejectedValueOnce(new ApiError('版本已过期', 409)).mockResolvedValueOnce({ ...original, title: 'Updated title', version: 3 });
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(<MantineProvider env="test"><QueryClientProvider client={queryClient}><MemoryRouter initialEntries={['/applications/7/tasks/9/edit']}><Routes><Route path="/applications/:applicationId/tasks/:taskId/edit" element={<EditTaskPage />} /><Route path="/applications/:applicationId/tasks/:taskId" element={<div>任务详情已打开</div>} /></Routes></MemoryRouter></QueryClientProvider></MantineProvider>);

    const title = await screen.findByRole('textbox', { name: '任务标题' });
    await user.clear(title);
    await user.type(title, 'My Change');
    await user.click(screen.getByRole('button', { name: '保存修改' }));
    expect(await screen.findByRole('button', { name: '加载最新版本' })).toBeInTheDocument();
    expect(taskApi.update).toHaveBeenCalledWith(7, 9, expect.objectContaining({ title: 'My Change' }), 1);

    await user.click(screen.getByRole('button', { name: '加载最新版本' }));
    expect(await screen.findByDisplayValue('Latest title')).toBeInTheDocument();
    await user.clear(screen.getByRole('textbox', { name: '任务标题' }));
    await user.type(screen.getByRole('textbox', { name: '任务标题' }), 'Updated title');
    await user.click(screen.getByRole('button', { name: '保存修改' }));

    await waitFor(() => expect(taskApi.update).toHaveBeenLastCalledWith(7, 9, expect.objectContaining({ title: 'Updated title' }), 2));
    expect(await screen.findByText('任务详情已打开')).toBeInTheDocument();
  });
});
