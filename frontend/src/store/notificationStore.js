import { useSyncExternalStore } from 'react';
import {
  listNotifications,
  markNotificationRead,
  markAllNotificationsRead,
  userKeyOf,
} from '../services/notificationService';

// Tiny shared store so the header bell and the Notifications page always show the same
// unread count without adding a new provider to main.jsx.
let state = { userKey: null, items: [], loading: false, loaded: false, error: '' };
const listeners = new Set();

function setState(patch) {
  state = { ...state, ...patch };
  listeners.forEach((listener) => listener());
}

const subscribe = (listener) => { listeners.add(listener); return () => listeners.delete(listener); };
const getSnapshot = () => state;

export const useNotificationStore = () => useSyncExternalStore(subscribe, getSnapshot);

export const unreadCount = (items) => items.filter((n) => !n.read).length;

let latestRequest = 0;

export async function loadNotifications(user) {
  const userKey = userKeyOf(user);
  const switchedUser = state.userKey !== userKey;
  const requestId = ++latestRequest;
  // Keep showing the current list while refreshing; only show the spinner on first load.
  setState(switchedUser
    ? { userKey, items: [], loaded: false, loading: true, error: '' }
    : { loading: !state.loaded, error: '' });
  try {
    const items = await listNotifications(user);
    if (requestId !== latestRequest) return;
    const sorted = [...items].sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt)));
    setState({ items: sorted, loading: false, loaded: true, error: '' });
  } catch (error) {
    if (requestId !== latestRequest) return;
    setState({ loading: false, error: error.message || 'Could not load notifications.' });
  }
}

// Optimistic update; rolls back and rethrows if the backend rejects the change.
async function optimistic(user, change, call) {
  const previous = state.items;
  setState({ items: change(previous) });
  try {
    await call();
  } catch (error) {
    setState({ items: previous });
    throw error;
  }
}

export const markAsRead = (user, id) =>
  optimistic(user, (items) => items.map((n) => (n.id === id ? { ...n, read: true } : n)), () => markNotificationRead(user, id));

export const markAllAsRead = (user) =>
  optimistic(user, (items) => items.map((n) => ({ ...n, read: true })), () => markAllNotificationsRead(user));
