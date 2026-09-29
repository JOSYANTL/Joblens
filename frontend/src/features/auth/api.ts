import { getJson, resetCsrfToken, sendJson } from '../../shared/api/client';
import type { AuthUser } from '../../shared/api/types';

export const authApi = {
  me: () => getJson<AuthUser>('/api/auth/me'),
  register: async (body: { email: string; password: string; displayName: string }) => {
    const user = await sendJson<AuthUser>('/api/auth/register', 'POST', body);
    resetCsrfToken();
    return user;
  },
  login: async (body: { email: string; password: string }) => {
    const user = await sendJson<AuthUser>('/api/auth/login', 'POST', body);
    resetCsrfToken();
    return user;
  },
  logout: async () => {
    await sendJson<void>('/api/auth/logout', 'POST', undefined);
    resetCsrfToken();
  },
};
