import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '../../src/features/auth/AuthContext';
import { RegisterPage } from '../../src/features/auth/RegisterPage';

afterEach(() => vi.unstubAllGlobals());

describe('RegisterPage', () => {
  it('rejects mismatched passwords before calling the API', async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn().mockResolvedValue({ ok: false, status: 401, json: async () => ({}) });
    vi.stubGlobal('fetch', fetchMock);
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

    render(
      <MantineProvider env="test">
        <QueryClientProvider client={client}>
          <MemoryRouter initialEntries={['/register']}>
            <AuthProvider><RegisterPage /></AuthProvider>
          </MemoryRouter>
        </QueryClientProvider>
      </MantineProvider>,
    );

    await user.type(screen.getByRole('textbox', { name: '昵称' }), 'Test User');
    await user.type(screen.getByRole('textbox', { name: '邮箱' }), 'test@example.com');
    const passwords = screen.getAllByLabelText(/密码/);
    await user.type(passwords[0], 'secure-password-123');
    await user.type(passwords[1], 'different-password-123');
    await user.click(screen.getByRole('button', { name: '创建账户' }));

    expect(screen.getByText('两次输入的密码不一致')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock).toHaveBeenCalledWith('/api/auth/me', expect.anything());
  });
});
