import { notFound } from "next/navigation";
import { EventBookingExperience } from "@/components/event-booking-experience";
import { StatusPill } from "@/components/status-pill";
import {
  availabilitySnapshot,
  getCategory,
  getEventTheme,
} from "@/lib/event-presentation";
import { formatDateRange, formatMoney } from "@/lib/formatters";
import { getPublicEventById, getPublicSeatsByEvent } from "@/lib/server-api";

export default async function EventDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const eventId = Number(id);
  const event = await getPublicEventById(eventId);

  if (!event) {
    notFound();
  }

  const seats = await getPublicSeatsByEvent(eventId);
  const theme = getEventTheme(event, 0);
  const snapshot = availabilitySnapshot(seats);

  return (
    <div className="pb-12">
      <section className="shell pt-10 sm:pt-14">
        <div
          className="surface-dark overflow-hidden p-8 sm:p-10"
          style={{ backgroundImage: theme.background }}
        >
          <div className="flex flex-wrap items-center gap-3">
            <span className="ticket-chip border-white/15 bg-white/10 text-white">
              {getCategory(event)}
            </span>
            <StatusPill value={event.status} />
          </div>
          <div className="mt-8 grid gap-6 xl:grid-cols-[1.15fr_0.85fr] xl:items-end">
            <div>
              <h1 className="max-w-4xl font-display text-4xl font-bold leading-tight text-white sm:text-6xl">
                {event.title}
              </h1>
              <p className="mt-4 max-w-3xl text-sm leading-7 text-white/80">
                {event.description}
              </p>
            </div>
            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
              <div className="rounded-3xl border border-white/12 bg-white/10 px-5 py-4 text-white">
                <p className="text-xs uppercase tracking-[0.16em] text-white/60">
                  Venue & time
                </p>
                <p className="mt-2 text-sm font-semibold">{event.venue}</p>
                <p className="mt-1 text-sm text-white/70">
                  {formatDateRange(event.startsAt, event.endsAt)}
                </p>
              </div>
              <div className="rounded-3xl border border-white/12 bg-white/10 px-5 py-4 text-white">
                <p className="text-xs uppercase tracking-[0.16em] text-white/60">
                  Pricing window
                </p>
                <p className="mt-2 text-sm font-semibold">
                  Regular {formatMoney(event.regularPrice)}
                </p>
                <p className="mt-1 text-sm text-white/70">
                  VIP {formatMoney(event.vipPrice)} · {snapshot.available} seats
                  still open
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="shell mt-8">
        <div className="grid gap-4 md:grid-cols-3">
          <div className="metric-card">
            <p className="text-xs uppercase tracking-[0.16em] text-smoke">
              Available now
            </p>
            <p className="mt-3 font-display text-4xl font-bold text-ink">
              {snapshot.available}
            </p>
          </div>
          <div className="metric-card">
            <p className="text-xs uppercase tracking-[0.16em] text-smoke">
              Seat footprint
            </p>
            <p className="mt-3 font-display text-4xl font-bold text-ink">
              {event.totalRows * event.seatsPerRow}
            </p>
          </div>
          <div className="metric-card">
            <p className="text-xs uppercase tracking-[0.16em] text-smoke">
              Seat mix
            </p>
            <p className="mt-3 font-display text-2xl font-bold text-ink">
              {event.vipRows} VIP rows / {event.totalRows - event.vipRows}{" "}
              regular
            </p>
          </div>
        </div>
      </section>

      <section className="shell mt-10">
        <EventBookingExperience event={event} seats={seats} />
      </section>
    </div>
  );
}
