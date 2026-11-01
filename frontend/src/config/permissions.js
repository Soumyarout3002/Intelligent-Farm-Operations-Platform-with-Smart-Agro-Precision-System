
import {
  LayoutDashboard,
  Users,
  Tractor,
  UserCircle,
  Package,
  Wrench,
  CalendarDays,
  ClipboardList
} from 'lucide-react';

// Role-based permissions
export const ROLE_PERMISSIONS = {
  farmer: [
    'overview',
    'farms',
    'profile',
    'inventory',
    'equipment',
    'equipmentBookings',
    'resources'
  ],

  fieldWorker: [
    'overview',
    'farms',
    'profile',
    'equipment',
    'equipmentBookings',
    'resources'
  ],

  admin: [
    'overview',
    'users',
    'farms',
    'profile',
    'inventory',
    'equipment',
    'equipmentBookings',
    'resources'
  ]
};

// Sidebar navigation


    icon: LayoutDashboard
    icon: LayoutDashboard,
  },
  {
    key: 'users',
    label: 'User & Access Management',
    path: '/users',
    icon: Users
    icon: Users,
  },
  {
    key: 'farms',
    label: 'Farm Management',
    path: '/farms',
    icon: Tractor
  },
  {
    key: 'inventory',
    label: 'Inventory Management',
    path: '/inventory',
    icon: Package
  },
  {
    key: 'equipment',
    label: 'Equipment Management',
    path: '/equipment',
    icon: Wrench
  },
  {
    key: 'equipmentBookings',
    label: 'Equipment Bookings',
    path: '/equipment/bookings',
    icon: CalendarDays
  },
  {
    key: 'resources',
    label: 'Farm Resource Planning',
    path: '/resources',
    icon: ClipboardList
    icon: Tractor,
  },
  {
    key: 'profile',
    label: 'User Profile',
    path: '/profile',
    icon: UserCircle
  }
];

// Permission checking
export const can = (role, key) =>
  (ROLE_PERMISSIONS[role] || []).includes(key);

// Available roles
export const ROLES = ['farmer', 'fieldWorker', 'admin'];

// Capitalize text
export const cap = (s = '') =>
  s.charAt(0).toUpperCase() + s.slice(1);

// Display-friendly role names
export const roleLabel = (role) => {
  if (role === 'fieldWorker') return 'Field Worker';
  return cap(role);
};
    icon: UserCircle,
  },
];

// Normalize backend roles such as FIELD_WORKER for frontend use.
export const normalizeRole = (role = '') =>
  role.toLowerCase().replace(/-/g, '_');

export const can = (role, key) =>
  (ROLE_PERMISSIONS[normalizeRole(role)] || []).includes(key);

// Only the documented project roles are available.
export const ROLES = ['farmer', 'field_worker', 'admin'];

export const cap = (value = '') =>
  value
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
