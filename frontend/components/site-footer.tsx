import Link from "next/link";

export function SiteFooter() {
  return (
    <footer className="mt-20 border-t border-line/70 py-10">
      <div className="shell flex flex-col gap-8 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="font-display text-2xl font-bold text-ink">Pulse Tickets</p>
          <p className="mt-2 max-w-xl text-sm leading-6 text-smoke">
            A modern front-end for your Spring Boot ticketing platform: editorial discovery, responsive seat selection,
            admin event publishing, and a bookings hub that matches the APIs already in your microservice backend.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3 text-sm font-semibold text-ink">
          <Link href="/events">Browse events</Link>
          <span className="text-line">/</span>
          <Link href="/bookings">My tickets</Link>
          <span className="text-line">/</span>
          <Link href="/admin/events">Organizer studio</Link>
        </div>
      </div>
    </footer>
  );
}
