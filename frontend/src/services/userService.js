
import { read, write } from '../utils/storage';
import { isEmail, isMobile } from '../utils/validation';

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
    {
      name: 'Demo Admin',
      email: 'admin@farmverse.demo',
      mobile: '9000000001',
      role: 'admin',
      organization: 'FarmVerse Nexus',
      password: 'Admin@123'
    },
    {
      name: 'Demo Field Worker',
      email: 'manager@farmverse.demo',
      mobile: '9000000002',
      role: 'fieldWorker',
      organization: 'Green Valley Co-op',
      designation: 'Field Worker',
      assignedFarm: 'Green Valley Farm',
      password: 'Manager@123'
    },
    {
      name: 'Demo Farmer',
      email: 'farmer@farmverse.demo',
      mobile: '9000000003',
      role: 'farmer',
      organization: 'Sunrise Farm',
      language: 'English',
      password: 'Farmer@123'
    }
  ];

  const createdUsers = await Promise.all(
    seed.map(makeUser)
  );

  saveRaw(createdUsers);
}

// Get all users
export const listUsers = async () =>
  allRaw().map(strip);

// Get a user by ID
export const getUser = async (id) => {
  const user = allRaw().find(
    (item) => item.id === id
  );

  return user ? strip(user) : null;
};

// Validate user details
function check(data, users, ignoreId) {
  if (!data.name?.trim()) {
    throw new Error('Name is required.');
  }

  if (!isEmail(data.email)) {
    throw new Error('Enter a valid email address.');
  }

  if (data.mobile && !isMobile(data.mobile)) {
    throw new Error('Enter a valid mobile number.');
  }

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

// Create a user
export async function createUser(data) {
  const users = allRaw();

  check(data, users);

  if (!data.password || data.password.length < 8) {
    throw new Error(
      'Password must be at least 8 characters.'
    );
  }

  const user = await makeUser({
    ...data,
    role: normalizeRole(data.role),
    email: data.email.trim().toLowerCase(),
    name: data.name.trim()
  });

  saveRaw([...users, user]);

  return strip(user);
}

// Update a user
export async function updateUser(
  id,
  data,
  currentUserId
) {
  const users = allRaw();

  const current = users.find(
    (user) => user.id === id
  );

  if (!current) {
    throw new Error('User not found.');
  }

  const normalizedData = {
    ...data,
    ...(data.role
      ? { role: normalizeRole(data.role) }
      : {})
  };

  check(
    { ...current, ...normalizedData },
    users,
    id
  );

  // Users cannot change their own role
  if (
    id === currentUserId &&
    normalizedData.role &&
    normalizedData.role !== current.role
  ) {
    throw new Error(
      'You cannot change your own role.'
    );
  }

  // Prevent demoting the last Admin
  if (
    current.role === 'admin' &&
    normalizedData.role &&
    normalizedData.role !== 'admin' &&
    users.filter(
      (user) => user.role === 'admin'
    ).length <= 1
  ) {
    throw new Error(
      'The last Admin cannot be demoted.'
    );
  }

  const updated = {
    ...current,
    ...normalizedData,
    email: (
      normalizedData.email ?? current.email
    ).trim().toLowerCase()
  };

  // Never overwrite password hashes through normal editing
  delete updated.password;

  saveRaw(
    users.map((user) =>
      user.id === id ? updated : user
    )
  );

  return strip(updated);
}

// Change account status
export async function setStatus(
  id,
  status,
  currentUserId
) {
  if (id === currentUserId) {
    throw new Error(
      'You cannot change the status of your own account.'
    );
  }

  if (!['active', 'inactive', 'suspended'].includes(status)) {
    throw new Error('Invalid account status.');
  }

  const users = allRaw();

  const current = users.find(
    (user) => user.id === id
  );

  if (!current) {
    throw new Error('User not found.');
  }

  // At least one active Admin must remain
  if (
    current.role === 'admin' &&
    status !== 'active' &&
    users.filter(
      (user) =>
        user.role === 'admin' &&
        user.status === 'active'
    ).length <= 1
  ) {
    throw new Error(
      'At least one active Admin must remain.'
    );
  }

  saveRaw(
    users.map((user) =>
      user.id === id
        ? { ...user, status }
        : user
    )
  );
}

// Delete a user
export async function deleteUser(
  id,
  currentUserId
) {
  if (id === currentUserId) {
    throw new Error(
      'You cannot delete your own account.'
    );
  }

  const users = allRaw();

  const current = users.find(
    (user) => user.id === id
  );

  if (!current) {
    throw new Error('User not found.');
  }

  // Prevent deleting the last Admin
  if (
    current.role === 'admin' &&
    users.filter(
      (user) => user.role === 'admin'
    ).length <= 1
  ) {
    throw new Error(
      'The last Admin cannot be deleted.'
    );
  }

  saveRaw(
    users.filter(
      (user) => user.id !== id
    )
  );
}
