export async function getJson<T>(path: string, params?: Record<string, string | number | undefined>): Promise<T> {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params ?? {})) {
    if (value !== undefined && value !== '') query.set(key, String(value));
  }
  const url = `${path}${query.size ? `?${query}` : ''}`;
  const response = await fetch(url, { headers: { Accept: 'application/json' } });
  if (!response.ok) {
    let detail = '';
    try {
      const body = await response.json() as { detail?: string; message?: string };
      detail = body.detail ?? body.message ?? '';
    } catch {
      // Some error responses do not contain JSON.
    }
    throw new Error(detail || `请求失败（HTTP ${response.status}）`);
  }
  return response.json() as Promise<T>;
}
