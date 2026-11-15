import { request } from './api';
import { getToken } from './authService';

import {
  DEFAULT_PREFERENCES,
  sampleList,
  sampleSetRead,
  sampleMarkAll,
  samplePreferences,
  sampleSavePreferences,
} from './sampleData/notificationSample';

// Set VITE_NOTIFICATIONS_USE_SAMPLE_DATA=false in frontend/.env
// to use the real Spring Boot notification APIs.
export const NOTIFICATION_SAMPLE_MODE =
  import.meta.env.VITE_NOTIFICATIONS_USE_SAMPLE_DATA !== 'false';

const pause = (ms = 300) =>
  new Promise((resolve) => setTimeout(resolve, ms));

function token() {
  const t = getToken();

  if (!t) {
    throw new Error('Please log in to view notifications.');
  }

  return t;
}

export const userKeyOf = (user) =>
  String(user?.id ?? user?.email ?? 'anonymous');

const toArray = (data) =>
  Array.isArray(data) ? data : data?.content || [];

// Convert backend notification types into frontend categories.
function getCategory(type) {
  const categories = {
    WEATHER: 'weather',
    CROP: 'crop',
    INVENTORY: 'inventory',
    IRRIGATION: 'irrigation',
    EQUIPMENT: 'equipment',
    GENERAL: 'general',
  };

  return categories[String(type || '').toUpperCase()] || 'general';
}

// Convert backend response into the structure used by React components.
function normalize(n) {
  return {
    id: String(n.id),
    category: getCategory(n.type ?? n.category),
    severity: String(n.severity || 'info').toLowerCase(),
    title: n.title || 'Notification',
    description: n.message || n.description || '',
    createdAt: n.createdAt,
    read: Boolean(n.read ?? n.isRead),
    link: n.link || null,
  };
}

// GET /api/notifications
export async function listNotifications(user) {
  if (NOTIFICATION_SAMPLE_MODE) {
    await pause();
    return sampleList(userKeyOf(user), user.role);
  }

  const data = await request('/api/notifications?size=200', {
    token: token(),
  });

  return toArray(data).map(normalize);
}

// GET /api/notifications/unread-count
export async function getUnreadNotificationCount() {
  if (NOTIFICATION_SAMPLE_MODE) {
    const notifications = await listNotifications();
    return notifications.filter((n) => !n.read).length;
  }

  const data = await request('/api/notifications/unread-count', {
    token: token(),
  });

  return Number(data.unreadCount ?? data.count ?? 0);
}

// PATCH /api/notifications/{id}/read
export async function markNotificationRead(user, id) {
  if (NOTIFICATION_SAMPLE_MODE) {
    sampleSetRead(userKeyOf(user), user.role, [id]);
    return;
  }

  return request(
    `/api/notifications/${encodeURIComponent(id)}/read`,
    {
      method: 'PATCH',
      token: token(),
    }
  );
}

// PATCH /api/notifications/read-all
export async function markAllNotificationsRead(user) {
  if (NOTIFICATION_SAMPLE_MODE) {
    sampleMarkAll(userKeyOf(user), user.role);
    return;
  }

  return request('/api/notifications/read-all', {
    method: 'PATCH',
    token: token(),
  });
}

// DELETE /api/notifications/{id}
export async function deleteNotification(id) {
  if (NOTIFICATION_SAMPLE_MODE) {
    return;
  }

  return request(`/api/notifications/${encodeURIComponent(id)}`, {
    method: 'DELETE',
    token: token(),
  });
}

// Preferences remain sample-based because the backend preferences
// endpoints have not been implemented yet.
export async function getNotificationPreferences(user) {
  await pause(200);

  return samplePreferences(userKeyOf(user), user.role);
}

export async function saveNotificationPreferences(user, preferences) {
  await pause(300);

  return sampleSavePreferences(
    userKeyOf(user),
    user.role,
    preferences
  );
}