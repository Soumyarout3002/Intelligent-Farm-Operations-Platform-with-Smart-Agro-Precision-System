import { LayoutDashboard, Users, Tractor, UserCircle } from 'lucide-react';

// Configurable role -> permission map (FRONTEND DEMO ONLY; backend must enforce real authorization).
export const ROLE_PERMISSIONS = {
  farmer: ['overview', 'farms', 'profile'],
  manager: ['overview', 'farms', 'profile'],
  admin: ['overview', 'users', 'farms', 'profile'],
};

// Add future modules here (one entry + one route in App.jsx).
export const NAV_ITEMS = [
  { key: 'overview', label: 'Overview', path: '/overview', icon: LayoutDashboard },
  { key: 'users', label: 'User & Access Management', path: '/users', icon: Users },
  { key: 'farms', label: 'Farm Management', path: '/farms', icon: Tractor },
  { key: 'profile', label: 'User Profile', path: '/profile', icon: UserCircle },
];

export const can = (role, key) => (ROLE_PERMISSIONS[role] || []).includes(key);
export const ROLES = ['farmer', 'manager', 'admin'];
export const cap = (s = '') => s.charAt(0).toUpperCase() + s.slice(1);
