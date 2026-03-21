import { EventRecord, EventTheme, SeatRecord } from "@/lib/types";

const THEMES: EventTheme[] = [
  {
    name: "Cobalt Pulse",
    background: "linear-gradient(135deg, #0F1732 0%, #1A2B5F 42%, #2858FF 100%)",
    accent: "#2459FF",
    subtle: "#E7ECFF",
    glow: "rgba(36, 89, 255, 0.22)",
  },
  {
    name: "Ember Room",
    background: "linear-gradient(135deg, #221311 0%, #61241C 48%, #E86C4D 100%)",
    accent: "#E86C4D",
    subtle: "#FDE7E1",
    glow: "rgba(232, 108, 77, 0.22)",
  },
  {
    name: "Plum Night",
    background: "linear-gradient(135deg, #130E2E 0%, #31205D 48%, #7546F1 100%)",
    accent: "#7546F1",
    subtle: "#EFE8FF",
    glow: "rgba(117, 70, 241, 0.22)",
  },
  {
    name: "Forest House",
    background: "linear-gradient(135deg, #0E1E1A 0%, #144D3B 48%, #1E7855 100%)",
    accent: "#1E7855",
    subtle: "#DEF4EA",
    glow: "rgba(30, 120, 85, 0.22)",
  },
];

const CATEGORY_MAP: Array<{ label: string; matchers: string[] }> = [
  { label: "Concert", matchers: ["music", "live", "concert", "festival", "dj", "tour"] },
  { label: "Conference", matchers: ["summit", "expo", "conference", "tech", "design", "forum"] },
  { label: "Cinema", matchers: ["cinema", "film", "movie", "screening"] },
  { label: "Theatre", matchers: ["theatre", "theater", "play", "musical", "stage"] },
  { label: "Experience", matchers: ["immersive", "dining", "experience", "popup"] },
];

export function getCategory(event: EventRecord) {
  const haystack = `${event.title} ${event.description}`.toLowerCase();
  const match = CATEGORY_MAP.find((entry) => entry.matchers.some((keyword) => haystack.includes(keyword)));
  return match?.label ?? "Featured";
}

export function getEventTheme(event: EventRecord, index = 0) {
  return THEMES[(event.id + index) % THEMES.length];
}

export function availabilitySnapshot(seats: SeatRecord[]) {
  const available = seats.filter((seat) => seat.status === "AVAILABLE").length;
  const booked = seats.filter((seat) => seat.status === "BOOKED").length;
  return {
    available,
    booked,
    total: seats.length,
    sellThrough: seats.length === 0 ? 0 : Math.round((booked / seats.length) * 100),
  };
}

export function rowLabelFromSeat(seatNumber: string) {
  return seatNumber.match(/[A-Z]+/)?.[0] ?? "";
}

export function sortEvents(events: EventRecord[]) {
  return [...events].sort((a, b) => new Date(a.startsAt).getTime() - new Date(b.startsAt).getTime());
}
