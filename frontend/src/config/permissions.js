
import {
  LayoutDashboard,
  Users,
  Tractor,
  UserCircle,
  History,
} from 'lucide-react';

// Frontend role permissions.
// Real authorization is enforced by the Spring Boot backend.
export const ROLE_PERMISSIONS = {
  farmer: ['overview', 'farms', 'profile'],
  field_worker: ['overview', 'farms', 'profile'],
  admin: [
    'overview',
    'users',
    'activity-history',
    'farms',
    'profile',
  ],
};

// Navigation items available in the application.
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
    key: 'profile',
    label: 'User Profile',
    path: '/profile',
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
