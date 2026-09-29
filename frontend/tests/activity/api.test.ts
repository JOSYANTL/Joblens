import { afterEach, describe, expect, it, vi } from 'vitest';
import { activityApi } from '../../src/features/activity/api';
import { resetCsrfToken } from '../../src/shared/api/client';

afterEach(() => {
  vi.unstubAllGlobals();
  resetCsrfToken();
});

describe('activityApi', () => {
  it('loads the recent activity feed with the requested size', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => [] });
    vi.stubGlobal('fetch', fetchMock);

    await activityApi.recent(8);

    expect(fetchMock).toHaveBeenCalledWith('/api/activities/recent?size=8', expect.anything());
  });

  it('loads a filtered page and performs versioned note operations', async () => {
    document.cookie = 'XSRF-TOKEN=activity-csrf; path=/';
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ content: [] }) })
      .mockResolvedValueOnce({ ok: true, status: 201, json: async () => ({ id: 4, version: 0 }) })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ id: 4, version: 1 }) })
      .mockResolvedValueOnce({ ok: true, status: 204 });
    vi.stubGlobal('fetch', fetchMock);

    await activityApi.list(7, { page: 1, size: 10, type: 'NOTE_CREATED' });
    await activityApi.createNote(7, 'Call after 10 AM');
    await activityApi.updateNote(7, 4, 'Call after 11 AM', 0);
    await activityApi.deleteNote(7, 4, 1);

    expect(fetchMock.mock.calls[0][0]).toBe('/api/applications/7/activities?page=1&size=10&type=NOTE_CREATED');
    expect(fetchMock.mock.calls[1][0]).toBe('/api/applications/7/notes');
    expect(fetchMock.mock.calls[1][1]).toEqual(expect.objectContaining({ method: 'POST' }));
    expect(fetchMock.mock.calls[2][1]).toEqual(expect.objectContaining({
      method: 'PUT', body: JSON.stringify({ content: 'Call after 11 AM', version: 0 }),
    }));
    expect(fetchMock.mock.calls[3][0]).toBe('/api/applications/7/notes/4?version=1');
    expect((fetchMock.mock.calls[3][1].headers as Headers).get('X-XSRF-TOKEN')).toBe('activity-csrf');
  });
});
