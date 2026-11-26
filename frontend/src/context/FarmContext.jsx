import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useAuth } from './AuthContext';
import { listFarms } from '../services/farmService';

// One shared "active farm" for every farm-scoped module.
// The farm list comes from the existing Farm API, which the backend already limits by role
// (admin: all farms, farmer: own farms, field worker: actively assigned farms).
// The selection can only ever be one of those returned farms.

const FarmContext = createContext(null);
export const useFarm = () => useContext(FarmContext);

const storageKey = (user) => `farmverse.activeFarm.${user?.id ?? user?.email ?? 'anonymous'}`;

const readStored = (user) => {
  try { return sessionStorage.getItem(storageKey(user)) || ''; } catch { return ''; }
};
const writeStored = (user, id) => {
  try {
    if (id) sessionStorage.setItem(storageKey(user), id);
    else sessionStorage.removeItem(storageKey(user));
  } catch { /* storage unavailable - selection just is not remembered */ }
};

export function FarmProvider({ children }) {
  const { user } = useAuth();
  const [farms, setFarms] = useState([]);
  const [status, setStatus] = useState('loading'); // loading | ready | error
  const [error, setError] = useState('');
  const [selectedFarmId, setSelectedId] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!user) return undefined;
    let ignore = false;
    // Never show the previous user's / previous load's farms while loading.
    setStatus('loading');
    setError('');
    listFarms()
      .then((list) => {
        if (ignore) return;
        const stored = readStored(user);
        const next = list.some((f) => String(f.id) === stored)
          ? stored
          : (list[0] ? String(list[0].id) : '');
        setFarms(list);
        setSelectedId(next);
        writeStored(user, next);
        setStatus('ready');
      })
      .catch((e) => {
        if (ignore) return;
        setFarms([]);
        setSelectedId('');
        setError(e.message || 'Could not load farms.');
        setStatus('error');
      });
    return () => { ignore = true; };
  }, [user, reloadKey]);

  const setSelectedFarmId = useCallback((id) => {
    const value = String(id ?? '');
    // Ignore any id that is not in the authorised list returned by the backend.
    if (!farms.some((f) => String(f.id) === value)) return;
    setSelectedId(value);
    writeStored(user, value);
  }, [farms, user]);

  const reload = useCallback(() => setReloadKey((k) => k + 1), []);

  const selectedFarm = useMemo(
    () => farms.find((f) => String(f.id) === selectedFarmId) || null,
    [farms, selectedFarmId]
  );

  const value = useMemo(
    () => ({ farms, status, error, selectedFarm, selectedFarmId, setSelectedFarmId, reload }),
    [farms, status, error, selectedFarm, selectedFarmId, setSelectedFarmId, reload]
  );

  return <FarmContext.Provider value={value}>{children}</FarmContext.Provider>;
}
