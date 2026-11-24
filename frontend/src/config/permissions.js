
import {
  LayoutDashboard,
  Users,
  Tractor,
  Package,
  Wrench,
  CalendarDays,
  ClipboardList,
  CloudSun,
  Droplets,
  BarChart3,
  Bot,
} from 'lucide-react';

// Frontend role permissions.
// Real authorization is enforced by the Spring Boot backend.
export const ROLE_PERMISSIONS = {
  farmer: [
    'overview',
    'farms',
    'profile',
    'inventory',
    'equipment',
    'equipmentBookings',
    'resources',
    'weather',
    'irrigation',
    'analytics',
    'aiAssistant',
    'notifications',
  ],

  field_worker: [
    'overview',
    'farms',
    'profile',
    'equipment',
    'equipmentBookings',
    'resources',
    'weather',
    'notifications',
  ],

  admin: [
    'overview',
    'users',
    'activity-history',
    'farms',
    'profile',
    'inventory',
    'equipment',
    'equipmentBookings',
    'resources',
    'weather',
    'irrigation',
    'analytics',
    'aiAssistant',
    'notifications',
  ],
};

// Sidebar navigation
// Activity History intentionally does not appear in the sidebar.
// It is accessible through the Dashboard's "View all" link.
export const NAV_ITEMS = [
  {
    key: 'overview',
    label: 'Overview',
    path: '/overview',
    icon: LayoutDashboard,
  },
  {
    key: 'users',
    label: 'User & Access Management',
    path: '/users',
    icon: Users,
  },
  {
    key: 'farms',
    label: 'Farm Management',
    path: '/farms',
    icon: Tractor,
  },
  {
    key: 'inventory',
    label: 'Inventory Management',
    path: '/inventory',
    icon: Package,
  },
  {
    key: 'equipment',
    label: 'Equipment Management',
    path: '/equipment',
    icon: Wrench,
  },
  {
    key: 'equipmentBookings',
    label: 'Equipment Bookings',
    path: '/equipment/bookings',
    icon: CalendarDays,
  },
  {
    key: 'resources',
    label: 'Farm Resource Planning',
    path: '/resources',
    icon: ClipboardList,
  },
  {
    key: 'weather',
    label: 'Weather Monitoring',
    path: '/weather',
    icon: CloudSun,
  },
  {
    key: 'irrigation',
    label: 'Irrigation Scheduling',
    path: '/irrigation',
    icon: Droplets,
  },
  {
    key: 'analytics',
    label: 'Agricultural Analytics',
    path: '/analytics',
    icon: BarChart3,
  },
  {
    key: 'aiAssistant',
    label: 'AI Assistant',
    path: '/ai-assistant',
    icon: Bot,
  },
  // Notifications now live in the header bell (see Header.jsx).
  // The 'notifications' permission and the /notifications route stay for the "View all" page.
  // User Profile is no longer in the sidebar; it opens from the header name/avatar.
  // The 'profile' permission and /profile route are unchanged.
];

// Normalize backend roles such as FIELD_WORKER for frontend use.
export const normalizeRole = (role = '') =>
  role.toLowerCase().replace(/-/g, '_');

// Permission checking
export const can = (role, key) =>
  (ROLE_PERMISSIONS[normalizeRole(role)] || []).includes(key);

// Only the documented project roles are available.
export const ROLES = ['farmer', 'field_worker', 'admin'];

// Capitalize text: "field_worker" -> "Field Worker"
export const cap = (value = '') =>
  value
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');

// Display-friendly role names
export const roleLabel = (role) => cap(normalizeRole(role));
