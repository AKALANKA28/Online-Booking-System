"use client";

import { useState } from "react";
import { EventRecord, SeatRecord } from "@/lib/types";
import { createBooking } from "@/lib/client-api";
import { formatMoney } from "@/lib/formatters";
import { useRouter } from "next/navigation";
import { useAuth } from "@/components/auth-provider";

interface EventBookingExperienceProps {
  event: EventRecord;
  seats: SeatRecord[];
}

export function EventBookingExperience({
  event,
  seats,
}: EventBookingExperienceProps) {
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
  const [isBooking, setIsBooking] = useState(false);
  const router = useRouter();
  const { user } = useAuth();

  const toggleSeat = (seatNumber: string) => {
    setSelectedSeats((prev) =>
      prev.includes(seatNumber)
        ? prev.filter((s) => s !== seatNumber)
        : [...prev, seatNumber],
    );
  };

  const selectedSeatObjects = seats.filter((seat) =>
    selectedSeats.includes(seat.seatNumber),
  );
  const totalAmount = selectedSeatObjects.reduce(
    (sum, seat) => sum + seat.price,
    0,
  );

  const handleBooking = async () => {
    if (selectedSeats.length === 0 || !user) return;

    setIsBooking(true);
    try {
      const booking = await createBooking(
        {
          eventId: event.id,
          seatNumbers: selectedSeats,
          paymentMethod: "CARD",
          cardToken: "4242424242424242",
        },
        user.accessToken,
      );

      router.push(`/bookings?reference=${booking.bookingReference}`);
    } catch (error) {
      console.error("Booking failed:", error);
      alert("Booking failed. Please try again.");
    } finally {
      setIsBooking(false);
    }
  };

  const renderSeats = () => {
    // Group seats by row
    const seatsByRow: Record<string, SeatRecord[]> = {};
    seats.forEach((seat) => {
      const row = seat.seatNumber.charAt(0);
      if (!seatsByRow[row]) seatsByRow[row] = [];
      seatsByRow[row].push(seat);
    });

    const rows = Object.keys(seatsByRow).sort();

    return rows.map((row) => (
      <div key={row} className="flex justify-center gap-2">
        {seatsByRow[row]
          .sort(
            (a, b) =>
              parseInt(a.seatNumber.slice(1)) - parseInt(b.seatNumber.slice(1)),
          )
          .map((seat) => {
            const isSelected = selectedSeats.includes(seat.seatNumber);
            const isAvailable = seat.status === "AVAILABLE";

            return (
              <button
                key={seat.seatNumber}
                onClick={() => isAvailable && toggleSeat(seat.seatNumber)}
                disabled={!isAvailable}
                className={`w-8 h-8 rounded border-2 text-xs font-semibold transition-colors ${
                  isSelected
                    ? "bg-cobalt border-cobalt text-white"
                    : isAvailable
                      ? "border-gray-300 hover:border-cobalt text-gray-700"
                      : "border-gray-200 bg-gray-100 text-gray-400 cursor-not-allowed"
                }`}
              >
                {seat.seatNumber.slice(1)}
              </button>
            );
          })}
      </div>
    ));
  };

  return (
    <div className="grid gap-8 lg:grid-cols-[1fr_300px]">
      <div>
        <h2 className="mb-6 font-display text-2xl font-bold">
          Select your seats
        </h2>
        <div className="space-y-4">
          <div className="flex justify-center">
            <div className="rounded-lg border border-line bg-white p-6">
              <div className="mb-4 text-center text-sm text-gray-600">
                Stage
              </div>
              <div className="space-y-2">{renderSeats()}</div>
            </div>
          </div>
        </div>
      </div>

      <div className="surface sticky top-6 h-fit p-6">
        <h3 className="font-display text-xl font-bold">Your selection</h3>
        {selectedSeats.length === 0 ? (
          <p className="mt-4 text-sm text-gray-600">No seats selected</p>
        ) : (
          <>
            <div className="mt-4 space-y-2">
              {selectedSeatObjects.map((seat) => (
                <div
                  key={seat.seatNumber}
                  className="flex justify-between text-sm"
                >
                  <span>
                    Seat {seat.seatNumber} ({seat.category})
                  </span>
                  <span>{formatMoney(seat.price)}</span>
                </div>
              ))}
            </div>
            <div className="mt-4 border-t pt-4">
              <div className="flex justify-between font-semibold">
                <span>Total</span>
                <span>{formatMoney(totalAmount)}</span>
              </div>
            </div>
            {user ? (
              <button
                onClick={handleBooking}
                disabled={isBooking}
                className="mt-6 w-full rounded-2xl bg-cobalt px-6 py-3 text-sm font-semibold text-white transition-colors hover:bg-cobalt/90 disabled:opacity-50"
              >
                {isBooking ? "Processing..." : "Book now"}
              </button>
            ) : (
              <div className="mt-6 text-center">
                <p className="text-sm text-gray-600 mb-4">
                  Please log in to book tickets
                </p>
                <button
                  onClick={() => router.push("/login")}
                  className="w-full rounded-2xl bg-cobalt px-6 py-3 text-sm font-semibold text-white transition-colors hover:bg-cobalt/90"
                >
                  Log in
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
