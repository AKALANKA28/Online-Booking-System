import { Suspense } from "react";
import { LoginForm } from "@/components/login-form";

export default function LoginPage() {
  return (
    <div className="shell py-10 sm:py-14">
      <div className="mb-8 max-w-3xl">
        <p className="eyebrow">Welcome back</p>
        <h1 className="mt-2 font-display text-5xl font-bold text-ink">
          Sign in to your account.
        </h1>
        <p className="mt-4 text-base leading-8 text-smoke">
          Access your bookings, manage your account, and get personalized event updates.
        </p>
      </div>
      <Suspense
        fallback={
          <div className="py-10 text-center text-sm text-smoke">
            Loading login form…
          </div>
        }
      >
        <LoginForm />
      </Suspense>
    </div>
  );
}
