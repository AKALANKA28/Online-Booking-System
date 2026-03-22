"use client";

import {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { LoginResponse, SessionUser } from "@/lib/types";

// Use NEXT_PUBLIC_ env vars for client-side Next.js code.
// If not set, fallback to a static literal key.
const SESSION_KEY =
  process.env.NEXT_PUBLIC_SESSION_KEY || "smart-ticketing-session";

interface AuthContextValue {
  user: SessionUser | null;
  hydrated: boolean;
  signIn: (payload: LoginResponse) => void;
  signOut: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function toSessionUser(payload: LoginResponse): SessionUser {
  return {
    accessToken: payload.accessToken,
    userId: payload.userId,
    email: payload.email,
    role: payload.role,
    expiresAt: payload.expiresAt,
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(null);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    try {
      const raw = window.localStorage.getItem(SESSION_KEY);
      if (raw) {
        const parsed = JSON.parse(raw) as SessionUser;
        if (new Date(parsed.expiresAt).getTime() > Date.now()) {
          setUser(parsed);
        } else {
          window.localStorage.removeItem(SESSION_KEY);
        }
      }
    } catch {
      window.localStorage.removeItem(SESSION_KEY);
    } finally {
      setHydrated(true);
    }
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      hydrated,
      signIn: (payload) => {
        const nextUser = toSessionUser(payload);
        setUser(nextUser);
        window.localStorage.setItem(SESSION_KEY, JSON.stringify(nextUser));
      },
      signOut: () => {
        setUser(null);
        window.localStorage.removeItem(SESSION_KEY);
      },
    }),
    [hydrated, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
