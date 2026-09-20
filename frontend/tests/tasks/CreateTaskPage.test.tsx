import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { FollowUpTask, JobApplication } from '../../src/shared/api/types';
import { applicationApi } from '../../src/features/applications/api';
import { taskApi } from '../../src/features/tasks/api';
import { CreateTaskPage } from '../../src/features/tasks/CreateTaskPage';
import { toIsoInstant } from '../../src/features/interviews/time';

vi.mock('../../src/features/applications/api', () => ({ applicationApi: { get: vi.fn() } }));
vi.mock('../../src/features/tasks/api', () => ({ taskApi: { create: vi.fn() } }));

const application: JobApplication = {
  id: 7, company: 'Example Company', position: 'Engineer', description: 'Java', status: 'SAVED',
  createdAt: '2026-09-18T10:00:00Z', updatedAt: '2026-09-18T10:00:00Z', version: 0,
};
const task: FollowUpTask = {
  id: 9, applicationId: 7, title: 'Email HR', notes: '', dueAt: '2026-09-20T06:30:00Z',
  status: 'TODO', createdAt: '2026-09-18T10:00:00Z', updatedAt: '2026-09-18T10:00:00Z',
  completedAt: null, version: 0, overdue: false,
};

beforeEach(() => vi.clearAllMocks());

describe('CreateTaskPage', () => {
  it('creates a task from the application and opens its detail', async () => {
    vi.mocked(applicationApi.get).mockResolvedValue(application);
    vi.mocked(taskApi.create).mockResolvedValue(task);
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(<MantineProvider env="test"><QueryClientProvider client={queryClient}><MemoryRouter initialEntries={['/applications/7/tasks/new']}><Routes><Route path="/applications/:applicationId/tasks/new" element={<CreateTaskPage />} /><Route path="/applications/:applicationId/tasks/:taskId" element={<div>任务详情已打开</div>} /></Routes></MemoryRouter></QueryClientProvider></MantineProvider>);

    await screen.findByText('Example Company · Engineer');
    fireEvent.change(screen.getByRole('textbox', { name: '任务标题' }), { target: { value: 'Email HR' } });
    fireEvent.change(screen.getByLabelText(/截止时间/), { target: { value: '2026-09-20T14:30' } });
    fireEvent.click(screen.getByRole('button', { name: '创建任务' }));

    expect(await screen.findByText('任务详情已打开')).toBeInTheDocument();
    expect(taskApi.create).toHaveBeenCalledWith(7, { title: 'Email HR', notes: '', dueAt: toIsoInstant('2026-09-20T14:30') });
  });
});
