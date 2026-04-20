"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/components/auth-provider";
import { ApiError, login, registerUser } from "@/lib/client-api";

export function LoginForm() {
  const [mode, setMode] = useState<"signin" | "register">("signin");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const router = useRouter();
  const searchParams = useSearchParams();
  const { signIn } = useAuth();
  const oauthLoginUrl = process.env.NEXT_PUBLIC_OAUTH_LOGIN_URL;

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      if (mode === "register") {
        await registerUser({
          username,
          email,
          password,
          ...(phone.trim() ? { phone: phone.trim() } : {}),
        });
        setSuccess("Account created. Signing you in now...");
      }

      const response = await login({ username, password });
      signIn(response);
      router.push(searchParams.get("next") || "/bookings");
      router.refresh();
    } catch (caught) {
      if (caught instanceof ApiError) {
        setError(caught.message || "Authentication failed.");
      } else {
        setError(
          "Could not reach the gateway. Make sure the Spring services are running.",
        );
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
      <div
        className="surface-dark p-8"
        style={{
          backgroundImage:
            "linear-gradient(135deg, #10182F 0%, #1B2B61 48%, #2459FF 100%)",
        }}
      >
        <p className="eyebrow text-white/70">Account access</p>
        <h2 className="mt-3 font-display text-4xl font-bold text-white">
          Sign in with user-service credentials.
        </h2>
        <p className="mt-4 max-w-lg text-sm leading-7 text-white/80">
          Login now validates credentials against the user-service database
          through the gateway, then stores a JWT for bookings, admin actions,
          payments, and notification history.
        </p>
        <div className="mt-8 rounded-3xl border border-white/15 bg-white/10 px-5 py-4 text-sm text-white/80">
          <p className="font-semibold text-white">
            Admin account is auto-seeded in user-service.
          </p>
          <p className="mt-2">
            Defaults come from backend env vars: ADMIN_USERNAME / ADMIN_PASSWORD
            / ADMIN_EMAIL.
          </p>
        </div>
      </div>

      <form onSubmit={onSubmit} className="surface p-8">
        <p className="eyebrow">Account</p>
        <h3 className="mt-2 font-display text-3xl font-bold text-ink">
          {mode === "signin" ? "Sign in" : "Create account"}
        </h3>
        <p className="mt-3 text-sm leading-6 text-smoke">
          {mode === "signin"
            ? "Use your existing username and password from user-service."
            : "Register a new customer account in user-service and sign in immediately."}
        </p>

        <div className="mt-6 grid grid-cols-2 gap-2 rounded-full border border-line bg-cloud p-1">
          <button
            type="button"
            onClick={() => {
              setMode("signin");
              setError(null);
              setSuccess(null);
            }}
            className={
              mode === "signin"
                ? "rounded-full bg-ink px-4 py-2 text-sm font-semibold text-white"
                : "rounded-full px-4 py-2 text-sm font-semibold text-ink"
            }
          >
            Sign in
          </button>
          <button
            type="button"
            onClick={() => {
              setMode("register");
              setError(null);
              setSuccess(null);
            }}
            className={
              mode === "register"
                ? "rounded-full bg-ink px-4 py-2 text-sm font-semibold text-white"
                : "rounded-full px-4 py-2 text-sm font-semibold text-ink"
            }
          >
            Register
          </button>
        </div>

        <div className="mt-8 space-y-5">
          <label className="block">
            <span className="eyebrow">Username</span>
            <input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt"
            />
          </label>

          {mode === "register" && (
            <label className="block">
              <span className="eyebrow">Email</span>
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt"
              />
            </label>
          )}

          {mode === "register" && (
            <label className="block">
              <span className="eyebrow">Mobile (optional, E.164)</span>
              <input
                type="tel"
                placeholder="+15551234567"
                value={phone}
                onChange={(event) => setPhone(event.target.value)}
                className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt"
              />
            </label>
          )}

          <label className="block">
            <span className="eyebrow">Password</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt"
            />
          </label>

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-full bg-ink px-5 py-4 text-sm font-semibold text-white transition hover:bg-cobalt disabled:opacity-70"
          >
            {loading
              ? mode === "signin"
                ? "Signing in..."
                : "Creating account..."
              : mode === "signin"
                ? "Continue to Pulse Tickets"
                : "Register and continue"}
          </button>

          <button
            type="button"
            disabled={!oauthLoginUrl || loading}
            onClick={() => {
              if (oauthLoginUrl) {
                window.location.href = oauthLoginUrl;
              }
            }}
            className="w-full rounded-full border border-line bg-white px-5 py-4 text-sm font-semibold text-ink transition hover:border-cobalt disabled:cursor-not-allowed disabled:opacity-60"
          >
            Continue with OAuth
          </button>

          {!oauthLoginUrl && (
            <p className="text-xs leading-6 text-smoke">
              OAuth can be enabled by setting NEXT_PUBLIC_OAUTH_LOGIN_URL. Neon
              console OAuth secures Neon access, not end-user app sign-in.
            </p>
          )}

          {success && (
            <p className="rounded-2xl border border-pine/20 bg-pineSoft px-4 py-3 text-sm font-medium text-pine">
              {success}
            </p>
          )}

          {error && (
            <p className="rounded-2xl border border-ember/20 bg-emberSoft px-4 py-3 text-sm font-medium text-ember">
              {error}
            </p>
          )}
        </div>
      </form>
    </div>
  );
}
