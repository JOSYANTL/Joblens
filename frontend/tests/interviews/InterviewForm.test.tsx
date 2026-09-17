import { MantineProvider } from '@mantine/core';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import { InterviewForm, emptyInterviewForm, validateInterview } from '../../src/features/interviews/InterviewForm';
import { toIsoInstant, toLocalDateTimeInput } from '../../src/features/interviews/time';

describe('interview date conversion and validation', () => {
  it('round-trips a local date-time and rejects impossible dates', () => {
    const local = '2026-09-18T14:30';
    const iso = toIsoInstant(local);
    expect(iso).not.toBeNull();
    expect(toLocalDateTimeInput(iso!)).toBe(local);
    expect(toIsoInstant('2026-02-30T14:30')).toBeNull();
  });

  it('validates required fields, numeric ranges and meeting links', () => {
    expect(validateInterview({ ...emptyInterviewForm, round: '0', durationMinutes: '481', startsAt: '', meetingUrl: 'ftp://example.com' })).toMatchObject({
      round: expect.any(String), durationMinutes: expect.any(String), startsAt: expect.any(String), meetingUrl: expect.any(String),
    });
    expect(validateInterview({ ...emptyInterviewForm, startsAt: '2026-09-18T14:30', meetingUrl: 'https://example.com/meet' })).toEqual({});
  });

  it('submits an ISO instant and trimmed optional fields', async () => {
    const onSubmit = vi.fn();
    render(<MantineProvider env="test"><MemoryRouter><InterviewForm initialValues={{ ...emptyInterviewForm, startsAt: '2026-09-18T14:30' }} submitLabel="保存面试" cancelTo="/applications/7" pending={false} error={null} onSubmit={onSubmit} /></MemoryRouter></MantineProvider>);

    fireEvent.change(screen.getByRole('textbox', { name: '联系人' }), { target: { value: '  HR  ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存面试' }));

    expect(onSubmit).toHaveBeenCalledWith({
      round: 1, type: 'VIDEO', startsAt: toIsoInstant('2026-09-18T14:30'), durationMinutes: 60,
      contact: 'HR', meetingUrl: '', location: '',
    });
  });
});
