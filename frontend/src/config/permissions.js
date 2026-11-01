
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
export const NAV_ITEMS = [
  {
    key: 'overview',
    label: 'Overview',
    path: '/overview',
    icon: LayoutDashboard
  },
  {
    key: 'users',
    label: 'User & Access Management',
    path: '/users',
    icon: Users
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
