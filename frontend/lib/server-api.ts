import { buildMockSeatsForEvent, MOCK_EVENTS } from "@/lib/mock-data";
import { EventRecord, SeatRecord } from "@/lib/types";

const API_BASE_URL = process.env.API_BASE_URL || process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";

async function fetchJson<T>(path: string, timeoutMs = 2000): Promise<T> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);

  try {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      cache: "no-store",
      signal: controller.signal,
    });

    if (!response.ok) {
      throw new Error(`Request failed for ${path}: ${response.status}`);
    }

    return response.json() as Promise<T>;
  } finally {
    clearTimeout(timeout);
  }
}

export async function getPublicEvents() {
  try {
    return await fetchJson<EventRecord[]>("/api/events");
  } catch {
    return MOCK_EVENTS;
  }
}

export async function getPublicEventById(eventId: number) {
  try {
    return await fetchJson<EventRecord>(`/api/events/${eventId}`);
  } catch {
    return MOCK_EVENTS.find((event) => event.id === eventId) ?? null;
  }
}

export async function getPublicSeatsByEvent(eventId: number) {
  try {
    return await fetchJson<SeatRecord[]>(`/api/seats/events/${eventId}`);
  } catch {
    const event = MOCK_EVENTS.find((entry) => entry.id === eventId);
    return event ? buildMockSeatsForEvent(event) : [];
  }
}
