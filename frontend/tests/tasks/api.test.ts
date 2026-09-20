import { afterEach, describe, expect, it, vi } from 'vitest';
import { taskApi } from '../../src/features/tasks/api';

afterEach(() => vi.unstubAllGlobals());

const details = { title: 'Follow up with HR', notes: 'Send thank-you email', dueAt: '2026-09-20T06:30:00.000Z' };

describe('taskApi', () => {
  it('uses the backend filter names for list queries', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => ({ content: [] }) });
    vi.stubGlobal('fetch', fetchMock);

    await taskApi.list({ page: 1, applicationId: 7, status: 'TODO', overdueOnly: true });

    expect(fetchMock.mock.calls[0][0]).toBe('/api/tasks?size=10&page=1&applicationId=7&status=TODO&overdueOnly=true');
  });

  it('sends create, update and status payloads with current versions', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => ({ id: 9 }) });
    vi.stubGlobal('fetch', fetchMock);

    await taskApi.create(7, details);
    await taskApi.update(7, 9, details, 3);
    await taskApi.changeStatus(7, 9, 'DONE', 4);

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/applications/7/tasks', expect.objectContaining({ method: 'POST', body: JSON.stringify(details) }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/applications/7/tasks/9', expect.objectContaining({ method: 'PUT', body: JSON.stringify({ ...details, version: 3 }) }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/applications/7/tasks/9/status', expect.objectContaining({ method: 'PATCH', body: JSON.stringify({ status: 'DONE', version: 4 }) }));
  });

  it('includes the version when deleting', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 204 });
    vi.stubGlobal('fetch', fetchMock);

    await taskApi.remove(7, 9, 5);

    expect(fetchMock).toHaveBeenCalledWith('/api/applications/7/tasks/9?version=5', expect.objectContaining({ method: 'DELETE' }));
  });
});
