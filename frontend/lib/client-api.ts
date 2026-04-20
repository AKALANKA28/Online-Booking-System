import {
  BookingRecord,
  CreateBookingRequest,
  EventRecord,
  LoginRequest,
  LoginResponse,
  NotificationLogRecord,
  PaymentRecord,
  RegisterRequest,
  RegisteredUserResponse,
  SeatRecord,
} from "@/lib/types";

export class ApiError extends Error {
  status: number;
  payload?: unknown;

  constructor(message: string, status: number, payload?: unknown) {
    super(message);
    this.status = status;
    this.payload = payload;
  }
}

async function parseBody(response: Response) {
  const contentType = response.headers.get("content-type") || "";
  if (response.status === 204) return undefined;
  if (contentType.includes("application/json")) {
    return response.json();
  }
  return response.text();
}

async function request<T>(
  path: string,
  options: RequestInit = {},
  token?: string,
) {
  const headers = new Headers(options.headers);
  if (!headers.has("Content-Type") && options.body) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`/api/proxy${path}`, {
    ...options,
    headers,
    cache: "no-store",
  });

  const body = await parseBody(response);

  if (!response.ok) {
    const message =
      typeof body === "string"
        ? body || `Request failed (${response.status})`
        : body &&
            typeof body === "object" &&
            "message" in body &&
            typeof body.message === "string"
          ? body.message
          : `Request failed (${response.status})`;

    throw new ApiError(message, response.status, body);
  }

  return body as T;
}

export function login(payload: LoginRequest) {
  return request<LoginResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function registerUser(payload: RegisterRequest) {
  return request<RegisteredUserResponse>("/api/users/register", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function createBooking(payload: CreateBookingRequest, token: string) {
  return request<BookingRecord>(
    "/api/bookings",
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    token,
  );
}

export function getMyBookings(token: string) {
  return request<BookingRecord[]>("/api/bookings/me", {}, token);
}

export function getPayment(paymentReference: string, token: string) {
  return request<PaymentRecord>(`/api/payments/${paymentReference}`, {}, token);
}

export function getNotifications(bookingReference: string, token: string) {
  return request<NotificationLogRecord[]>(
    `/api/notifications/bookings/${bookingReference}`,
    {},
    token,
  );
}

export function createEvent(payload: Partial<EventRecord>, token: string) {
  return request<EventRecord>(
    "/api/events",
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    token,
  );
}

export function getEvents() {
  return request<EventRecord[]>("/api/events");
}

export function updateEventStatus(
  eventId: number,
  status: EventRecord["status"],
  token: string,
) {
  return request<EventRecord>(
    `/api/events/${eventId}/status`,
    {
      method: "PUT",
      body: JSON.stringify({ status }),
    },
    token,
  );
}

export function deleteEvent(eventId: number, token: string) {
  return request<void>(
    `/api/events/${eventId}`,
    {
      method: "DELETE",
    },
    token,
  );
}

export function getSeatsByEvent(eventId: number) {
  return request<SeatRecord[]>(`/api/seats/events/${eventId}`);
}
