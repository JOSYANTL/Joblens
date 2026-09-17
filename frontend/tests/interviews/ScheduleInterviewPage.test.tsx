import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Interview, JobApplication } from '../../src/shared/api/types';
import { applicationApi } from '../../src/features/applications/api';
import { interviewApi } from '../../src/features/interviews/api';
import { ScheduleInterviewPage } from '../../src/features/interviews/ScheduleInterviewPage';
import { toIsoInstant } from '../../src/features/interviews/time';

vi.mock('../../src/features/applications/api', () => ({ applicationApi: { get: vi.fn() } }));
vi.mock('../../src/features/interviews/api', () => ({ interviewApi: { schedule: vi.fn() } }));

const application: JobApplication = {
  id: 7, company: 'Example Company', position: 'Engineer', description: 'Java', status: 'SAVED',
  createdAt: '2026-09-18T10:00:00', updatedAt: '2026-09-18T10:00:00', version: 0,
};

const interview: Interview = {
  id: 9, applicationId: 7, round: 1, type: 'VIDEO', startsAt: '2026-09-18T06:30:00Z',
  endsAt: '2026-09-18T07:30:00Z', durationMinutes: 60, contact: '', meetingUrl: '', location: '',
  status: 'SCHEDULED', feedback: { questions: '', summary: '', nextSteps: '' },
  createdAt: '2026-09-18T05:00:00Z', updatedAt: '2026-09-18T05:00:00Z', completedAt: null, version: 0,
};

beforeEach(() => vi.clearAllMocks());

describe('ScheduleInterviewPage', () => {
  it('schedules from an application and opens the new interview', async () => {
    vi.mocked(applicationApi.get).mockResolvedValue(application);
    vi.mocked(interviewApi.schedule).mockResolvedValue(interview);
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(<MantineProvider env="test"><QueryClientProvider client={queryClient}><MemoryRouter initialEntries={['/applications/7/interviews/new']}><Routes><Route path="/applications/:applicationId/interviews/new" element={<ScheduleInterviewPage />} /><Route path="/applications/:applicationId/interviews/:interviewId" element={<div>面试详情已打开</div>} /></Routes></MemoryRouter></QueryClientProvider></MantineProvider>);

    const start = await screen.findByLabelText(/开始时间/);
    fireEvent.change(start, { target: { value: '2026-09-18T14:30' } });
    fireEvent.click(screen.getByRole('button', { name: '保存面试' }));

    expect(await screen.findByText('面试详情已打开')).toBeInTheDocument();
    expect(interviewApi.schedule).toHaveBeenCalledWith(7, {
      round: 1, type: 'VIDEO', startsAt: toIsoInstant('2026-09-18T14:30'),
      durationMinutes: 60, contact: '', meetingUrl: '', location: '',
    });
  });
});
