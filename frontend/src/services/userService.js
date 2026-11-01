
import { read, write } from '../utils/storage';
import { isEmail, isMobile } from '../utils/validation';
import { request } from './api';
import { getToken } from './authService';

// DEMO user store using LocalStorage.
// Real applications must enforce authentication and
// authorization through a secure backend.

const KEY = 'fvn_users';

// Convert the old Manager role to Field Worker
const normalizeRole = (role) =>
  role === 'manager' ? 'fieldWorker' : role;

// Remove password information before exposing user data
export const strip = ({ passwordHash, salt, ...user }) => ({
  ...user,
  role: normalizeRole(user.role)
});

// Demo password hashing
export async function hashPassword(password, salt) {
  const data = new TextEncoder().encode(
    `${salt}:${password}`
  );

  const buffer = await crypto.subtle.digest(
    'SHA-256',
    data
  );

  return Array.from(new Uint8Array(buffer))
    .map((byte) => byte.toString(16).padStart(2, '0'))
    .join('');
}

// Generate a random salt
const newSalt = () =>
  Array.from(crypto.getRandomValues(new Uint8Array(8)))
    .map((byte) => byte.toString(16).padStart(2, '0'))
    .join('');

// Generate a unique user ID
const newId = () =>
  `U-${Date.now().toString(36)}${Math.floor(
    Math.random() * 1000
  )}`;

// Read users and automatically migrate old Manager accounts
export const allRaw = () => {
  const stored = read(KEY, []);

  if (!Array.isArray(stored)) {
    return [];
  }

  let changed = false;

  const updatedUsers = stored.map((user) => {
    const updated = { ...user };

    if (updated.role === 'manager') {
      updated.role = 'fieldWorker';
      changed = true;
    }

    // Rename only the original demo account
    if (
      updated.name === 'Demo Manager' &&
      updated.email === 'manager@farmverse.demo'
    ) {
      updated.name = 'Demo Field Worker';
      changed = true;
    }

    return updated;
  });

  if (changed) {
    write(KEY, updatedUsers);
  }

  return updatedUsers;
};

// Save user records
export const saveRaw = (users) => {
  write(
    KEY,
    users.map((user) => ({
      ...user,
      role: normalizeRole(user.role)
    }))
  );
};

// Create a new user
export async function makeUser({ password, ...rest }) {
  const salt = newSalt();

  return {
    id: newId(),
    status: 'active',
    createdAt: new Date().toISOString(),
    ...rest,
    role: normalizeRole(rest.role),
    salt,
    passwordHash: await hashPassword(password, salt)
  };
}

// Seed demo users only when storage is empty
export async function seedUsers() {
  if (allRaw().length) {
    return;
  }

  const seed = [
    { name: 'Demo Admin', email: 'admin@farmverse.demo', mobile: '9000000001', role: 'admin', organization: 'FarmVerse Nexus', password: 'Admin@123' },
    {
  name: 'Demo Field Worker',
  email: 'fieldworker@farmverse.demo',
  mobile: '9000000002',
  role: 'field_worker',
  organization: 'Green Valley Co-op',
  designation: 'Field Worker',
  assignedFarm: 'Green Valley Farm',
  password: 'FieldWorker@123'
},
    { name: 'Demo Farmer', email: 'farmer@farmverse.demo', mobile: '9000000003', role: 'farmer', organization: 'Sunrise Farm', language: 'English', password: 'Farmer@123' },
  ];

  const createdUsers = await Promise.all(
    seed.map(makeUser)
  );

  saveRaw(createdUsers);
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

  if (
    users.some(
      (user) =>
        user.id !== ignoreId &&
        user.email.toLowerCase() ===
          data.email.trim().toLowerCase()
    )
  ) {
    throw new Error(
      'A user with this email already exists.'
    );
  }
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

export async function updateMyProfile({ name, mobile, language }) {
  const token = getToken();

  if (!token) {
    throw new Error('Your session has expired. Please log in again.');
  }

  const updatedProfile = await request('/api/profile', {
    method: 'PUT',
    token,
    body: {
      name: name.trim(),
      mobile: mobile?.trim() || '',
      language: language?.trim() || 'English'
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


