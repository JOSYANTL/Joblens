import { afterEach, describe, expect, it, vi } from 'vitest';
import { notificationApi } from '../../src/features/notifications/api';
import { resetCsrfToken } from '../../src/shared/api/client';

afterEach(() => {
  vi.unstubAllGlobals();
  resetCsrfToken();
});

describe('notificationApi', () => {
  it('builds paginated unread-only queries', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ content: [], page: 1, size: 20, totalElements: 0, totalPages: 0 }),
    });
    vi.stubGlobal('fetch', fetchMock);

    await notificationApi.list({ page: 1, unreadOnly: true });

    expect(fetchMock.mock.calls[0][0]).toBe('/api/notifications?page=1&size=20&unreadOnly=true');
  });

  it('marks one or all notifications as read and deletes one', async () => {
    document.cookie = 'XSRF-TOKEN=notification-csrf; path=/';
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ id: 9, readAt: '2026-09-22T10:00:00Z' }) })
      .mockResolvedValueOnce({ ok: true, status: 204 })
      .mockResolvedValueOnce({ ok: true, status: 204 });
    vi.stubGlobal('fetch', fetchMock);

    await notificationApi.markRead(9);
    await notificationApi.markAllRead();
    await notificationApi.remove(9);

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/notifications/9/read', expect.objectContaining({ method: 'PATCH' }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/notifications/read-all', expect.objectContaining({ method: 'PATCH' }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/notifications/9', expect.objectContaining({ method: 'DELETE' }));
    for (const call of fetchMock.mock.calls) {
      expect((call[1].headers as Headers).get('X-XSRF-TOKEN')).toBe('notification-csrf');
    }
  });
});
