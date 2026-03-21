import { LoginForm } from "@/components/login-form";

export default function LoginPage() {
  return (
    <div className="shell py-10 sm:py-14">
      <div className="mb-8 max-w-3xl">
        <p className="eyebrow">Authentication</p>
        <h1 className="mt-2 font-display text-5xl font-bold text-ink">Sign in with the Spring gateway’s demo users.</h1>
        <p className="mt-4 text-base leading-8 text-smoke">This page is intentionally simple and trustworthy so it supports the polished experience instead of feeling like a hacked-on admin screen.</p>
      </div>
      <LoginForm />
    </div>
  );
}
