// DEMO user store (LocalStorage). Backend: GET/POST /users, GET/PUT/DELETE /users/:id, PATCH /users/:id/status
import { read, write } from '../utils/storage';
import { isEmail, isMobile } from '../utils/validation';

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

export const listUsers = async () => allRaw().map(strip);
export const getUser = async (id) => { const u = allRaw().find((x) => x.id === id); return u ? strip(u) : null; };

function check(d, users, ignoreId) {
  if (!d.name?.trim()) throw new Error('Name is required.');
  if (!isEmail(d.email)) throw new Error('Enter a valid email address.');
  if (d.mobile && !isMobile(d.mobile)) throw new Error('Enter a valid mobile number.');
  if (users.some((u) => u.id !== ignoreId && u.email.toLowerCase() === d.email.trim().toLowerCase()))
    throw new Error('A user with this email already exists.');
}

export async function createUser(d) {
  const users = allRaw();
  check(d, users);
  if (!d.password || d.password.length < 8) throw new Error('Password must be at least 8 characters.');
  const u = await makeUser({ ...d, email: d.email.trim().toLowerCase(), name: d.name.trim() });
  saveRaw([...users, u]);
  return strip(u);
}

export async function updateUser(id, d, currentUserId) {
  const users = allRaw();
  const cur = users.find((u) => u.id === id);
  if (!cur) throw new Error('User not found.');
  check({ ...cur, ...d }, users, id);
  if (id === currentUserId && d.role && d.role !== cur.role) throw new Error('You cannot change your own role.');
  if (cur.role === 'admin' && d.role && d.role !== 'admin' && users.filter((u) => u.role === 'admin').length <= 1)
    throw new Error('The last Admin cannot be demoted.');
  const upd = { ...cur, ...d, email: (d.email ?? cur.email).trim().toLowerCase() };
  delete upd.password;
  saveRaw(users.map((u) => (u.id === id ? upd : u)));
  return strip(upd);
}

export async function setStatus(id, status, currentUserId) {
  if (id === currentUserId) throw new Error('You cannot change the status of your own account.');
  const users = allRaw();
  const cur = users.find((u) => u.id === id);
  if (!cur) throw new Error('User not found.');
  if (cur.role === 'admin' && status !== 'active' && users.filter((u) => u.role === 'admin' && u.status === 'active').length <= 1)
    throw new Error('At least one active Admin must remain.');
  saveRaw(users.map((u) => (u.id === id ? { ...u, status } : u)));
}

export async function deleteUser(id, currentUserId) {
  if (id === currentUserId) throw new Error('You cannot delete your own account.');
  const users = allRaw();
  const cur = users.find((u) => u.id === id);
  if (!cur) throw new Error('User not found.');
  if (cur.role === 'admin' && users.filter((u) => u.role === 'admin').length <= 1) throw new Error('The last Admin cannot be deleted.');
  saveRaw(users.filter((u) => u.id !== id));
}
