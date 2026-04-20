import Link from "next/link";
import { formatDateRange, formatMoney } from "@/lib/formatters";
import { getCategory, getEventTheme } from "@/lib/event-presentation";
import { EventRecord } from "@/lib/types";
import { StatusPill } from "@/components/status-pill";

export function EventCard({ event, index = 0 }: { event: EventRecord; index?: number }) {
  const theme = getEventTheme(event, index);
  const category = getCategory(event);

  return (
    <Link href={`/events/${event.id}`} className="group block h-full">
      <article className="surface ticket-divider flex h-full flex-col overflow-hidden transition duration-300 hover:-translate-y-1 hover:shadow-panel">
        <div className="p-4">
          <div className="surface-dark min-h-[180px] overflow-hidden p-6" style={{ backgroundImage: theme.background, boxShadow: `0 24px 48px ${theme.glow}` }}>
            <div className="flex items-start justify-between gap-4">
              <div className="ticket-chip border-white/15 bg-white/10 text-white">{category}</div>
              <StatusPill value={event.status} />
            </div>
            <div className="mt-10">
              <p className="text-sm uppercase tracking-[0.2em] text-white/70">{event.venue}</p>
              <h3 className="mt-3 font-display text-3xl font-bold leading-tight text-white">{event.title}</h3>
            </div>
          </div>
        </div>
        <div className="flex flex-1 flex-col gap-4 px-6 pb-6 pt-1">
          <p className="line-clamp-3 text-sm leading-6 text-smoke">{event.description}</p>
          <div className="mt-auto flex items-end justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-smoke">Starts from</p>
              <p className="mt-2 font-display text-2xl font-bold text-ink">{formatMoney(event.regularPrice)}</p>
            </div>
            <div className="text-right text-sm text-smoke">
              <p>{formatDateRange(event.startsAt, event.endsAt)}</p>
              <p className="mt-1 font-semibold text-ink group-hover:text-cobalt">Open event →</p>
            </div>
          </div>
        </div>
      </article>
    </Link>
  );
}
