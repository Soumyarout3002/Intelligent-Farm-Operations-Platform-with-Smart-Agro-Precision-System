
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || '';

export async function request(
  path,
  { method = 'GET', body, token } = {}
) {
  if (!API_BASE_URL) {
    throw new Error(
      'VITE_API_BASE_URL is not set. Check frontend/.env.'
    );
  }

  const headers = {
    ...(body !== undefined
      ? { 'Content-Type': 'application/json' }
      : {}),
    ...(token
      ? { Authorization: `Bearer ${token}` }
      : {}),
  };

  const res = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined
      ? JSON.stringify(body)
      : undefined,
  });

  const responseText = await res.text();

  let data = {};

  if (responseText) {
    try {
      data = JSON.parse(responseText);
    } catch {
      data = { message: responseText };
    }
  }

  if (!res.ok) {
    throw new Error(
      data.message || `Request failed (${res.status})`
    );
  }

  return data;
}
