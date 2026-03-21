"use client";

import Link from "next/link";
import { useState } from "react";
import { useAuth } from "@/components/auth-provider";
import { ApiError, createEvent } from "@/lib/client-api";
import { EventRecord } from "@/lib/types";

const defaultState = {
  title: "Colombo Spotlight Sessions",
  description: "A polished live-session format with intimate seating, premium front rows, and quick digital ticket delivery.",
  venue: "Independence Hall",
  startsAt: "2026-08-20T18:30",
  endsAt: "2026-08-20T22:00",
  totalRows: 8,
  seatsPerRow: 10,
  vipRows: 2,
  vipPrice: 140,
  regularPrice: 55,
};

export function AdminEventStudio() {
  const { user } = useAuth();
  const [form, setForm] = useState(defaultState);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [created, setCreated] = useState<EventRecord | null>(null);

  function patch<K extends keyof typeof defaultState>(key: K, value: (typeof defaultState)[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function onSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!user || user.role !== "ADMIN") return;

    setLoading(true);
    setError(null);
    setCreated(null);

    try {
      const response = await createEvent({
        ...form,
        startsAt: new Date(form.startsAt).toISOString(),
        endsAt: new Date(form.endsAt).toISOString(),
      }, user.accessToken);
      setCreated(response);
    } catch (caught) {
      if (caught instanceof ApiError) setError(caught.message);
      else setError("Could not create the event. Confirm the gateway and RabbitMQ-backed services are running.");
    } finally {
      setLoading(false);
    }
  }

  if (!user) {
    return (
      <div className="surface p-8">
        <h3 className="font-display text-2xl font-bold text-ink">Admin sign-in required</h3>
        <p className="mt-3 max-w-xl text-sm leading-6 text-smoke">This page posts directly to <span className="font-semibold text-ink">/api/events</span>, which your gateway protects for admin users only.</p>
        <Link href="/login?next=/admin/events" className="mt-6 inline-flex rounded-full bg-ink px-5 py-3 text-sm font-semibold text-white">Sign in as admin</Link>
      </div>
    );
  }

  if (user.role !== "ADMIN") {
    return <div className="surface p-8 text-sm font-semibold text-ember">You are signed in, but not as an admin. Use the admin demo account to publish events.</div>;
  }

  return (
    <div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
      <form onSubmit={onSubmit} className="surface p-8">
        <p className="eyebrow">Organizer studio</p>
        <h3 className="mt-2 font-display text-3xl font-bold text-ink">Create an event that instantly feeds the seat service.</h3>
        <p className="mt-3 text-sm leading-6 text-smoke">Publishing through this screen gives you a complete demo: event created in the event service, seats generated asynchronously, then available for booking in the customer flow.</p>

        <div className="mt-8 grid gap-5 md:grid-cols-2">
          <label className="block md:col-span-2">
            <span className="eyebrow">Title</span>
            <input value={form.title} onChange={(event) => patch("title", event.target.value)} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <label className="block md:col-span-2">
            <span className="eyebrow">Description</span>
            <textarea value={form.description} onChange={(event) => patch("description", event.target.value)} rows={5} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <label className="block">
            <span className="eyebrow">Venue</span>
            <input value={form.venue} onChange={(event) => patch("venue", event.target.value)} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <label className="block">
            <span className="eyebrow">VIP rows</span>
            <input type="number" value={form.vipRows} onChange={(event) => patch("vipRows", Number(event.target.value))} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <label className="block">
            <span className="eyebrow">Starts</span>
            <input type="datetime-local" value={form.startsAt} onChange={(event) => patch("startsAt", event.target.value)} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <label className="block">
            <span className="eyebrow">Ends</span>
            <input type="datetime-local" value={form.endsAt} onChange={(event) => patch("endsAt", event.target.value)} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <label className="block">
            <span className="eyebrow">Total rows</span>
            <input type="number" value={form.totalRows} onChange={(event) => patch("totalRows", Number(event.target.value))} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <label className="block">
            <span className="eyebrow">Seats per row</span>
            <input type="number" value={form.seatsPerRow} onChange={(event) => patch("seatsPerRow", Number(event.target.value))} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <label className="block">
            <span className="eyebrow">VIP price</span>
            <input type="number" value={form.vipPrice} onChange={(event) => patch("vipPrice", Number(event.target.value))} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>

          <label className="block">
            <span className="eyebrow">Regular price</span>
            <input type="number" value={form.regularPrice} onChange={(event) => patch("regularPrice", Number(event.target.value))} className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt" />
          </label>
        </div>

        <button type="submit" disabled={loading} className="mt-8 rounded-full bg-ink px-5 py-4 text-sm font-semibold text-white transition hover:bg-cobalt disabled:opacity-70">
          {loading ? "Publishing event..." : "Publish event"}
        </button>

        {error && <p className="mt-4 rounded-2xl border border-ember/20 bg-emberSoft px-4 py-3 text-sm font-medium text-ember">{error}</p>}
      </form>

      <div className="space-y-6">
        <div className="surface-dark p-8" style={{ backgroundImage: "linear-gradient(135deg, #130E2E 0%, #31205D 48%, #7546F1 100%)" }}>
          <p className="eyebrow text-white/70">Why this matters</p>
          <h4 className="mt-2 font-display text-3xl font-bold text-white">This gives you a complete admin-to-customer story.</h4>
          <ul className="mt-5 space-y-3 text-sm leading-6 text-white/80">
            <li>• Admin creates the event in the event service.</li>
            <li>• Seat service consumes the event-created message and generates inventory.</li>
            <li>• Customers immediately see the event and book through the same web UI.</li>
          </ul>
        </div>

        {created && (
          <div className="surface p-6">
            <p className="eyebrow">Created successfully</p>
            <h4 className="mt-2 font-display text-2xl font-bold text-ink">{created.title}</h4>
            <p className="mt-3 text-sm leading-6 text-smoke">Event #{created.id} is live. Give the seat service a few seconds to create the seat map, then open the event detail page.</p>
            <div className="mt-5 flex flex-wrap gap-3">
              <Link href={`/events/${created.id}`} className="rounded-full bg-ink px-5 py-3 text-sm font-semibold text-white">Open event page</Link>
              <Link href="/events" className="rounded-full border border-line px-5 py-3 text-sm font-semibold text-ink">View all events</Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
