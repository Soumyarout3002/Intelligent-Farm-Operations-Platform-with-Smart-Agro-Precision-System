import { CloudRain, Sprout, Package, Droplets, Wrench, Info } from 'lucide-react';
import { normalizeRole } from './permissions';

// Notification categories from the mentor's plan (weather, crop, inventory, irrigation, equipment, general).
export const CATEGORIES = [
  { key: 'weather', label: 'Weather alerts', short: 'Weather', icon: CloudRain, tone: 'blue', hint: 'Heavy rain, heatwave and strong wind warnings for your farms.' },
  { key: 'crop', label: 'Crop alerts', short: 'Crops', icon: Sprout, tone: 'green', hint: 'Growth stage changes, pest or disease reports and harvest reminders.' },
  { key: 'inventory', label: 'Inventory alerts', short: 'Inventory', icon: Package, tone: 'amber', hint: 'Low stock and expiry warnings for seeds, fertiliser and fuel.' },
  { key: 'irrigation', label: 'Irrigation alerts', short: 'Irrigation', icon: Droplets, tone: 'teal', hint: 'Watering reminders and "skip - rain expected" suggestions.' },
  { key: 'equipment', label: 'Equipment alerts', short: 'Equipment', icon: Wrench, tone: 'slate', hint: 'Booking approvals, service reminders and breakdown reports.' },
  { key: 'general', label: 'General farm notifications', short: 'General', icon: Info, tone: 'gray', hint: 'Farm updates, task assignments and announcements.' },
];

export const categoryMeta = (key) => CATEGORIES.find((c) => c.key === key) || CATEGORIES[CATEGORIES.length - 1];

// Field Workers do not have the Inventory screens, so they are not offered inventory alerts.
export const categoriesForRole = (role) =>
  normalizeRole(role) === 'field_worker' ? CATEGORIES.filter((c) => c.key !== 'inventory') : CATEGORIES;
