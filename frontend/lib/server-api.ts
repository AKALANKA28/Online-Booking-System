import { EventRecord, SeatRecord } from "@/lib/types";

const API_BASE_URL =
  process.env.API_BASE_URL ||
  process.env.NEXT_PUBLIC_API_BASE_URL ||
  "http://localhost:8080";

type FetchJsonOptions = {
  timeoutMs?: number;
  retries?: number;
  retryDelayMs?: number;
};

function sleep(milliseconds: number) {
  return new Promise<void>((resolve) => {
    setTimeout(resolve, milliseconds);
  });
}

async function fetchJson<T>(
  path: string,
  options: FetchJsonOptions = {},
): Promise<T> {
  const { timeoutMs = 6000, retries = 0, retryDelayMs = 400 } = options;

  let lastError: unknown;

  for (let attempt = 0; attempt <= retries; attempt += 1) {
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
    } catch (error) {
      lastError = error;
      if (attempt < retries) {
        await sleep(retryDelayMs * (attempt + 1));
      }
    } finally {
      clearTimeout(timeout);
    }
  }

  throw lastError ?? new Error(`Request failed for ${path}`);
}

export async function getPublicEvents() {
  try {
    return await fetchJson<EventRecord[]>("/api/events", {
      timeoutMs: 6000,
      retries: 1,
    });
  } catch {
    return [];
  }
}

export async function getPublicEventById(eventId: number) {
  try {
    return await fetchJson<EventRecord>(`/api/events/${eventId}`, {
      timeoutMs: 6000,
      retries: 1,
    });
  } catch {
    return null;
  }
}

export async function getPublicSeatsByEvent(eventId: number) {
  try {
    let seats = await fetchJson<SeatRecord[]>(`/api/seats/events/${eventId}`, {
      timeoutMs: 6000,
      retries: 2,
      retryDelayMs: 500,
    });

    // Seats are generated asynchronously after event creation, so retry briefly when empty.
    for (let attempt = 0; seats.length === 0 && attempt < 3; attempt += 1) {
      await sleep(700);
      seats = await fetchJson<SeatRecord[]>(`/api/seats/events/${eventId}`, {
        timeoutMs: 6000,
      });
    }

    return seats;
  } catch {
    return [];
  }
}
