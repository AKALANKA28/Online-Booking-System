import { EventRecord, SeatRecord } from "@/lib/types";

const iso = (value: string) => new Date(value).toISOString();

export const MOCK_EVENTS: EventRecord[] = [
  {
    id: 9001,
    title: "Midnight Frequency Live",
    description: "A synth-pop arena show with immersive lighting, late-night energy, and premium floor access.",
    venue: "Lotus Ballroom",
    startsAt: iso("2026-05-18T19:30:00Z"),
    endsAt: iso("2026-05-18T22:15:00Z"),
    totalRows: 10,
    seatsPerRow: 12,
    vipRows: 3,
    vipPrice: 145,
    regularPrice: 68,
    status: "PUBLISHED",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 9002,
    title: "Colombo Design Futures Summit",
    description: "A premium conference for product leaders, founders, and experience designers shaping the next wave of digital experiences.",
    venue: "Harbor Forum",
    startsAt: iso("2026-06-04T08:00:00Z"),
    endsAt: iso("2026-06-04T17:00:00Z"),
    totalRows: 8,
    seatsPerRow: 10,
    vipRows: 2,
    vipPrice: 120,
    regularPrice: 45,
    status: "PUBLISHED",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 9003,
    title: "Sea Wall Cinema Under the Stars",
    description: "An open-air film night with ambient dining, waterfront views, and lounge seating tiers.",
    venue: "Galle Face Pavilion",
    startsAt: iso("2026-07-22T18:00:00Z"),
    endsAt: iso("2026-07-22T21:30:00Z"),
    totalRows: 6,
    seatsPerRow: 14,
    vipRows: 1,
    vipPrice: 88,
    regularPrice: 32,
    status: "PUBLISHED",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

export function buildMockSeatsForEvent(event: EventRecord): SeatRecord[] {
  const seats: SeatRecord[] = [];
  const letters = Array.from({ length: event.totalRows }, (_, index) => String.fromCharCode(65 + index));

  let counter = 1;
  letters.forEach((row, rowIndex) => {
    for (let seatNo = 1; seatNo <= event.seatsPerRow; seatNo += 1) {
      const category = rowIndex < event.vipRows ? "VIP" : "REGULAR";
      const price = category === "VIP" ? event.vipPrice : event.regularPrice;
      const booked = (seatNo + rowIndex) % 7 === 0;
      const reserved = !booked && (seatNo + rowIndex) % 9 === 0;
      seats.push({
        id: counter,
        eventId: event.id,
        seatNumber: `${row}${seatNo}`,
        category,
        price,
        status: booked ? "BOOKED" : reserved ? "RESERVED" : "AVAILABLE",
        lockedUntil: reserved ? new Date(Date.now() + 8 * 60 * 1000).toISOString() : null,
      });
      counter += 1;
    }
  });

  return seats;
}
