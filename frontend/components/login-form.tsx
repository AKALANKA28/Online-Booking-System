"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/components/auth-provider";
import { ApiError, login, registerUser } from "@/lib/client-api";

export function LoginForm() {
  const [mode, setMode] = useState<"signin" | "register">("signin");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const router = useRouter();
  const searchParams = useSearchParams();
  const { signIn } = useAuth();

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      if (mode === "register") {
        await registerUser({ username, email, password });
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
            "Could not connect to the server. Please try again later.",
          );
        }
       }
     finally {
      const response = await login({ username, password });
      signIn(response);
      router.push(searchParams.get("next") || "/bookings");
      router.refresh();
    } catch (caught) {
      if (caught instanceof ApiError) {
        setError(caught.message || "Authentication failed.");
      } else {
        setError(
          "Could not connect to the server. Please try again later.",
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
          Sign in to access your bookings, manage your account, and get personalized event updates.
        </p>

      </div>

      <form onSubmit={onSubmit} className="surface p-8">
        <p className="eyebrow">Account</p>
        <h3 className="mt-2 font-display text-3xl font-bold text-ink">
          {mode === "signin" ? "Sign in" : "Create account"}
        </h3>
        <p className="mt-3 text-sm leading-6 text-smoke">
          {mode === "signin"
            ? "Enter your username and password to sign in."
            : "Create a new account to start booking events."}
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
