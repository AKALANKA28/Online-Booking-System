import { Suspense } from "react";
import { LoginForm } from "@/components/login-form";

export default function LoginPage() {
  return (
    <div className="shell py-10 sm:py-14">
      <div className="mb-8 max-w-3xl">
        <p className="eyebrow">Authentication</p>
        <h1 className="mt-2 font-display text-5xl font-bold text-ink">
          Sign in with real user-service accounts.
        </h1>
        <p className="mt-4 text-base leading-8 text-smoke">
          Register and login now connect to user-service backed by PostgreSQL,
          while the API gateway still issues and validates JWTs across the
          platform.
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
