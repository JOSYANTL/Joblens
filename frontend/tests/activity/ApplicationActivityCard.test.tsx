import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApplicationActivityCard } from '../../src/features/activity/ApplicationActivityCard';
import { activityApi } from '../../src/features/activity/api';
import type { ApplicationActivity, Page } from '../../src/shared/api/types';

vi.mock('../../src/features/activity/api', () => ({ activityApi: { list: vi.fn() } }));

describe('ApplicationActivityCard', () => {
  afterEach(() => vi.unstubAllGlobals());
  it('renders activity labels and context', async () => {
    const page: Page<ApplicationActivity> = {
      content: [{ id: 1, type: 'APPLICATION_STATUS_CHANGED', subjectType: 'APPLICATION',
        subjectId: 7, summary: '申请状态从 SAVED 更新为 APPLIED',
        occurredAt: '2030-01-01T00:00:00Z' }],
      page: 0, size: 10, totalElements: 1, totalPages: 1,
    };
    vi.mocked(activityApi.list).mockResolvedValue(page);
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    client.setQueryData(['application-activities', 7, { page: 0, type: null }], page);
    render(<MantineProvider env="test"><QueryClientProvider client={client}>
      <ApplicationActivityCard applicationId={7} enabled />
    </QueryClientProvider></MantineProvider>);

    expect(screen.getByText('申请状态从 SAVED 更新为 APPLIED')).toBeInTheDocument();
    expect(screen.getAllByText('更新申请状态').length).toBeGreaterThan(0);
  });
});
