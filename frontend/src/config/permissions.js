import {
  LayoutDashboard,
  Users,
  Tractor,
  UserCircle,
  Package,
  Wrench,
  CalendarDays,
  ClipboardList,
  CloudSun,
  Bell,
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
    'notifications',
  ],
};

// Sidebar navigation
// Activity History intentionally does not appear in the sidebar.
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
    key: 'notifications',
    label: 'Notifications',
    path: '/notifications',
    icon: Bell,
  },
  {
    key: 'profile',
    label: 'User Profile',
    path: '/profile',
    icon: UserCircle,
  },
];

// Normalize backend roles such as FIELD_WORKER.
export const normalizeRole = (role = '') =>
  role.toLowerCase().replace(/-/g, '_');

// Permission checking
export const can = (role, key) =>
  (ROLE_PERMISSIONS[normalizeRole(role)] || []).includes(key);

export const ROLES = ['farmer', 'field_worker', 'admin'];

// Capitalize text.
export const cap = (value = '') =>
  value
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');

export const roleLabel = (role) => cap(normalizeRole(role));