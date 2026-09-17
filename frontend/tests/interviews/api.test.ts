import { afterEach, describe, expect, it, vi } from 'vitest';
import { interviewApi } from '../../src/features/interviews/api';

afterEach(() => vi.unstubAllGlobals());

const details = {
  round: 2, type: 'VIDEO' as const, startsAt: '2026-09-18T06:30:00.000Z',
  durationMinutes: 60, contact: 'HR', meetingUrl: 'https://example.com/meet', location: '',
};

describe('interviewApi', () => {
  it('sends scheduling details as the POST body', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 201, json: async () => ({ id: 9 }) });
    vi.stubGlobal('fetch', fetchMock);

    await interviewApi.schedule(7, details);

    expect(fetchMock).toHaveBeenCalledWith('/api/applications/7/interviews', expect.objectContaining({
      method: 'POST', body: JSON.stringify(details),
    }));
  });

  it('includes the version when rescheduling and changing status', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => ({ id: 9 }) });
    vi.stubGlobal('fetch', fetchMock);

    await interviewApi.reschedule(7, 9, details, 3);
    await interviewApi.changeStatus(7, 9, 'CANCELLED', 4);

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/applications/7/interviews/9', expect.objectContaining({
      method: 'PUT', body: JSON.stringify({ details, version: 3 }),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/applications/7/interviews/9/status', expect.objectContaining({
      method: 'PATCH', body: JSON.stringify({ status: 'CANCELLED', version: 4 }),
    }));
  });

  it('sends feedback fields and the current version', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => ({ id: 9 }) });
    vi.stubGlobal('fetch', fetchMock);

    await interviewApi.saveFeedback(7, 9, { questions: 'Q', summary: 'S', nextSteps: 'N' }, 5);

    expect(fetchMock).toHaveBeenCalledWith('/api/applications/7/interviews/9/feedback', expect.objectContaining({
      method: 'PUT', body: JSON.stringify({ questions: 'Q', summary: 'S', nextSteps: 'N', version: 5 }),
    }));
  });
});
