// Central API config. Demo services in this folder use LocalStorage.
// To integrate the backend: set VITE_API_BASE_URL and replace the demo bodies in
// authService.js / userService.js / farmService.js with calls to request().
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export async function request(path, { method = 'GET', body, token } = {}) {
  if (!API_BASE_URL) throw new Error('VITE_API_BASE_URL is not set. The app is running in demo mode.');
  const res = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || `Request failed (${res.status})`);
  return data;
}
