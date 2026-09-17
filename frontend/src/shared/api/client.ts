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

async function requestJson<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...init,
    headers: { Accept: 'application/json', ...init?.headers },
  });
  if (!response.ok) {
    let body: ProblemDetail = {};
    try {
      body = await response.json() as ProblemDetail;
    } catch {
      // Some error responses do not contain JSON.
    }
    throw new ApiError(body.detail ?? body.message ?? `请求失败（HTTP ${response.status}）`, response.status, body.errors);
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
