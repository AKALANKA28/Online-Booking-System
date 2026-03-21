"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/components/auth-provider";
import { ApiError, login } from "@/lib/client-api";

const DEMO_USERS = [
  { username: "admin", password: "admin123", label: "Organizer / Admin" },
  { username: "customer", password: "customer123", label: "Primary Customer" },
  { username: "user2", password: "user2123", label: "Secondary Customer" },
];

export function LoginForm() {
  const [username, setUsername] = useState("customer");
  const [password, setPassword] = useState("customer123");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();
  const searchParams = useSearchParams();
  const { signIn } = useAuth();

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await login({ username, password });
      signIn(response);
      router.push(searchParams.get("next") || "/bookings");
      router.refresh();
    } catch (caught) {
      if (caught instanceof ApiError) {
        setError(caught.message || "Invalid username or password.");
      } else {
        setError("Could not reach the gateway. Make sure the Spring services are running.");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
      <div className="surface-dark p-8" style={{ backgroundImage: "linear-gradient(135deg, #10182F 0%, #1B2B61 48%, #2459FF 100%)" }}>
        <p className="eyebrow text-white/70">Demo access</p>
        <h2 className="mt-3 font-display text-4xl font-bold text-white">Sign in the same way your gateway expects.</h2>
        <p className="mt-4 max-w-lg text-sm leading-7 text-white/80">
          The UI sends credentials to <span className="font-semibold text-white">/auth/login</span>, stores the JWT, and then uses that token for bookings, organizer actions, payments, and notification history.
        </p>
        <div className="mt-8 grid gap-3">
          {DEMO_USERS.map((user) => (
            <button
              key={user.username}
              type="button"
              onClick={() => {
                setUsername(user.username);
                setPassword(user.password);
              }}
              className="rounded-3xl border border-white/15 bg-white/10 px-5 py-4 text-left transition hover:bg-white/15"
            >
              <p className="text-sm font-semibold text-white">{user.label}</p>
              <p className="mt-1 text-sm text-white/70">{user.username} / {user.password}</p>
            </button>
          ))}
        </div>
      </div>

      <form onSubmit={onSubmit} className="surface p-8">
        <p className="eyebrow">Account</p>
        <h3 className="mt-2 font-display text-3xl font-bold text-ink">Enter your demo credentials</h3>
        <p className="mt-3 text-sm leading-6 text-smoke">Keep this page in your frontend because it makes your local demo flow much smoother during marking and viva.</p>

        <div className="mt-8 space-y-5">
          <label className="block">
            <span className="eyebrow">Username</span>
            <input value={username} onChange={(event) => setUsername(event.target.value)} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <label className="block">
            <span className="eyebrow">Password</span>
            <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <button type="submit" disabled={loading} className="w-full rounded-full bg-ink px-5 py-4 text-sm font-semibold text-white transition hover:bg-cobalt disabled:opacity-70">
            {loading ? "Signing in..." : "Continue to Pulse Tickets"}
          </button>

          {error && <p className="rounded-2xl border border-ember/20 bg-emberSoft px-4 py-3 text-sm font-medium text-ember">{error}</p>}
        </div>
      </form>
    </div>
  );
}
