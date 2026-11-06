// Safe LocalStorage helpers: never throw on empty/invalid/blocked storage.
export function read(key, fallback, store = localStorage) {
  try {
    const raw = store.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch {
    return fallback;
  }
}
export function write(key, value, store = localStorage) {
  try {
    store.setItem(key, JSON.stringify(value));
    return true;
  } catch {
    return false;
  }
}
export function remove(key, store = localStorage) {
  try { store.removeItem(key); } catch { /* ignore */ }
}
