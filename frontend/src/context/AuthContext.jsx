import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import * as auth from '../services/authService';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => auth.getSession());
  const login = useCallback(async (creds) => { const u = await auth.login(creds); setUser(u); return u; }, []);
  // Wait for backend logout before clearing the user from React state.
  const logout = useCallback(async () => {
    try {
      await auth.logout();
    } finally {
      setUser(null);
    }
  }, []);
  const refresh = useCallback(() => setUser(auth.getSession()), []);
  const value = useMemo(() => ({ user, login, logout, refresh, register: auth.register }), [user, login, logout, refresh]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
