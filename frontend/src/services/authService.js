// DEMO auth (frontend only). Backend: POST /auth/login, /auth/register, /auth/logout, /auth/forgot-password.
// Real auth must use server-side hashing + secure tokens/cookies.
import { read, write, remove } from '../utils/storage';
import { allRaw, saveRaw, makeUser, hashPassword, strip } from './userService';
import { createFarm } from './farmService';
import { isEmail } from '../utils/validation';

const SESSION_KEY = 'fvn_session';
const REMEMBER_KEY = 'fvn_remember_email';
export const ADMIN_DEMO_CODE = 'FARMVERSE-ADMIN-2026';
const normalizeRole = (role) =>
  role === 'manager' ? 'fieldWorker' : role;

export const getRememberedEmail = () => read(REMEMBER_KEY, '');


export async function login({ email, password, remember }) {
  const user = allRaw().find(
    (u) => u.email === (email || '').trim().toLowerCase()
  );

  const ok =
    user &&
    (await hashPassword(password, user.salt)) === user.passwordHash;

  if (!ok) {
    throw new Error('Invalid email or password.');
  }

  if (user.status !== 'active') {
    throw new Error(
      `Your account is ${user.status}. Contact an Admin.`
    );
  }

  const normalizedRole = normalizeRole(user.role);

  if (normalizedRole !== user.role) {
    const updatedUsers = allRaw().map((u) =>
      u.id === user.id
        ? { ...u, role: normalizedRole }
        : u
    );

    saveRaw(updatedUsers);
  }

  write(
    SESSION_KEY,
    { id: user.id },
    remember ? localStorage : sessionStorage
  );

  if (remember) {
    write(REMEMBER_KEY, user.email);
  } else {
    remove(REMEMBER_KEY);
  }

  return strip({ ...user, role: normalizedRole });
}


export function getSession() {
  const s =
    read(SESSION_KEY, null, sessionStorage) ||
    read(SESSION_KEY, null, localStorage);

  const user = s && allRaw().find((u) => u.id === s.id);

  if (!user || user.status !== 'active') {
    return null;
  }

  return strip({
    ...user,
    role: normalizeRole(user.role)
  });
}


export function logout() {
  remove(SESSION_KEY, sessionStorage);
  remove(SESSION_KEY, localStorage);
}

export async function register(role, d) {
  role = normalizeRole(role);
  const users = allRaw();
  if (users.some((u) => u.email === d.email.trim().toLowerCase())) throw new Error('This email is already registered.');
  if (role === 'admin' && (d.adminCode || '').trim() !== ADMIN_DEMO_CODE) throw new Error('Invalid admin invitation code.');
  // eslint-disable-next-line no-unused-vars
  const { confirmPassword, terms, adminCode, farmName, farmLocation, farmArea, farmUnit, primaryCrop, ...rest } = d;
  const user = await makeUser({ ...rest, name: d.name.trim(), email: d.email.trim().toLowerCase(), role,
    ...(role === 'farmer' ? { organization: farmName } : {}) });
  saveRaw([...users, user]);
  if (role === 'farmer') {
    await createFarm({ name: farmName, location: farmLocation, area: farmArea, unit: farmUnit, soil: 'Loamy',
      crop: primaryCrop, plots: 1, status: 'active', notes: 'Created during registration.' }).catch(() => {});
  }
  return strip(user);
}

export async function requestPasswordReset(email) {
  if (!isEmail(email)) throw new Error('Enter a valid email address.');
  return 'Demo only: no email was sent. Real password reset requires backend integration.';
}
