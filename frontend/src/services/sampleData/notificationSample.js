// SAMPLE NOTIFICATIONS - NOT REAL EVENTS.
// Used only while the backend notification API is not connected. Read/unread state and
// preferences are kept in this browser's localStorage, per user, so the UI can be demonstrated.

import { read, write } from '../../utils/storage';
import { categoriesForRole } from '../../config/notificationCategories';

const HOUR = 3600 * 1000;
const ago = (hours) => new Date(Date.now() - hours * HOUR).toISOString();

const SEEDS = [
  { category: 'weather', severity: 'critical', title: 'Heavy rainfall warning', description: 'Heavy rain is forecast for your farm in the next 48 hours. Postpone spraying and check field drainage.', hoursAgo: 1, read: false, link: '/weather' },
  { category: 'inventory', severity: 'warning', title: 'Low stock: Organic Fertilizer', description: 'Stock has fallen below the minimum level. Reorder before the next application.', hoursAgo: 3, read: false, link: '/inventory' },
  { category: 'irrigation', severity: 'info', title: 'Skip watering - rain expected', description: 'Rain is expected tomorrow. Consider skipping the scheduled irrigation to save water.', hoursAgo: 5, read: false, link: '/weather' },
  { category: 'equipment', severity: 'info', title: 'Equipment booking approved', description: 'Your tractor booking for this week has been approved by the admin.', hoursAgo: 9, read: false, link: '/equipment/bookings' },
  { category: 'crop', severity: 'info', title: 'Crop moved to flowering stage', description: 'One of your crops has reached the flowering stage. Review the care checklist.', hoursAgo: 26, read: true },
  { category: 'weather', severity: 'warning', title: 'Heatwave warning', description: 'Daytime temperatures may exceed 40°C. Irrigate in the early morning or evening.', hoursAgo: 30, read: false, link: '/weather' },
  { category: 'general', severity: 'info', title: 'New task assigned', description: 'A field worker has been assigned to your farm. Check the farm page for details.', hoursAgo: 52, read: true, link: '/farms' },
  { category: 'equipment', severity: 'warning', title: 'Service reminder: Sprayer', description: 'The sprayer is due for maintenance within the next 7 days.', hoursAgo: 75, read: true, link: '/equipment' },
  { category: 'inventory', severity: 'warning', title: 'Seeds expiring soon', description: 'A batch of seeds will expire within 30 days. Use it in the next sowing.', hoursAgo: 98, read: true, link: '/inventory' },
  { category: 'crop', severity: 'warning', title: 'Pest observation recorded', description: 'A pest problem was logged on one of your plots. Review treatment options.', hoursAgo: 130, read: true },
  { category: 'general', severity: 'info', title: 'Welcome to FarmVerse Nexus', description: 'Your account is ready. Explore your farms, weather and notifications.', hoursAgo: 200, read: true },
];

export const DEFAULT_PREFERENCES = {
  categories: { weather: true, crop: true, inventory: true, irrigation: true, equipment: true, general: true },
  channels: { inApp: true, email: false, sms: false },
  dailySummary: { enabled: false, time: '06:30' },
};

const key = (userKey) => `fvn_sample_notifications_${userKey}`;

function load(userKey, role) {
  const saved = read(key(userKey), null);
  if (saved && Array.isArray(saved.items)) return saved;
  const allowed = new Set(categoriesForRole(role).map((c) => c.key));
  const fresh = {
    items: SEEDS.filter((s) => allowed.has(s.category)).map((s, i) => ({
      id: `sample-${i + 1}`,
      category: s.category,
      severity: s.severity,
      title: s.title,
      description: s.description,
      createdAt: ago(s.hoursAgo),
      read: s.read,
      link: s.link || null,
    })),
    preferences: DEFAULT_PREFERENCES,
  };
  write(key(userKey), fresh);
  return fresh;
}

export const sampleList = (userKey, role) => load(userKey, role).items;

export function sampleSetRead(userKey, role, ids, value = true) {
  const data = load(userKey, role);
  const set = new Set(ids);
  data.items = data.items.map((n) => (set.has(n.id) ? { ...n, read: value } : n));
  write(key(userKey), data);
}

export function sampleMarkAll(userKey, role) {
  const data = load(userKey, role);
  data.items = data.items.map((n) => ({ ...n, read: true }));
  write(key(userKey), data);
}

export const samplePreferences = (userKey, role) => ({ ...DEFAULT_PREFERENCES, ...load(userKey, role).preferences });

export function sampleSavePreferences(userKey, role, preferences) {
  const data = load(userKey, role);
  data.preferences = preferences;
  write(key(userKey), data);
  return preferences;
}
