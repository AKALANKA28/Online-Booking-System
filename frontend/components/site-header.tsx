"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import { initialsFromEmail } from "@/lib/formatters";
import { useAuth } from "@/components/auth-provider";

const links = [
  { href: "/", label: "Home" },
  { href: "/events", label: "Explore" },
  { href: "/bookings", label: "My tickets" },
];

function linkClass(active: boolean) {
  return active
    ? "rounded-full bg-ink px-4 py-2 text-sm font-semibold text-white"
    : "rounded-full px-4 py-2 text-sm font-semibold text-ink transition hover:bg-white/70";
}

export function SiteHeader() {
  const pathname = usePathname();
  const [open, setOpen] = useState(false);
  const { user, hydrated, signOut } = useAuth();

  return (
    <header className="sticky top-0 z-40 border-b border-line/70 bg-paper/85 backdrop-blur-xl">
      <div className="shell flex h-20 items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <Link href="/" className="flex items-center gap-3">
            <span className="grid h-11 w-11 place-items-center rounded-2xl bg-ink font-display text-lg font-bold text-white">
              PT
            </span>
             <div>
               <p className="font-display text-lg font-bold text-ink">Pulse Tickets</p>
               <p className="text-xs text-smoke">Your ticket to unforgettable moments.</p>
             </div>
          </Link>
        </div>

        <nav className="hidden items-center gap-2 lg:flex">
          {links.map((link) => (
            <Link key={link.href} href={link.href} className={linkClass(pathname === link.href)}>
              {link.label}
            </Link>
          ))}
          {user?.role === "ADMIN" && (
            <Link href="/admin/events" className={linkClass(pathname === "/admin/events")}>
              Organizer
            </Link>
          )}
        </nav>

        <div className="hidden items-center gap-3 lg:flex">
          {hydrated && user ? (
            <>
              <div className="flex items-center gap-3 rounded-full border border-line bg-white/90 px-3 py-2">
                <span className="grid h-9 w-9 place-items-center rounded-full bg-cobaltSoft text-sm font-bold text-cobalt">
                  {initialsFromEmail(user.email)}
                </span>
                <div className="pr-2">
                  <p className="text-sm font-semibold text-ink">{user.email}</p>
                  <p className="text-xs uppercase tracking-[0.18em] text-smoke">{user.role}</p>
                </div>
              </div>
              <button
                type="button"
                onClick={signOut}
                className="rounded-full border border-line bg-white px-4 py-2 text-sm font-semibold text-ink transition hover:border-ink"
              >
                Sign out
              </button>
            </>
          ) : (
            <Link href="/login" className="rounded-full bg-ink px-5 py-3 text-sm font-semibold text-white transition hover:bg-cobalt">
              Sign in
            </Link>
          )}
        </div>

        <button
          type="button"
          className="inline-flex rounded-full border border-line bg-white px-4 py-2 text-sm font-semibold text-ink lg:hidden"
          onClick={() => setOpen((state) => !state)}
        >
          Menu
        </button>
      </div>

      {open && (
        <div className="shell pb-4 lg:hidden">
          <div className="surface flex flex-col gap-2 p-3">
            {links.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className={linkClass(pathname === link.href)}
                onClick={() => setOpen(false)}
              >
                {link.label}
              </Link>
            ))}
            {user?.role === "ADMIN" && (
              <Link href="/admin/events" className={linkClass(pathname === "/admin/events")} onClick={() => setOpen(false)}>
                Organizer
              </Link>
            )}
            {hydrated && user ? (
              <button
                type="button"
                onClick={() => {
                  signOut();
                  setOpen(false);
                }}
                className="rounded-full border border-line px-4 py-2 text-left text-sm font-semibold text-ink"
              >
                Sign out
              </button>
            ) : (
              <Link href="/login" className="rounded-full bg-ink px-4 py-2 text-sm font-semibold text-white" onClick={() => setOpen(false)}>
                Sign in
              </Link>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
