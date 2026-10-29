
import { read, write, remove } from '../utils/storage';
import { request } from './api';

const SESSION_KEY = 'fvn_session';
const REMEMBER_KEY = 'fvn_remember_email';

// Read the email saved by the Remember Me option.
export const getRememberedEmail = () => read(REMEMBER_KEY, '');

// Store the authenticated user and token together in the browser session.
function saveSession(user, token, remember) {
  const storage = remember ? localStorage : sessionStorage;

  // Normalize role casing because the frontend permission map uses lowercase roles.
  const normalizedUser = {
    ...user,
    role: user.role?.toLowerCase(),
  };

  // The token is needed for future protected backend API requests.
  write(SESSION_KEY, { user: normalizedUser, token }, storage);

  if (remember) {
    write(REMEMBER_KEY, user.email);
  } else {
    remove(REMEMBER_KEY);
  }

  return normalizedUser;
}

// Log in through the Spring Boot backend.
export async function login({ email, password, remember }) {
  const response = await request('/api/auth/login', {
    method: 'POST',
    body: {
      email: email.trim().toLowerCase(),
      password,
    },
  });

  // Do not create a local session unless the backend returns a token.
  if (!response.token) {
    throw new Error('Login response did not contain an authentication token.');
  }

  return saveSession(response, response.token, remember);
}

// Restore the user when the application reloads.
export function getSession() {
  const session =
    read(SESSION_KEY, null, sessionStorage) ||
    read(SESSION_KEY, null, localStorage);

  return session?.user || null;
}

// Retrieve the token for authenticated API calls.
export function getToken() {
  const session =
    read(SESSION_KEY, null, sessionStorage) ||
    read(SESSION_KEY, null, localStorage);

  return session?.token || null;
}

/**
 * Keep the saved browser session synchronized with the latest backend profile.
 */
export function updateSessionUser(profile) {
  for (const storage of [sessionStorage, localStorage]) {
    const session = read(SESSION_KEY, null, storage);

    if (session?.token) {
      write(
        SESSION_KEY,
        {
          ...session,
          user: {
            ...session.user,
            ...profile,
            role: profile.role?.toLowerCase() ?? session.user?.role,
          },
        },
        storage
      );
    }
  }
}


// End the backend session, then clear the local session even if the request fails.
export async function logout() {
  const token = getToken();

  try {
    if (token) {
      await request('/api/auth/logout', {
        method: 'POST',
        token,
      });
    }
  } finally {
    // Remove both copies so a remembered session cannot remain active locally.
    remove(SESSION_KEY, sessionStorage);
    remove(SESSION_KEY, localStorage);
  }
}

// Register a new account through the backend.
export async function register(role, data) {
  const normalizedRole = role.toUpperCase();

  // Public registration must never create an Admin account.
  if (normalizedRole === 'ADMIN') {
    throw new Error(
      'Admin accounts must be created through an authorized process.'
    );
  }

  return request('/api/auth/register', {
    method: 'POST',
    body: {
      name: data.name?.trim(),
      email: data.email?.trim().toLowerCase(),
      password: data.password,
      role: normalizedRole,
      mobile: data.mobile || null,
      language: data.language || 'English',
    },
  });
}

// Password reset is not implemented by the backend yet.
export async function requestPasswordReset(email) {
  if (!email?.trim()) {
    throw new Error('Email is required.');
  }

  throw new Error('Password reset is not connected to the backend yet.');
}


