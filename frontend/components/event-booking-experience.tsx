"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/components/auth-provider";
import { StatusPill } from "@/components/status-pill";
import { ApiError, createBooking, getSeatsByEvent } from "@/lib/client-api";
import {
  availabilitySnapshot,
  rowLabelFromSeat,
} from "@/lib/event-presentation";
import { formatDateRange, formatMoney, seatSort } from "@/lib/formatters";
import { BookingRecord, EventRecord, SeatRecord } from "@/lib/types";

function groupByRow(seats: SeatRecord[]) {
  return seats.reduce<Record<string, SeatRecord[]>>((accumulator, seat) => {
    const row = rowLabelFromSeat(seat.seatNumber);
    accumulator[row] = [...(accumulator[row] ?? []), seat].sort((left, right) =>
      seatSort(left.seatNumber, right.seatNumber),
    );
    return accumulator;
  }, {});
}

function feesFor(amount: number) {
  return Math.round(amount * 0.08);
}

function chooseTogether(seats: SeatRecord[], quantity: number) {
  const rows = Object.values(
    groupByRow(seats.filter((seat) => seat.status === "AVAILABLE")),
  );
  for (const row of rows) {
    for (let index = 0; index <= row.length - quantity; index += 1) {
      const slice = row.slice(index, index + quantity);
      const consecutive = slice.every((seat, seatIndex) => {
        if (seatIndex === 0) return true;
        const previous = Number(
          slice[seatIndex - 1].seatNumber.match(/\d+/)?.[0] ?? 0,
        );
        const current = Number(seat.seatNumber.match(/\d+/)?.[0] ?? 0);
        return current - previous === 1;
      });
      if (consecutive) {
        return slice.map((seat) => seat.seatNumber);
      }
    }
  }
  return [];
}

