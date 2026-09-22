import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import type { AuthUser } from '../../shared/api/types';
import { ApiError } from '../../shared/api/client';
import { authApi } from './api';

type AuthContextValue = {
  user: AuthUser | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, displayName: string) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);
  const queryClient = useQueryClient();

  useEffect(() => {
    let active = true;
    authApi.me().then((current) => { if (active) setUser(current); })
      .catch((error) => {
        if (!(error instanceof ApiError && error.status === 401)) setUser(null);
      })
      .finally(() => { if (active) setLoading(false); });
    const unauthorized = () => { setUser(null); queryClient.clear(); };
    window.addEventListener('joblens:unauthorized', unauthorized);
    return () => { active = false; window.removeEventListener('joblens:unauthorized', unauthorized); };
  }, [queryClient]);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    loading,
    login: async (email, password) => { setUser(await authApi.login({ email, password })); queryClient.clear(); },
    register: async (email, password, displayName) => { setUser(await authApi.register({ email, password, displayName })); queryClient.clear(); },
    logout: async () => { await authApi.logout(); setUser(null); queryClient.clear(); },
  }), [loading, queryClient, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth must be used inside AuthProvider');
  return value;
}
