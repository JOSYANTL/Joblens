import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Interview } from '../../src/shared/api/types';
import { interviewApi } from '../../src/features/interviews/api';
import { InterviewDetailPage } from '../../src/features/interviews/InterviewDetailPage';

vi.mock('../../src/features/interviews/api', () => ({ interviewApi: { get: vi.fn(), changeStatus: vi.fn(), saveFeedback: vi.fn() } }));

const scheduled: Interview = {
  id: 9, applicationId: 7, round: 1, type: 'VIDEO', startsAt: '2020-01-01T09:00:00Z',
  endsAt: '2020-01-01T10:00:00Z', durationMinutes: 60, contact: 'HR', meetingUrl: '',
  location: '', status: 'SCHEDULED', feedback: { questions: '', summary: '', nextSteps: '' },
  createdAt: '2020-01-01T00:00:00Z', updatedAt: '2020-01-01T00:00:00Z', completedAt: null, version: 1,
};

beforeEach(() => vi.clearAllMocks());

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<MantineProvider env="test"><QueryClientProvider client={queryClient}><MemoryRouter initialEntries={['/applications/7/interviews/9']}><Routes><Route path="/applications/:applicationId/interviews/:interviewId" element={<InterviewDetailPage />} /></Routes></MemoryRouter></QueryClientProvider></MantineProvider>);
}

describe('InterviewDetailPage', () => {
  it('completes a started interview and saves feedback with the new version', async () => {
    const user = userEvent.setup();
    const completed: Interview = { ...scheduled, status: 'COMPLETED', completedAt: '2020-01-01T10:00:00Z', version: 2 };
    vi.mocked(interviewApi.get).mockResolvedValue(scheduled);
    vi.mocked(interviewApi.changeStatus).mockResolvedValue(completed);
    vi.mocked(interviewApi.saveFeedback).mockResolvedValue({ ...completed, feedback: { questions: '', summary: 'Good interview', nextSteps: '' }, version: 3 });
    renderPage();

    await user.click(await screen.findByRole('button', { name: '标记完成' }));
    await waitFor(() => expect(interviewApi.changeStatus).toHaveBeenCalledWith(7, 9, 'COMPLETED', 1));
    const summary = await screen.findByRole('textbox', { name: '总结' });
    await user.type(summary, 'Good interview');
    await user.click(screen.getByRole('button', { name: '保存反馈' }));

    await waitFor(() => expect(interviewApi.saveFeedback).toHaveBeenCalledWith(7, 9, { questions: '', summary: 'Good interview', nextSteps: '' }, 2));
    expect(await screen.findByText('面试反馈已保存。')).toBeInTheDocument();
  });

  it('does not allow completing an interview before it starts', async () => {
    vi.mocked(interviewApi.get).mockResolvedValue({ ...scheduled, startsAt: '2099-01-01T09:00:00Z' });
    renderPage();

    expect(await screen.findByRole('button', { name: '标记完成' })).toBeDisabled();
    expect(screen.getByText('面试开始时间尚未到，暂不能标记完成。')).toBeInTheDocument();
  });
});
