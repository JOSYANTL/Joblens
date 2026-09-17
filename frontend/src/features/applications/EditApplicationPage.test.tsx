import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError } from '../../shared/api/client';
import type { JobApplication } from '../../shared/api/types';
import { applicationApi } from './api';
import { EditApplicationPage } from './EditApplicationPage';

vi.mock('./api', () => ({ applicationApi: { get: vi.fn(), update: vi.fn() } }));

const original: JobApplication = {
  id: 7, company: 'Original Company', position: 'Engineer', description: 'Java',
  status: 'SAVED', createdAt: '2026-09-17T10:00:00', updatedAt: '2026-09-17T10:00:00', version: 1,
};

beforeEach(() => vi.clearAllMocks());

function renderPage() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<MantineProvider env="test"><QueryClientProvider client={queryClient}><MemoryRouter initialEntries={['/applications/7/edit']}><Routes><Route path="/applications/:id/edit" element={<EditApplicationPage />} /><Route path="/applications/:id" element={<div>申请详情已打开</div>} /></Routes></MemoryRouter></QueryClientProvider></MantineProvider>);
}

describe('EditApplicationPage', () => {
  it('reloads the latest version after a conflict before retrying', async () => {
    const user = userEvent.setup();
    vi.mocked(applicationApi.get).mockResolvedValueOnce(original).mockResolvedValue({ ...original, company: 'Latest Company', version: 2 });
    vi.mocked(applicationApi.update).mockRejectedValueOnce(new ApiError('版本已过期', 409)).mockResolvedValueOnce({ ...original, company: 'Updated Company', version: 3 });
    renderPage();

    const company = await screen.findByRole('textbox', { name: /公司/ });
    await user.clear(company);
    await user.type(company, 'My Change');
    await user.click(screen.getByRole('button', { name: '保存修改' }));
    expect(await screen.findByRole('button', { name: '加载最新版本' })).toBeInTheDocument();
    expect(applicationApi.update).toHaveBeenCalledWith(7, { company: 'My Change', position: 'Engineer', description: 'Java', version: 1 });

    await user.click(screen.getByRole('button', { name: '加载最新版本' }));
    expect(await screen.findByDisplayValue('Latest Company')).toBeInTheDocument();
    await user.clear(screen.getByRole('textbox', { name: /公司/ }));
    await user.type(screen.getByRole('textbox', { name: /公司/ }), 'Updated Company');
    await user.click(screen.getByRole('button', { name: '保存修改' }));

    await waitFor(() => expect(applicationApi.update).toHaveBeenLastCalledWith(7, { company: 'Updated Company', position: 'Engineer', description: 'Java', version: 2 }));
    expect(await screen.findByText('申请详情已打开')).toBeInTheDocument();
  });
});
