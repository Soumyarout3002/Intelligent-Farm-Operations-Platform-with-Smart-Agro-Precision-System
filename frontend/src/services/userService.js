// DEMO user store (LocalStorage). Backend: GET/POST /users, GET/PUT/DELETE /users/:id, PATCH /users/:id/status
import { read, write } from '../utils/storage';
import { isEmail, isMobile } from '../utils/validation';
import { request } from './api';
import { getToken } from './authService';

const KEY = 'fvn_users';
export const strip = ({ passwordHash, salt, ...u }) => u;

export async function hashPassword(password, salt) {
  // Demo only: salted SHA-256 via Web Crypto. Real systems must hash server-side (bcrypt/argon2).
  const data = new TextEncoder().encode(`${salt}:${password}`);
  const buf = await crypto.subtle.digest('SHA-256', data);
  return Array.from(new Uint8Array(buf)).map((b) => b.toString(16).padStart(2, '0')).join('');
}
const newSalt = () => Array.from(crypto.getRandomValues(new Uint8Array(8))).map((b) => b.toString(16).padStart(2, '0')).join('');
const newId = () => `U-${Date.now().toString(36)}${Math.floor(Math.random() * 1000)}`;

export const allRaw = () => { const u = read(KEY, []); return Array.isArray(u) ? u : []; };
export const saveRaw = (u) => write(KEY, u);

export async function makeUser({ password, ...rest }) {
  const salt = newSalt();
  return { id: newId(), status: 'active', createdAt: new Date().toISOString(), ...rest, salt, passwordHash: await hashPassword(password, salt) };
}

export async function seedUsers() {
  if (allRaw().length) return;
  const seed = [
    { name: 'Demo Admin', email: 'admin@farmverse.demo', mobile: '9000000001', role: 'admin', organization: 'FarmVerse Nexus', password: 'Admin@123' },
    { name: 'Demo Manager', email: 'manager@farmverse.demo', mobile: '9000000002', role: 'manager', organization: 'Green Valley Co-op', designation: 'Farm Manager', assignedFarm: 'Green Valley Farm', password: 'Manager@123' },
    { name: 'Demo Farmer', email: 'farmer@farmverse.demo', mobile: '9000000003', role: 'farmer', organization: 'Sunrise Farm', language: 'English', password: 'Farmer@123' },
  ];
  saveRaw(await Promise.all(seed.map(makeUser)));
}

export async function listUsers() {
  const token = getToken();

  if (!token) {
    throw new Error('Your session has expired. Please log in again.');
  }

  const users = await request('/api/admin/users', { token });

  return users.map((user) => ({
    ...user,
    role: (user.role || '').toLowerCase().replace(/-/g, '_'),
    status: (user.status || 'ACTIVE').toLowerCase(),
    mobile: user.mobile || '',
    language: user.language || 'English',
    organization: user.organization || '',
    createdAt: user.createdAt || '',
  }));
}
export const getUser = async (id) => { const u = allRaw().find((x) => x.id === id); return u ? strip(u) : null; };

function check(d, users, ignoreId) {
  if (!d.name?.trim()) throw new Error('Name is required.');
  if (!isEmail(d.email)) throw new Error('Enter a valid email address.');
  if (d.mobile && !isMobile(d.mobile)) throw new Error('Enter a valid mobile number.');
  if (users.some((u) => u.id !== ignoreId && u.email.toLowerCase() === d.email.trim().toLowerCase()))
    throw new Error('A user with this email already exists.');
}

export async function createUser(data) {
  if (!data.name?.trim()) {
    throw new Error('Name is required.');
  }

  if (!data.email?.trim()) {
    throw new Error('Email is required.');
  }

  if (!data.password || data.password.length < 8) {
    throw new Error('Password must be at least 8 characters.');
  }

  const token = getToken();

  if (!token) {
    throw new Error('Your session has expired. Please log in again.');
  }

  const user = await request('/api/admin/users', {
    method: 'POST',
    token,
    body: {
      name: data.name.trim(),
      email: data.email.trim().toLowerCase(),
      password: data.password,
      role: data.role.toUpperCase(),
      mobile: data.mobile?.trim() || null,
      language: data.language || 'English',
    },
  });

  return {
    ...user,
    role: (user.role || '').toLowerCase().replace(/-/g, '_'),
    status: (user.status || 'ACTIVE').toLowerCase(),
    mobile: user.mobile || '',
    language: user.language || 'English',
    organization: user.organization || '',
    createdAt: user.createdAt || '',
  };
}


/**
 * Update user details through Spring Boot and MySQL.
 * Backend endpoint: PUT /api/admin/users/{id}
 */
export async function updateUser(id, data, currentUserId) {
  if (String(id) === String(currentUserId) && data.role) {
    throw new Error('You cannot change your own role.');
  }

  const token = getToken();

  if (!token) {
    throw new Error('Your session has expired. Please log in again.');
  }

  const updatedUser = await request(`/api/admin/users/${id}`, {
    method: 'PUT',
    token,
    body: {
      name: data.name?.trim(),
      email: data.email?.trim().toLowerCase(),
      mobile: data.mobile?.trim() || '',
      language: data.language?.trim() || 'English',
    },
  });

  return {
    ...updatedUser,
    role: (updatedUser.role || '').toLowerCase().replace(/-/g, '_'),
    status: (updatedUser.status || 'ACTIVE').toLowerCase(),
    mobile: updatedUser.mobile || '',
    language: updatedUser.language || 'English',
    organization: updatedUser.organization || '',
    createdAt: updatedUser.createdAt || '',
  };
}

/**
 * Activate or deactivate a user through Spring Boot.
 * Backend endpoint: PATCH /api/admin/users/{id}/status
 */
export async function setStatus(id, status, currentUserId) {
  if (String(id) === String(currentUserId)) {
    throw new Error('You cannot change your own account status.');
  }

  const token = getToken();

  if (!token) {
    throw new Error('Your session has expired. Please log in again.');
  }

  await request(`/api/admin/users/${id}/status`, {
    method: 'PATCH',
    token,
    body: {
      status: status.toUpperCase(),
    },
  });
}

/**
 * Delete a user through Spring Boot and MySQL.
 * Backend endpoint: DELETE /api/admin/users/{id}
 */
export async function deleteUser(id, currentUserId) {
  if (String(id) === String(currentUserId)) {
    throw new Error('You cannot delete your own account.');
  }

  const token = getToken();

  if (!token) {
    throw new Error('Your session has expired. Please log in again.');
  }

  await request(`/api/admin/users/${id}`, {
    method: 'DELETE',
    token,
  });
}

  
/**
 * Update the currently logged-in user's profile through Spring Boot.
 * The backend verifies the session token and saves changes in MySQL.
 */
export async function updateMyProfile({ name, mobile }) {
  const token = getToken();

  if (!token) {
    throw new Error('Your session has expired. Please log in again.');
  }

  // Call the authenticated backend profile endpoint.
  const updatedProfile = await request('/api/profile', {
    method: 'PUT',
    token,
    body: {
      name: name.trim(),
      mobile: mobile?.trim() || ''
    }
  });

  return updatedProfile;
}

// Fetch the latest profile from the backend.
export async function getMyProfile() {
  const token = getToken();

  if (!token) {
    throw new Error('Your session has expired. Please log in again.');
  }

  return request('/api/profile', {
    method: 'GET',
    token
  });
}


