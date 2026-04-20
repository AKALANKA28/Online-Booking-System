"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "@/components/auth-provider";
import { StatusPill } from "@/components/status-pill";
import { ApiError, getMyBookings, getNotifications, getPayment } from "@/lib/client-api";
import { formatDateRange, formatMoney } from "@/lib/formatters";
import { BookingRecord, EventRecord, NotificationLogRecord, PaymentRecord } from "@/lib/types";

interface BookingDetails {
  payment?: PaymentRecord | null;
  notifications?: NotificationLogRecord[];
}

export function BookingsDashboard({ events }: { events: EventRecord[] }) {
  const { user, hydrated } = useAuth();
  const [bookings, setBookings] = useState<BookingRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [details, setDetails] = useState<Record<string, BookingDetails>>({});
  const [expanded, setExpanded] = useState<string | null>(null);

  const eventMap = useMemo(() => new Map(events.map((event) => [event.id, event])), [events]);

  useEffect(() => {
    if (!user) return;
    setLoading(true);
    setError(null);
    getMyBookings(user.accessToken)
      .then(setBookings)
      .catch((caught) => {
        if (caught instanceof ApiError) setError(caught.message);
        else setError("Could not load your bookings.");
      })
      .finally(() => setLoading(false));
  }, [user]);

  async function toggle(reference: string, paymentReference?: string | null) {
    const nextExpanded = expanded === reference ? null : reference;
    setExpanded(nextExpanded);
    if (nextExpanded !== reference || !user || details[reference]) return;

    try {
      const [payment, notifications] = await Promise.all([
        paymentReference ? getPayment(paymentReference, user.accessToken) : Promise.resolve(null),
        getNotifications(reference, user.accessToken),
      ]);
      setDetails((current) => ({ ...current, [reference]: { payment, notifications } }));
    } catch (caught) {
      const message = caught instanceof ApiError ? caught.message : "Could not load payment details.";
      setError(message);
    }
  }

  if (!hydrated) {
    return <div className="surface p-8 text-sm text-smoke">Preparing your session…</div>;
  }

  if (!user) {
    return (
      <div className="surface p-8">
        <h3 className="font-display text-2xl font-bold text-ink">Sign in to view your bookings</h3>
        <p className="mt-3 max-w-xl text-sm leading-6 text-smoke">This page calls <span className="font-semibold text-ink">/api/bookings/me</span> and then lazily loads payment plus notification history for each booking.</p>
        <Link href="/login?next=/bookings" className="mt-6 inline-flex rounded-full bg-ink px-5 py-3 text-sm font-semibold text-white">Go to login</Link>
      </div>
    );
  }

  return (
    <div className="space-y-5">
      {loading && <div className="surface p-6 text-sm text-smoke">Loading your confirmed and failed orders…</div>}
      {error && <div className="rounded-3xl border border-ember/20 bg-emberSoft px-5 py-4 text-sm font-semibold text-ember">{error}</div>}

      {!loading && bookings.length === 0 && (
        <div className="surface p-10 text-center">
          <h3 className="font-display text-2xl font-bold text-ink">No tickets yet</h3>
          <p className="mt-3 text-sm leading-6 text-smoke">Book a seat from the event detail page and it will appear here with payment and notification history.</p>
          <Link href="/events" className="mt-6 inline-flex rounded-full bg-ink px-5 py-3 text-sm font-semibold text-white">Browse events</Link>
        </div>
      )}

      {bookings.map((booking) => {
        const event = eventMap.get(booking.eventId);
        const detail = details[booking.bookingReference];
        const isExpanded = expanded === booking.bookingReference;
        return (
          <article key={booking.bookingReference} className="surface overflow-hidden p-6">
            <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <StatusPill value={booking.status} />
                  <span className="ticket-chip">{booking.bookingReference}</span>
                </div>
                <h3 className="mt-4 font-display text-3xl font-bold text-ink">{event?.title ?? `Event #${booking.eventId}`}</h3>
                <p className="mt-2 text-sm leading-6 text-smoke">{event ? `${event.venue} · ${formatDateRange(event.startsAt, event.endsAt)}` : booking.userEmail}</p>
                <div className="mt-4 flex flex-wrap gap-2">
                  {booking.items.map((item) => (
                    <span key={item.seatNumber} className="ticket-chip bg-cobaltSoft text-cobalt">{item.seatNumber} · {item.category}</span>
                  ))}
                </div>
              </div>

              <div className="rounded-3xl bg-cloud px-5 py-4 text-right">
                <p className="text-xs uppercase tracking-[0.16em] text-smoke">Order total</p>
                <p className="mt-2 font-display text-3xl font-bold text-ink">{formatMoney(booking.totalAmount)}</p>
                <button type="button" onClick={() => toggle(booking.bookingReference, booking.paymentReference)} className="mt-4 rounded-full border border-line bg-white px-4 py-2 text-sm font-semibold text-ink">
                  {isExpanded ? "Hide trail" : "View payment & notifications"}
                </button>
              </div>
            </div>

            {isExpanded && (
              <div className="mt-6 grid gap-4 lg:grid-cols-2">
                <div className="rounded-3xl border border-line bg-white p-5">
                  <p className="eyebrow">Payment</p>
                  {detail?.payment ? (
                    <div className="mt-4 space-y-3 text-sm text-smoke">
                      <div className="flex items-center justify-between"><span>Status</span><StatusPill value={detail.payment.status} /></div>
                      <div className="flex items-center justify-between"><span>Reference</span><span className="font-semibold text-ink">{detail.payment.paymentReference}</span></div>
                      <div className="flex items-center justify-between"><span>Method</span><span className="font-semibold text-ink">{detail.payment.paymentMethod}</span></div>
                      <div className="flex items-center justify-between"><span>Provider ref</span><span className="font-semibold text-ink">{detail.payment.providerReference}</span></div>
                    </div>
                  ) : (
                    <p className="mt-4 text-sm text-smoke">No payment reference was saved for this booking.</p>
                  )}
                </div>

                <div className="rounded-3xl border border-line bg-white p-5">
                  <p className="eyebrow">Notification trail</p>
                  <div className="mt-4 space-y-3">
                    {detail?.notifications?.length ? detail.notifications.map((notification) => (
                      <div key={notification.id} className="rounded-2xl border border-line bg-cloud px-4 py-4">
                        <div className="flex items-center justify-between gap-4">
                          <div>
                            <p className="text-sm font-semibold text-ink">{notification.subject}</p>
                            <p className="mt-1 text-xs uppercase tracking-[0.16em] text-smoke">{notification.channel} · {notification.recipient}</p>
                          </div>
                          <StatusPill value={notification.status} />
                        </div>
                        <p className="mt-3 text-sm leading-6 text-smoke">{notification.message}</p>
                      </div>
                    )) : <p className="text-sm text-smoke">No notification records yet.</p>}
                  </div>
                </div>
              </div>
            )}
          </article>
        );
      })}
    </div>
  );
}
