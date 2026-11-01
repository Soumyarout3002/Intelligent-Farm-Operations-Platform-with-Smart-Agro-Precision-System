
 // Base URL for the Spring Boot backend, configured in frontend/.env.
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export async function request(path, { method = 'GET', body, token } = {}) {
  // Fail early if the backend URL has not been configured.
  if (!API_BASE_URL) {
    throw new Error('VITE_API_BASE_URL is not set. Check frontend/.env.');
  }

  const res = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      // Attach the session token when calling a protected endpoint.
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  // Some responses may have no JSON body, so handle that safely.
  const data = await res.json().catch(() => ({}));

  // Pass the backend's error message to the UI when a request fails.
  if (!res.ok) {
    throw new Error(data.message || `Request failed (${res.status})`);
  }

  return data;
}
