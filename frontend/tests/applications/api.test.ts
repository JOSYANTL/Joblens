import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError } from '../../src/shared/api/client';
import { applicationApi } from '../../src/features/applications/api';

afterEach(() => vi.unstubAllGlobals());

describe('applicationApi mutations', () => {
  it('sends the current version when editing an application', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => ({ id: 7, version: 3 }) });
    vi.stubGlobal('fetch', fetchMock);

    await applicationApi.update(7, { company: 'Acme', position: 'Engineer', description: 'Java', version: 2 });

    expect(fetchMock).toHaveBeenCalledWith('/api/applications/7', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({ company: 'Acme', position: 'Engineer', description: 'Java', version: 2 }),
    }));
  });

  it('accepts a no-content delete response', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 204 });
    vi.stubGlobal('fetch', fetchMock);

    await expect(applicationApi.delete(7)).resolves.toBeUndefined();
    expect(fetchMock).toHaveBeenCalledWith('/api/applications/7', expect.objectContaining({ method: 'DELETE' }));
  });

  it('exposes a version conflict for reloading', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false, status: 409, json: async () => ({ detail: 'Stale job application version' }) }));

    await expect(applicationApi.update(7, { company: 'Acme', position: 'Engineer', description: 'Java', version: 1 })).rejects.toMatchObject({
      name: 'ApiError', status: 409, message: 'Stale job application version',
    } satisfies Partial<ApiError>);
  });
});