export function EventBookingExperience({
  event,
  initialSeats,
}: {
  event: EventRecord;
  initialSeats: SeatRecord[];
}) {
  const { user, hydrated } = useAuth();
  const router = useRouter();
  const [seats, setSeats] = useState(initialSeats);
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
  const [category, setCategory] = useState<"ALL" | "VIP" | "REGULAR">("ALL");
  const [priceCap, setPriceCap] = useState(
    Math.max(...initialSeats.map((seat) => seat.price), event.vipPrice),
  );
  const [paymentMethod, setPaymentMethod] = useState("CARD");
  const [cardToken, setCardToken] = useState("4242424242424242");
  const [submitting, setSubmitting] = useState(false);
  const [refreshingSeats, setRefreshingSeats] = useState(false);
  const [result, setResult] = useState<BookingRecord | null>(null);
  const [error, setError] = useState<string | null>(null);

  const visibleSeats = useMemo(
    () =>
      seats.filter((seat) => {
        const categoryMatch = category === "ALL" || seat.category === category;
        return categoryMatch && seat.price <= priceCap;
      }),
    [category, priceCap, seats],
  );

  const rows = useMemo(() => groupByRow(visibleSeats), [visibleSeats]);
  const selectedSeatRecords = useMemo(
    () =>
      seats
        .filter((seat) => selectedSeats.includes(seat.seatNumber))
        .sort((left, right) => seatSort(left.seatNumber, right.seatNumber)),
    [selectedSeats, seats],
  );
  const subtotal = selectedSeatRecords.reduce(
    (sum, seat) => sum + seat.price,
    0,
  );
  const fees = feesFor(subtotal);
  const total = subtotal + fees;
  const snapshot = availabilitySnapshot(seats);

  const refreshSeats = useCallback(
    async (silent = false) => {
      if (!silent) {
        setRefreshingSeats(true);
      }

      try {
        const latestSeats = await getSeatsByEvent(event.id);
        setSeats(latestSeats);
        if (latestSeats.length > 0) {
          setError(null);
        }
        return latestSeats;
      } catch (caught) {
        if (!silent) {
          if (caught instanceof ApiError) {
            setError(caught.message);
          } else {
            setError("Could not load seats right now. Please retry.");
          }
        }
        return [];
      } finally {
        if (!silent) {
          setRefreshingSeats(false);
        }
      }
    },
    [event.id],
  );

  useEffect(() => {
    if (seats.length > 0) {
      return;
    }

    let active = true;
    let timer: ReturnType<typeof setTimeout> | null = null;

    const retryLoad = async (attempt: number) => {
      if (!active || attempt >= 5) {
        return;
      }

      const latestSeats = await refreshSeats(true);
      if (!active || latestSeats.length > 0) {
        return;
      }

      timer = setTimeout(() => {
        void retryLoad(attempt + 1);
      }, 900);
    };

    void retryLoad(0);

    return () => {
      active = false;
      if (timer) {
        clearTimeout(timer);
      }
    };
  }, [refreshSeats, seats.length]);

  function toggleSeat(seat: SeatRecord) {
    if (seat.status !== "AVAILABLE" && !selectedSeats.includes(seat.seatNumber))
      return;
    setResult(null);
    setError(null);
    setSelectedSeats((current) =>
      current.includes(seat.seatNumber)
        ? current.filter((item) => item !== seat.seatNumber)
        : [...current, seat.seatNumber].sort(seatSort),
    );
  }

  function applyQuickPick(quantity: number) {
    const nextSelection = chooseTogether(visibleSeats, quantity);
    if (nextSelection.length) {
      setSelectedSeats(nextSelection);
      setError(null);
      setResult(null);
    } else {
      setError(
        `Could not find ${quantity} adjacent seats in the current filter.`,
      );
    }
  }

  async function reserveSeats() {
    if (!hydrated) return;
    if (!user) {
      router.push(`/login?next=/events/${event.id}`);
      return;
    }
    if (event.status === "CANCELLED") {
      setError("This event has been cancelled and is no longer bookable.");
      return;
    }
    if (!selectedSeats.length) {
      setError("Select at least one available seat before checkout.");
      return;
    }

    setSubmitting(true);
    setError(null);
    setResult(null);

    try {
      const booking = await createBooking(
        {
          eventId: event.id,
          seatNumbers: selectedSeats,
          paymentMethod,
          cardToken,
        },
        user.accessToken,
      );

      setResult(booking);
      if (booking.status === "CONFIRMED") {
        setSeats((current) =>
          current.map((seat) =>
            selectedSeats.includes(seat.seatNumber)
              ? { ...seat, status: "BOOKED" }
              : seat,
          ),
        );
        setSelectedSeats([]);
      }
    } catch (caught) {
      if (
        caught instanceof ApiError &&
        caught.status === 409 &&
        caught.payload &&
        typeof caught.payload === "object"
      ) {
        const payload = caught.payload as BookingRecord;
        setResult(payload);
        setSelectedSeats([]);
      } else if (caught instanceof ApiError) {
        setError(caught.message);
      } else {
        setError("Something went wrong while creating the booking.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="grid gap-8 xl:grid-cols-[1.45fr_0.85fr]">
      <div className="space-y-6">
        <div className="surface overflow-hidden p-6">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <p className="eyebrow">Seat selection</p>
              <h2 className="mt-2 font-display text-3xl font-bold text-ink">
                Pick your exact seats.
              </h2>
              <p className="mt-3 max-w-2xl text-sm leading-6 text-smoke">
                Inspired by the strongest ticketing flows: a clear interactive
                seat map, price-aware filters, and a sticky booking summary that
                keeps the purchase moving.
              </p>
            </div>
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              <div className="metric-card">
                <p className="text-xs uppercase tracking-[0.16em] text-smoke">
                  Available
                </p>
                <p className="mt-2 font-display text-2xl font-bold text-ink">
                  {snapshot.available}
                </p>
              </div>
              <div className="metric-card">
                <p className="text-xs uppercase tracking-[0.16em] text-smoke">
                  Sold
                </p>
                <p className="mt-2 font-display text-2xl font-bold text-ink">
                  {snapshot.booked}
                </p>
              </div>
              <div className="metric-card">
                <p className="text-xs uppercase tracking-[0.16em] text-smoke">
                  Sell-through
                </p>
                <p className="mt-2 font-display text-2xl font-bold text-ink">
                  {snapshot.sellThrough}%
                </p>
              </div>
              <div className="metric-card">
                <p className="text-xs uppercase tracking-[0.16em] text-smoke">
                  Session
                </p>
                <p className="mt-2 text-sm font-semibold text-ink">
                  {formatDateRange(event.startsAt, event.endsAt)}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="surface p-6">
          <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
            <div className="grid w-full grid-cols-3 gap-3 sm:max-w-sm">
              {(["ALL", "VIP", "REGULAR"] as const).map((item) => (
                <button
                  key={item}
                  type="button"
                  onClick={() => setCategory(item)}
                  className={
                    category === item
                      ? "w-full whitespace-nowrap rounded-full bg-ink px-4 py-3 text-center text-sm font-semibold text-white"
                      : "w-full whitespace-nowrap rounded-full border border-line bg-white px-4 py-3 text-center text-sm font-semibold text-ink"
                  }
                >
                  {item === "ALL" ? "All tickets" : item}
                </button>
              ))}
            </div>

            <div className="w-full max-w-md">
              <div className="flex items-center justify-between text-sm font-semibold text-smoke">
                <span>Price ceiling</span>
                <span>{formatMoney(priceCap)}</span>
              </div>
              <input
                type="range"
                min={Math.min(
                  ...seats.map((seat) => seat.price),
                  event.regularPrice,
                )}
                max={Math.max(
                  ...seats.map((seat) => seat.price),
                  event.vipPrice,
                )}
                value={priceCap}
                onChange={(eventRange) =>
                  setPriceCap(Number(eventRange.target.value))
                }
                className="mt-3 w-full accent-cobalt"
              />
            </div>
          </div>

          <div className="mt-6 flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => applyQuickPick(2)}
              className="ticket-chip"
            >
              Best 2 together
            </button>
            <button
              type="button"
              onClick={() => applyQuickPick(4)}
              className="ticket-chip"
            >
              Best 4 together
            </button>
            <span className="ticket-chip bg-cobaltSoft text-cobalt">
              VIP {formatMoney(event.vipPrice)}
            </span>
            <span className="ticket-chip bg-white">
              Regular {formatMoney(event.regularPrice)}
            </span>
          </div>
        </div>

        <div className="surface seat-grid overflow-hidden p-4 sm:p-6">
          <div className="mx-auto mb-8 w-full max-w-3xl rounded-[999px] border border-ink/10 bg-ink px-6 py-4 text-center font-display text-lg font-bold text-white shadow-panel">
            STAGE / MAIN VIEW
          </div>

          <div className="mb-6 flex flex-wrap gap-2 text-xs font-semibold uppercase tracking-[0.14em] text-smoke">
            <span className="ticket-chip bg-white text-ink">Available</span>
            <span className="ticket-chip bg-cobaltSoft text-cobalt">
              Selected
            </span>
            <span className="ticket-chip bg-goldSoft text-gold">Reserved</span>
            <span className="ticket-chip bg-ink/10 text-ink">Sold</span>
            <span className="ticket-chip bg-emberSoft text-ember">
              Cancelled
            </span>
            <span className="ticket-chip bg-plumSoft text-plum">VIP zone</span>
          </div>

          {Object.keys(rows).length > 0 ? (
            <div className="space-y-4 overflow-x-auto pb-2">
              {Object.entries(rows).map(([row, seatsInRow]) => (
                <div key={row} className="flex min-w-max items-center gap-3">
                  <div className="grid h-10 w-10 place-items-center rounded-full border border-line bg-white text-sm font-bold text-ink">
                    {row}
                  </div>
                  <div className="flex gap-2">
                    {seatsInRow.map((seat) => {
                      const selected = selectedSeats.includes(seat.seatNumber);
                      const disabled = seat.status !== "AVAILABLE" && !selected;
                      const appearance = selected
                        ? "border-cobalt bg-cobalt text-white"
                        : seat.status === "BOOKED"
                          ? "border-ink/10 bg-ink/10 text-ink/60"
                          : seat.status === "RESERVED"
                            ? "border-gold/30 bg-goldSoft text-gold"
                            : seat.status === "CANCELLED"
                              ? "border-ember/30 bg-emberSoft text-ember"
                              : seat.category === "VIP"
                                ? "border-plum/20 bg-plumSoft text-plum hover:border-plum"
                                : "border-line bg-white text-ink hover:border-cobalt";

                      return (
                        <button
                          key={seat.id}
                          type="button"
                          onClick={() => toggleSeat(seat)}
                          disabled={disabled}
                          suppressHydrationWarning
                          className={`grid h-12 w-12 place-items-center rounded-2xl border text-xs font-bold transition ${appearance} ${disabled ? "cursor-not-allowed opacity-70" : ""}`}
                          title={`${seat.seatNumber} · ${seat.category} · ${seat.status} · ${formatMoney(seat.price)}`}
                        >
                          {seat.seatNumber}
                        </button>
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="rounded-2xl border border-dashed border-line bg-cloud px-5 py-5 text-sm text-smoke">
              <p>
                Seat inventory is syncing. This can happen right after
                publishing an event while asynchronous seat generation
                completes.
              </p>
              <button
                type="button"
                onClick={() => {
                  void refreshSeats();
                }}
                disabled={refreshingSeats}
                className="mt-3 rounded-full border border-line bg-white px-4 py-2 text-xs font-semibold text-ink disabled:opacity-60"
              >
                {refreshingSeats ? "Refreshing seats..." : "Reload seats"}
              </button>
            </div>
          )}
        </div>
      </div>

      <aside className="space-y-6 xl:sticky xl:top-28 xl:self-start">
        <div className="surface overflow-hidden p-6">
          <p className="eyebrow">Checkout rail</p>
          <h3 className="mt-2 font-display text-2xl font-bold text-ink">
            Selected seats
          </h3>

          <div className="mt-5 space-y-4">
            {selectedSeatRecords.length ? (
              <div className="flex flex-wrap gap-2">
                {selectedSeatRecords.map((seat) => (
                  <span
                    key={seat.id}
                    className="ticket-chip bg-cobaltSoft text-cobalt"
                  >
                    {seat.seatNumber}
                  </span>
                ))}
              </div>
            ) : (
              <p className="rounded-2xl border border-dashed border-line bg-cloud px-4 py-4 text-sm leading-6 text-smoke">
                Tap seats on the map to start your order. The rail stays visible
                on larger screens so the action never feels lost.
              </p>
            )}

            <div className="rounded-3xl bg-cloud p-4">
              <div className="flex items-center justify-between py-2 text-sm text-smoke">
                <span>Subtotal</span>
                <span>{formatMoney(subtotal)}</span>
              </div>
              <div className="flex items-center justify-between py-2 text-sm text-smoke">
                <span>Service fee</span>
                <span>{formatMoney(fees)}</span>
              </div>
              <div className="mt-2 flex items-center justify-between border-t border-line pt-4 text-base font-bold text-ink">
                <span>Total</span>
                <span>{formatMoney(total)}</span>
              </div>
            </div>

            <label className="block">
              <span className="eyebrow">Payment method</span>
              <select
                value={paymentMethod}
                onChange={(eventSelect) =>
                  setPaymentMethod(eventSelect.target.value)
                }
                suppressHydrationWarning
                className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt"
              >
                <option value="CARD">Card</option>
                <option value="APPLE_PAY">Apple Pay (demo)</option>
                <option value="PAYPAL">PayPal (demo)</option>
              </select>
            </label>

            <label className="block">
              <span className="eyebrow">Demo card token</span>
              <input
                value={cardToken}
                onChange={(eventInput) => setCardToken(eventInput.target.value)}
                suppressHydrationWarning
                className="mt-3 w-full rounded-2xl border border-line bg-white px-4 py-4 text-sm outline-none focus:border-cobalt"
              />
              <p className="mt-2 text-xs text-smoke">
                Use{" "}
                <span className="font-semibold text-ink">4242424242424242</span>{" "}
                for success. A token ending in{" "}
                <span className="font-semibold text-ink">0000</span> simulates a
                failed payment in the Spring backend.
              </p>
            </label>

            <button
              type="button"
              onClick={reserveSeats}
              disabled={submitting}
              suppressHydrationWarning
              className="w-full rounded-full bg-ink px-5 py-4 text-sm font-semibold text-white transition hover:bg-cobalt disabled:cursor-not-allowed disabled:opacity-60"
            >
              {submitting
                ? "Processing booking..."
                : user
                  ? "Secure these tickets"
                  : "Sign in to continue"}
            </button>

            {!user && hydrated && (
              <p className="text-xs leading-6 text-smoke">
                Authentication is handled by your Spring API gateway. This
                front-end stores the JWT locally and forwards it through a
                Next.js proxy so you can avoid browser CORS issues during local
                development.
              </p>
            )}

            {error && (
              <p className="rounded-2xl border border-ember/20 bg-emberSoft px-4 py-3 text-sm font-medium text-ember">
                {error}
              </p>
            )}

            {result && (
              <div
                className={`rounded-3xl border px-4 py-4 text-sm leading-6 ${result.status === "CONFIRMED" ? "border-pine/20 bg-pineSoft text-pine" : "border-ember/20 bg-emberSoft text-ember"}`}
              >
                <p className="font-display text-xl font-bold">
                  {result.status === "CONFIRMED"
                    ? "Booking confirmed"
                    : "Booking failed"}
                </p>
                <p className="mt-2">
                  Reference:{" "}
                  <span className="font-semibold">
                    {result.bookingReference}
                  </span>
                </p>
                {result.paymentReference && (
                  <p>
                    Payment:{" "}
                    <span className="font-semibold">
                      {result.paymentReference}
                    </span>
                  </p>
                )}
                <p className="mt-2">
                  View the full payment and notification trail in the{" "}
                  <Link href="/bookings" className="font-semibold underline">
                    My tickets
                  </Link>{" "}
                  area.
                </p>
              </div>
            )}
          </div>
        </div>

        <div className="surface p-6">
          <p className="eyebrow">Why this layout works</p>
          <ul className="mt-4 space-y-3 text-sm leading-6 text-smoke">
            <li>
              • Exact-seat map with price-aware filtering keeps decision-making
              fast.
            </li>
            <li>
              • Sticky order rail preserves context instead of hiding the
              checkout entry point.
            </li>
            <li>
              • Quick picks support the “best 2 / best 4” behavior users expect
              from high-volume ticket sites.
            </li>
          </ul>
        </div>
      </aside>
    </section>
  );
}
