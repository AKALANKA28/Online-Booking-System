import Link from "next/link";
import { EventCard } from "@/components/event-card";
import { SectionHeading } from "@/components/section-heading";
import { formatMoney } from "@/lib/formatters";
import { getCategory, getEventTheme, sortEvents } from "@/lib/event-presentation";
import { getPublicEvents } from "@/lib/server-api";

export default async function HomePage() {
  const events = sortEvents(await getPublicEvents());
  const featured = events[0];
  const categories = Array.from(new Set(events.map((event) => getCategory(event)))).slice(0, 5);
  const totalSeats = events.reduce((sum, event) => sum + (event.totalRows * event.seatsPerRow), 0);
  const featuredTheme = featured ? getEventTheme(featured, 0) : null;

  return (
    <div className="pb-10">
      <section className="shell pt-10 sm:pt-14">
        <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
          <div className="surface overflow-hidden p-8 sm:p-10">
            <p className="eyebrow">Ticketing UI direction</p>
            <h1 className="mt-3 max-w-4xl font-display text-5xl font-bold leading-[0.95] text-ink sm:text-6xl xl:text-7xl">
              A premium booking flow built for real seat selection, not generic mockups.
            </h1>
            <p className="mt-6 max-w-2xl text-base leading-8 text-smoke">
              Discovery-first landing, a focused event detail page, exact-seat selection, sticky checkout rail, organizer publishing, and a personal bookings hub. The interface is designed to feel product-led and modern without defaulting to the usual “AI gradient dashboard” look.
            </p>

            <div className="mt-8 flex flex-wrap gap-3">
              <Link href="/events" className="rounded-full bg-ink px-6 py-4 text-sm font-semibold text-white transition hover:bg-cobalt">
                Explore live events
              </Link>
              <Link href="/admin/events" className="rounded-full border border-line bg-white px-6 py-4 text-sm font-semibold text-ink transition hover:border-ink">
                Open organizer studio
              </Link>
            </div>

            <div className="mt-10 grid gap-4 sm:grid-cols-3">
              <div className="metric-card">
                <p className="text-xs uppercase tracking-[0.18em] text-smoke">Events in feed</p>
                <p className="mt-3 font-display text-4xl font-bold text-ink">{events.length}</p>
              </div>
              <div className="metric-card">
                <p className="text-xs uppercase tracking-[0.18em] text-smoke">Seat footprint</p>
                <p className="mt-3 font-display text-4xl font-bold text-ink">{totalSeats}</p>
              </div>
              <div className="metric-card">
                <p className="text-xs uppercase tracking-[0.18em] text-smoke">Entry point</p>
                <p className="mt-3 font-display text-4xl font-bold text-ink">{featured ? formatMoney(featured.regularPrice) : "$0"}</p>
              </div>
            </div>
          </div>

          {featured ? (
            <div className="surface-dark overflow-hidden p-8 sm:p-10" style={{ backgroundImage: featuredTheme?.background }}>
              <p className="eyebrow text-white/70">Featured now</p>
              <h2 className="mt-3 font-display text-4xl font-bold text-white">{featured.title}</h2>
              <p className="mt-4 max-w-xl text-sm leading-7 text-white/80">{featured.description}</p>
              <div className="mt-8 flex flex-wrap gap-3">
                <span className="ticket-chip border-white/15 bg-white/10 text-white">{featured.venue}</span>
                <span className="ticket-chip border-white/15 bg-white/10 text-white">{getCategory(featured)}</span>
                <span className="ticket-chip border-white/15 bg-white/10 text-white">Starts at {formatMoney(featured.regularPrice)}</span>
              </div>
              <Link href={`/events/${featured.id}`} className="mt-10 inline-flex rounded-full bg-white px-6 py-4 text-sm font-semibold text-ink transition hover:bg-cloud">
                Open featured event
              </Link>
            </div>
          ) : (
            <div className="surface p-8">
              <p className="text-sm text-smoke">No featured event yet. Publish one from the organizer studio.</p>
            </div>
          )}
        </div>
      </section>

      <section className="shell mt-20">
        <SectionHeading
          eyebrow="Discovery rails"
          title="A calmer, more editorial browse experience."
          description="Instead of a generic card wall, the homepage uses category chips, strong hierarchy, and a featured event block to guide users into the purchase flow quickly."
          action={<Link href="/events" className="rounded-full border border-line bg-white px-5 py-3 text-sm font-semibold text-ink">See all events</Link>}
        />
        <div className="mt-8 flex flex-wrap gap-3">
          {categories.map((category) => (
            <span key={category} className="ticket-chip bg-white text-ink">{category}</span>
          ))}
          <span className="ticket-chip bg-cobaltSoft text-cobalt">Responsive across phone, tablet, and desktop</span>
        </div>
      </section>

      <section className="shell mt-10 grid gap-6 lg:grid-cols-3">
        <div className="surface p-6">
          <p className="eyebrow">01 · Discover</p>
          <h3 className="mt-2 font-display text-2xl font-bold text-ink">Search, scan, and compare.</h3>
          <p className="mt-3 text-sm leading-7 text-smoke">Filters, category rails, and price-first cards make browsing feel familiar to ticket buyers without borrowing any one competitor’s visual language too closely.</p>
        </div>
        <div className="surface p-6">
          <p className="eyebrow">02 · Select</p>
          <h3 className="mt-2 font-display text-2xl font-bold text-ink">Pick seats with confidence.</h3>
          <p className="mt-3 text-sm leading-7 text-smoke">The event page focuses on exact seats, live availability, VIP vs regular filtering, and an always-visible order summary.</p>
        </div>
        <div className="surface p-6">
          <p className="eyebrow">03 · Verify</p>
          <h3 className="mt-2 font-display text-2xl font-bold text-ink">See payment and notification history.</h3>
          <p className="mt-3 text-sm leading-7 text-smoke">The account area surfaces booking status, payment references, and notification logs to prove the full microservice flow to your lecturer.</p>
        </div>
      </section>

      <section className="shell mt-20">
        <SectionHeading
          eyebrow="Upcoming experiences"
          title="Use the real event feed from your Spring gateway."
          description="These cards render your actual event records when the backend is running. If the services are offline, the UI falls back to curated mock data so you can still present the design work."
        />
        <div className="mt-8 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
          {events.slice(0, 6).map((event, index) => <EventCard key={event.id} event={event} index={index} />)}
        </div>
      </section>
    </div>
  );
}
