"use client";

import { useMemo, useState } from "react";
import { EventCard } from "@/components/event-card";
import { getCategory } from "@/lib/event-presentation";
import { EventRecord } from "@/lib/types";

const SORTS = {
  soonest: "Soonest",
  premium: "Highest price",
  value: "Lowest price",
} as const;

export function EventCatalogue({ initialEvents }: { initialEvents: EventRecord[] }) {
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("All");
  const [sort, setSort] = useState<keyof typeof SORTS>("soonest");
  const [publishedOnly, setPublishedOnly] = useState(true);

  const categories = useMemo(() => ["All", ...Array.from(new Set(initialEvents.map((event) => getCategory(event))))], [initialEvents]);

  const events = useMemo(() => {
    const filtered = initialEvents.filter((event) => {
      const matchesQuery = `${event.title} ${event.description} ${event.venue}`.toLowerCase().includes(query.toLowerCase());
      const matchesCategory = category === "All" || getCategory(event) === category;
      const matchesStatus = !publishedOnly || event.status === "PUBLISHED";
      return matchesQuery && matchesCategory && matchesStatus;
    });

    return filtered.sort((left, right) => {
      if (sort === "premium") return right.vipPrice - left.vipPrice;
      if (sort === "value") return left.regularPrice - right.regularPrice;
      return new Date(left.startsAt).getTime() - new Date(right.startsAt).getTime();
    });
  }, [category, initialEvents, publishedOnly, query, sort]);

  return (
    <div className="space-y-8">
      <div className="surface grid gap-5 p-5 lg:grid-cols-[1.4fr_auto_auto] lg:items-center">
        <label className="block">
          <span className="eyebrow">Search title, venue, vibe</span>
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search concerts, conferences, theatres..."
            className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none ring-0 transition focus:border-cobalt"
          />
        </label>

        <label>
          <span className="eyebrow">Sort</span>
          <select
            value={sort}
            onChange={(event) => setSort(event.target.value as keyof typeof SORTS)}
            className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt"
          >
            {Object.entries(SORTS).map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
        </label>

        <label className="flex items-center gap-3 rounded-2xl border border-line bg-white px-4 py-4 text-sm font-semibold text-ink">
          <input
            type="checkbox"
            checked={publishedOnly}
            onChange={(event) => setPublishedOnly(event.target.checked)}
            className="h-4 w-4 rounded border-line text-cobalt focus:ring-cobalt"
          />
          Published only
        </label>
      </div>

      <div className="flex flex-wrap gap-2">
        {categories.map((item) => {
          const active = category === item;
          return (
            <button
              key={item}
              type="button"
              onClick={() => setCategory(item)}
              className={active
                ? "rounded-full bg-ink px-4 py-2 text-sm font-semibold text-white"
                : "rounded-full border border-line bg-white px-4 py-2 text-sm font-semibold text-ink transition hover:border-ink"}
            >
              {item}
            </button>
          );
        })}
      </div>

      <div className="flex items-center justify-between gap-4 text-sm text-smoke">
        <p>{events.length} events available</p>
      </div>

      {events.length > 0 ? (
        <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
          {events.map((event, index) => <EventCard key={event.id} event={event} index={index} />)}
        </div>
      ) : (
         <div className="surface p-10 text-center">
           <p className="font-display text-2xl font-bold text-ink">No events match those filters yet.</p>
           <p className="mt-3 text-sm leading-6 text-smoke">Try broadening your search or adjusting filters to find what you're looking for.</p>
         </div>
      )}
    </div>
  );
}
