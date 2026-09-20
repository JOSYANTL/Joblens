import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError } from '../../src/shared/api/client';
import type { Interview } from '../../src/shared/api/types';
import { interviewApi } from '../../src/features/interviews/api';
import { RescheduleInterviewPage } from '../../src/features/interviews/RescheduleInterviewPage';

vi.mock('../../src/features/interviews/api', () => ({ interviewApi: { get: vi.fn(), reschedule: vi.fn() } }));

const original: Interview = {
  id: 9, applicationId: 7, round: 1, type: 'VIDEO', startsAt: '2026-09-18T06:30:00Z',
  endsAt: '2026-09-18T07:30:00Z', durationMinutes: 60, contact: 'Original HR', meetingUrl: '',
  location: '', status: 'SCHEDULED', feedback: { questions: '', summary: '', nextSteps: '' },
  createdAt: '2026-09-17T10:00:00Z', updatedAt: '2026-09-17T10:00:00Z', completedAt: null, version: 1,
};

beforeEach(() => vi.clearAllMocks());

describe('RescheduleInterviewPage', () => {
  it('reloads a stale interview version before retrying', async () => {
    const user = userEvent.setup();
    vi.mocked(interviewApi.get).mockResolvedValueOnce(original).mockResolvedValue({ ...original, contact: 'Latest HR', version: 2 });
    vi.mocked(interviewApi.reschedule).mockRejectedValueOnce(new ApiError('版本已过期', 409)).mockResolvedValueOnce({ ...original, contact: 'Updated HR', version: 3 });
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(<MantineProvider env="test"><QueryClientProvider client={queryClient}><MemoryRouter initialEntries={['/applications/7/interviews/9/edit']}><Routes><Route path="/applications/:applicationId/interviews/:interviewId/edit" element={<RescheduleInterviewPage />} /><Route path="/applications/:applicationId/interviews/:interviewId" element={<div>面试详情已打开</div>} /></Routes></MemoryRouter></QueryClientProvider></MantineProvider>);

    const contact = await screen.findByRole('textbox', { name: '联系人' });
    await user.clear(contact);
    await user.type(contact, 'My Change');
    await user.click(screen.getByRole('button', { name: '保存修改' }));
    expect(await screen.findByRole('button', { name: '加载最新版本' })).toBeInTheDocument();
    expect(interviewApi.reschedule).toHaveBeenCalledWith(7, 9, expect.objectContaining({ contact: 'My Change' }), 1);

    await user.click(screen.getByRole('button', { name: '加载最新版本' }));
    expect(await screen.findByDisplayValue('Latest HR')).toBeInTheDocument();
    await user.clear(screen.getByRole('textbox', { name: '联系人' }));
    await user.type(screen.getByRole('textbox', { name: '联系人' }), 'Updated HR');
    await user.click(screen.getByRole('button', { name: '保存修改' }));

    await waitFor(() => expect(interviewApi.reschedule).toHaveBeenLastCalledWith(7, 9, expect.objectContaining({ contact: 'Updated HR' }), 2));
    expect(await screen.findByText('面试详情已打开')).toBeInTheDocument();
  });
});
