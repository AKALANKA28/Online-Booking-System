export type EventStatus = "DRAFT" | "PUBLISHED" | "CANCELLED";
export type SeatStatus = "AVAILABLE" | "RESERVED" | "BOOKED";
export type BookingStatus = "PENDING_PAYMENT" | "CONFIRMED" | "FAILED" | "CANCELLED";
export type PaymentStatus = "SUCCESS" | "FAILED";
export type NotificationStatus = "SENT" | "FAILED";
export type UserRole = "ADMIN" | "CUSTOMER";

export interface EventRecord {
  id: number;
  title: string;
  description: string;
  venue: string;
  startsAt: string;
  endsAt: string;
  totalRows: number;
  seatsPerRow: number;
  vipRows: number;
  vipPrice: number;
  regularPrice: number;
  status: EventStatus;
  createdAt: string;
  updatedAt: string;
}

export interface SeatRecord {
  id: number;
  eventId: number;
  seatNumber: string;
  category: "VIP" | "REGULAR";
  price: number;
  status: SeatStatus;
  lockedUntil?: string | null;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  userId: string;
  email: string;
  role: UserRole;
}

export interface CreateBookingRequest {
  eventId: number;
  seatNumbers: string[];
  paymentMethod: string;
  cardToken: string;
}

export interface BookingItemRecord {
  seatNumber: string;
  category: "VIP" | "REGULAR";
  price: number;
}

export interface BookingRecord {
  bookingReference: string;
  eventId: number;
  userId: string;
  userEmail: string;
  totalAmount: number;
  status: BookingStatus;
  paymentReference?: string | null;
  createdAt: string;
  updatedAt: string;
  items: BookingItemRecord[];
}

export interface PaymentRecord {
  paymentReference: string;
  bookingReference: string;
  amount: number;
  paymentMethod: string;
  providerReference: string;
  status: PaymentStatus;
  userEmail: string;
  createdAt: string;
}

export interface NotificationLogRecord {
  id: number;
  bookingReference: string;
  recipient: string;
  channel: string;
  subject: string;
  message: string;
  status: NotificationStatus;
  createdAt: string;
}

export interface SessionUser {
  accessToken: string;
  userId: string;
  email: string;
  role: UserRole;
  expiresAt: string;
}

export interface EventTheme {
  name: string;
  background: string;
  accent: string;
  subtle: string;
  glow: string;
}
