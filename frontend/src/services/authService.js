
import { read, write, remove } from '../utils/storage';
import { request } from './api';

const SESSION_KEY = 'fvn_session';
const REMEMBER_KEY = 'fvn_remember_email';

// Read the email saved by the Remember Me option.
export const getRememberedEmail = () => read(REMEMBER_KEY, '');

// Store the authenticated user and token together.
function saveSession(user, token, remember) {
  const storage = remember ? localStorage : sessionStorage;

  const normalizedUser = {
    ...user,
    role: user.role?.toLowerCase(),
  };

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

// Synchronize the saved browser session with the latest profile.
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

// End the backend session and clear local session data.
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
    remove(SESSION_KEY, sessionStorage);
    remove(SESSION_KEY, localStorage);
  }
}

// Public registration is restricted to Farmer accounts.
export async function register(role, data) {
  const normalizedRole = role.toUpperCase();

  if (normalizedRole !== 'FARMER') {
    throw new Error(
      'Only Farmer accounts can register publicly. Admin and Field Worker accounts require an invitation.'
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

// Request a password reset email.
export async function requestPasswordReset(email) {
  if (!email?.trim()) {
    throw new Error('Email is required.');
  }

  const endpoint = import.meta.env.DEV
    ? '/api/dev/password-reset/link'
    : '/api/auth/forgot-password';

  return request(endpoint, {
    method: 'POST',
    body: {
      email: email.trim().toLowerCase(),
    },
  });
}

// Reset the password using the reset token.
export async function resetPassword(token, newPassword) {
  return request('/api/auth/reset-password', {
    method: 'POST',
    body: {
      token,
      newPassword,
    },
  });
}

// Validate an invitation token received through the invitation email.
export async function validateInvitation(token) {
  if (!token) {
    throw new Error('Invitation token is missing.');
  }

  return request(
    `/api/invitations/validate?token=${encodeURIComponent(token)}`
  );
}

// Accept an invitation and create the account with its assigned role.
export async function acceptInvitation(data) {
  if (!data?.token) {
    throw new Error('Invitation token is missing.');
  }

  return request('/api/invitations/accept', {
    method: 'POST',
    body: {
      token: data.token,
      name: data.name?.trim(),
      password: data.password,
    },
  });
}
