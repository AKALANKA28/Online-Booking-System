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
            <p className="eyebrow">Your gateway to live events</p>
            <h1 className="mt-3 max-w-4xl font-display text-5xl font-bold leading-[0.95] text-ink sm:text-6xl xl:text-7xl">
              Your premier destination for live event tickets. 
            </h1>
            <p className="mt-6 max-w-2xl text-base leading-8 text-smoke">
              Browse upcoming events, secure your seats instantly, and get ready for unforgettable experiences. Simple, secure, and designed with you in mind.
            </p>

            <div className="mt-8 flex flex-wrap gap-3">
              <Link href="/events" className="rounded-full bg-ink px-6 py-4 text-sm font-semibold text-white transition hover:bg-cobalt">
                Explore events
              </Link>
              <Link href="/admin/events" className="rounded-full border border-line bg-white px-6 py-4 text-sm font-semibold text-ink transition hover:border-ink">
                Organizer studio
              </Link>
            </div>

            <div className="mt-10 grid gap-4 sm:grid-cols-3">
              <div className="metric-card">
                <p className="text-xs uppercase tracking-[0.18em] text-smoke">Upcoming events</p>
                <p className="mt-3 font-display text-4xl font-bold text-ink">{events.length}</p>
              </div>
              <div className="metric-card">
                <p className="text-xs uppercase tracking-[0.18em] text-smoke">Total seats</p>
                <p className="mt-3 font-display text-4xl font-bold text-ink">{totalSeats}</p>
              </div>
              <div className="metric-card">
                <p className="text-xs uppercase tracking-[0.18em] text-smoke">Starting price</p>
                <p className="mt-3 font-display text-4xl font-bold text-ink">{featured ? formatMoney(featured.regularPrice) : "LKR 0"}</p>
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
               <p className="text-sm text-smoke">No featured event available right now. Check back soon or browse all events.</p>
             </div>
           )}
        </div>
      </section>

      <section className="shell mt-20">
        <SectionHeading
          eyebrow="Explore events"
          title="Find your perfect event from our curated selection."
          description="Browse events by category, discover what's trending, and find your next unforgettable experience."
          action={<Link href="/events" className="rounded-full border border-line bg-white px-5 py-3 text-sm font-semibold text-ink">View all events</Link>}
        />
        <div className="mt-8 flex flex-wrap gap-3">
          {categories.map((category) => (
            <span key={category} className="ticket-chip bg-white text-ink">{category}</span>
          ))}
          <span className="ticket-chip bg-cobaltSoft text-cobalt">Live events</span>
        </div>
      </section>

      <section className="shell mt-10 grid gap-6 lg:grid-cols-3">
        <div className="surface p-6">
          <p className="eyebrow">01 · Explore</p>
          <h3 className="mt-2 font-display text-2xl font-bold text-ink">Find events you'll love.</h3>
          <p className="mt-3 text-sm leading-7 text-smoke">Search by category, venue, or keyword. Filter and sort to discover the perfect experience.</p>
        </div>
        <div className="surface p-6">
          <p className="eyebrow">02 · Select</p>
          <h3 className="mt-2 font-display text-2xl font-bold text-ink">Pick your perfect seats.</h3>
          <p className="mt-3 text-sm leading-7 text-smoke">Interactive seat maps show real-time availability. Choose exactly where you sit with price-aware filtering.</p>
        </div>
        <div className="surface p-6">
          <p className="eyebrow">03 · Book</p>
          <h3 className="mt-2 font-display text-2xl font-bold text-ink">Complete your purchase.</h3>
          <p className="mt-3 text-sm leading-7 text-smoke">Secure payment, instant confirmation, and all your tickets stored in one place for easy access.</p>
        </div>
      </section>

      <section className="shell mt-20">
        <SectionHeading
          eyebrow="Upcoming events"
          title="Discover what's next."
          description="These cards are pulled from our live event catalog. Fresh events are added daily, so there's always something exciting around the corner."
        />
        <div className="mt-8 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
          {events.slice(0, 6).map((event, index) => <EventCard key={event.id} event={event} index={index} />)}
        </div>
      </section>
    </div>
  );
}
