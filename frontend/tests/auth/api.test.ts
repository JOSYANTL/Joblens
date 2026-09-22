import { afterEach, describe, expect, it, vi } from 'vitest';
import { authApi } from '../../src/features/auth/api';

afterEach(() => {
  vi.unstubAllGlobals();
  document.cookie = 'XSRF-TOKEN=test-csrf-token; path=/';
});

describe('authApi', () => {
  it('registers with CSRF protection and same-origin credentials', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 201,
      json: async () => ({ id: 1, email: 'user@example.com', displayName: 'User' }),
    });
    vi.stubGlobal('fetch', fetchMock);

    await authApi.register({ email: 'user@example.com', password: 'secure-password-123', displayName: 'User' });

    expect(fetchMock).toHaveBeenCalledWith('/api/auth/register', expect.objectContaining({
      method: 'POST',
      credentials: 'same-origin',
      body: JSON.stringify({ email: 'user@example.com', password: 'secure-password-123', displayName: 'User' }),
    }));
    const headers = fetchMock.mock.calls[0][1].headers as Headers;
    expect(headers.get('X-XSRF-TOKEN')).toBe('test-csrf-token');
  });

  it('loads and ends the current session', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ id: 1, email: 'user@example.com', displayName: 'User' }) })
      .mockResolvedValueOnce({ ok: true, status: 204 });
    vi.stubGlobal('fetch', fetchMock);

    await expect(authApi.me()).resolves.toMatchObject({ email: 'user@example.com' });
    await expect(authApi.logout()).resolves.toBeUndefined();

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/auth/me', expect.objectContaining({ credentials: 'same-origin' }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/auth/logout', expect.objectContaining({ method: 'POST', credentials: 'same-origin' }));
  });
});
