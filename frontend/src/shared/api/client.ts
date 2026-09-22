type ProblemDetail = {
  detail?: string;
  message?: string;
  errors?: Record<string, string>;
};

export class ApiError extends Error {
  constructor(message: string, public readonly status: number, public readonly errors: Record<string, string> = {}) {
    super(message);
    this.name = 'ApiError';
  }
}

type CsrfResponse = { headerName: string; token: string };
let csrf: CsrfResponse | null = null;

function readCsrfCookie(): string | undefined {
  return document.cookie.split('; ')
    .find((cookie) => cookie.startsWith('XSRF-TOKEN='))?.split('=')[1];
}

async function ensureCsrfToken(): Promise<CsrfResponse> {
  if (csrf) return csrf;
  let cookieToken = readCsrfCookie();
  if (cookieToken) {
    csrf = { headerName: 'X-XSRF-TOKEN', token: decodeURIComponent(cookieToken) };
    return csrf;
  }
  const response = await fetch('/api/auth/csrf', { headers: { Accept: 'application/json' } });
  if (!response.ok) throw new ApiError('无法初始化安全会话', response.status);
  const body = await response.json() as CsrfResponse;
  cookieToken = readCsrfCookie();
  if (!cookieToken) throw new ApiError('无法读取安全令牌', 500);
  csrf = { headerName: body.headerName, token: decodeURIComponent(cookieToken) };
  return csrf;
}

export function resetCsrfToken() {
  csrf = null;
  document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/; SameSite=Strict';
}

async function requestJson<T>(url: string, init?: RequestInit): Promise<T> {
  const method = init?.method?.toUpperCase() ?? 'GET';
  const headers = new Headers(init?.headers);
  headers.set('Accept', 'application/json');
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const token = await ensureCsrfToken();
    headers.set(token.headerName, token.token);
  }
  const response = await fetch(url, {
    ...init,
    credentials: 'same-origin',
    headers,
  });
  if (!response.ok) {
    let body: ProblemDetail = {};
    try {
      body = await response.json() as ProblemDetail;
    } catch {
      // Some error responses do not contain JSON.
    }
    const error = new ApiError(body.detail ?? body.message ?? `请求失败（HTTP ${response.status}）`, response.status, body.errors);
    if (response.status === 401 && !url.startsWith('/api/auth/')) {
      window.dispatchEvent(new Event('joblens:unauthorized'));
    }
    throw error;
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export async function getJson<T>(path: string, params?: Record<string, string | number | undefined>): Promise<T> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params ?? {})) {
    if (value !== undefined && value !== '') query.set(key, String(value));
  }
  const url = `${path}${query.size ? `?${query}` : ''}`;
  return requestJson<T>(url);
}

export function sendJson<T>(path: string, method: 'POST' | 'PUT' | 'PATCH', body: unknown): Promise<T> {
  return requestJson<T>(path, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
}

export function deleteResource(path: string): Promise<void> {
  return requestJson<void>(path, { method: 'DELETE' });
}
