import { afterEach, describe, expect, it, vi } from 'vitest';
import { documentApi } from '../../src/features/documents/api';
import { resetCsrfToken } from '../../src/shared/api/client';

afterEach(() => {
  vi.unstubAllGlobals();
  resetCsrfToken();
});

describe('documentApi', () => {
  it('uploads multipart content and deletes documents with CSRF protection', async () => {
    document.cookie = 'XSRF-TOKEN=document-csrf; path=/';
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 201, json: async () => ({ id: 9 }) })
      .mockResolvedValueOnce({ ok: true, status: 204 });
    vi.stubGlobal('fetch', fetchMock);
    const file = new File(['%PDF-1.7'], 'resume.pdf', { type: 'application/pdf' });

    await documentApi.upload(7, 'RESUME', file);
    await documentApi.remove(7, 9);

    const uploadInit = fetchMock.mock.calls[0][1] as RequestInit;
    expect(fetchMock.mock.calls[0][0]).toBe('/api/applications/7/documents');
    expect(uploadInit.method).toBe('POST');
    expect(uploadInit.body).toBeInstanceOf(FormData);
    expect((uploadInit.body as FormData).get('type')).toBe('RESUME');
    expect((uploadInit.body as FormData).get('file')).toBe(file);
    expect((uploadInit.headers as Headers).get('Content-Type')).toBeNull();
    expect((uploadInit.headers as Headers).get('X-XSRF-TOKEN')).toBe('document-csrf');
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/applications/7/documents/9',
      expect.objectContaining({ method: 'DELETE' }));
  });
});
