import { request } from './api';
import { getToken } from './authService';
import { DEFAULT_PREFERENCES, sampleList, sampleSetRead, sampleMarkAll, samplePreferences, sampleSavePreferences } from './sampleData/notificationSample';

// Sample data is ON by default because the notification backend is not implemented yet.
// Set VITE_NOTIFICATIONS_USE_SAMPLE_DATA=false in frontend/.env to call the real endpoints below.
export const NOTIFICATION_SAMPLE_MODE = import.meta.env.VITE_NOTIFICATIONS_USE_SAMPLE_DATA !== 'false';

const pause = (ms = 300) => new Promise((resolve) => setTimeout(resolve, ms));

function token() {
  const t = getToken();
  if (!t) throw new Error('Please log in to view notifications.');
  return t;
}

// The backend identifies the user from the session token; these keys only separate sample data per user.
export const userKeyOf = (user) => String(user?.id ?? user?.email ?? 'anonymous');

// Accept either a plain array or a Spring Page ({ content: [...] }).
const toArray = (data) => (Array.isArray(data) ? data : data?.content || []);

function normalize(n) {
  return {
    id: String(n.id),
    category: n.category ? String(n.category).toLowerCase() : 'general',
    severity: n.severity ? String(n.severity).toLowerCase() : 'info',
    title: n.title || 'Notification',
    description: n.description || n.message || '',
    createdAt: n.createdAt,
    read: Boolean(n.read ?? n.isRead),
    link: n.link || null,
  };
}

export async function listNotifications(user) {
  if (NOTIFICATION_SAMPLE_MODE) {
    await pause();
    return sampleList(userKeyOf(user), user.role);
  }
  const data = await request('/api/notifications?size=200', { token: token() });
  return toArray(data).map(normalize);
}

export async function markNotificationRead(user, id) {
  if (NOTIFICATION_SAMPLE_MODE) { sampleSetRead(userKeyOf(user), user.role, [id]); return; }
  await request(`/api/notifications/${encodeURIComponent(id)}/read`, { method: 'PATCH', token: token() });
}

export async function markAllNotificationsRead(user) {
  if (NOTIFICATION_SAMPLE_MODE) { sampleMarkAll(userKeyOf(user), user.role); return; }
  await request('/api/notifications/read-all', { method: 'PATCH', token: token() });
}

export async function getNotificationPreferences(user) {
  if (NOTIFICATION_SAMPLE_MODE) {
    await pause(200);
    return samplePreferences(userKeyOf(user), user.role);
  }
  const data = await request('/api/notifications/preferences', { token: token() });
  return {
    categories: { ...DEFAULT_PREFERENCES.categories, ...(data.categories || {}) },
    channels: { ...DEFAULT_PREFERENCES.channels, ...(data.channels || {}) },
    dailySummary: { ...DEFAULT_PREFERENCES.dailySummary, ...(data.dailySummary || {}) },
  };
}

export async function saveNotificationPreferences(user, preferences) {
  if (NOTIFICATION_SAMPLE_MODE) {
    await pause(300);
    return sampleSavePreferences(userKeyOf(user), user.role, preferences);
  }
  return request('/api/notifications/preferences', { method: 'PUT', body: preferences, token: token() });
}
