import { request } from './api';
import { getToken } from './authService';

const authToken = () => {
  const token = getToken();
  if (!token) throw new Error('Your session has expired. Please log in again.');
  return token;
};

const normalize = (user) => ({
  ...user,
  role: (user.role || '').toLowerCase().replace(/-/g, '_'),
  status: (user.status || 'ACTIVE').toLowerCase(),
  mobile: user.mobile || '',
  language: user.language || 'English',
  organization: user.organization || '',
  createdAt: user.createdAt || '',
});

export async function listUsers() {
  const users = await request('/api/admin/users', { token: authToken() });
  return users.map(normalize);
}

export async function createUser(data) {
  if (!data.name?.trim()) throw new Error('Name is required.');
  if (!data.email?.trim()) throw new Error('Email is required.');
  if (!data.password || data.password.length < 8) throw new Error('Password must be at least 8 characters.');
  const user = await request('/api/admin/users', {
    method: 'POST', token: authToken(),
    body: {
      name: data.name.trim(), email: data.email.trim().toLowerCase(),
      password: data.password, role: data.role.toUpperCase(),
      mobile: data.mobile?.trim() || null, language: data.language || 'English',
    },
  });
  return normalize(user);
}

export async function updateUser(id, data, currentUserId) {
  if (String(id) === String(currentUserId) && data.role) throw new Error('You cannot change your own role.');
  const updated = await request(`/api/admin/users/${id}`, {
    method: 'PUT', token: authToken(),
    body: {
      name: data.name?.trim(), email: data.email?.trim().toLowerCase(),
      mobile: data.mobile?.trim() || '', language: data.language?.trim() || 'English',
    },
  });
  return normalize(updated);
}

export async function setStatus(id, status, currentUserId) {
  if (String(id) === String(currentUserId)) throw new Error('You cannot change your own account status.');
  await request(`/api/admin/users/${id}/status`, {
    method: 'PATCH', token: authToken(), body: { status: status.toUpperCase() },
  });
}

export async function deleteUser(id, currentUserId) {
  if (String(id) === String(currentUserId)) throw new Error('You cannot delete your own account.');
  await request(`/api/admin/users/${id}`, { method: 'DELETE', token: authToken() });
}

export async function updateMyProfile({ name, mobile, language }) {
  return request('/api/profile', {
    method: 'PUT', token: authToken(),
    body: { name: name.trim(), mobile: mobile?.trim() || '', language: language?.trim() || 'English' },
  });
}

export async function getMyProfile() {
  return request('/api/profile', { token: authToken() });
}

export async function createInvitation(email, role) {
  return request('/api/admin/invitations', {
    method: 'POST', token: authToken(),
    body: { email: email.trim().toLowerCase(), role: role.toUpperCase() },
  });
}
