# Pulse Tickets Frontend

A modern, responsive web UI for the **Smart Event Ticketing and Reservation Platform** backend.

## Why this stack

### Next.js App Router
This project uses **Next.js + TypeScript** because the product has two very different UI needs:
- **discovery pages** benefit from server-rendered content and clean routing
- **seat selection, authentication, bookings, and organizer actions** require rich client-side interactivity

That makes the App Router a strong fit for a ticketing experience.

### Next.js proxy route
Your Spring gateway does not currently expose browser CORS configuration. To keep local development clean, the frontend uses a **same-origin Next.js proxy** under `/api/proxy/...`, which forwards requests to the Spring API gateway.

This means the browser talks only to the Next.js app, while Next.js forwards requests to `http://localhost:8080` (or whatever you put in `API_BASE_URL`).

### Tailwind CSS with a custom design system
Tailwind is used only as a utility layer. There is **no prebuilt component kit** here, because those often produce the generic AI-looking UI you explicitly wanted to avoid.

The visual language is intentionally more editorial and product-led:
- warm paper background instead of generic dark dashboard chrome
- strong typographic hierarchy
- asymmetric hero composition
- premium seat-selection layout with sticky purchase rail
- restrained accents instead of random rainbow gradients

## Main pages

- `/` - discovery-led homepage
- `/events` - search and browse
- `/events/[id]` - event detail + seat selection + checkout rail
- `/login` - JWT login against Spring gateway demo users
- `/bookings` - booking history with payment and notification trail
- `/admin/events` - admin event publishing UI

## Works with your existing backend

Frontend endpoint mapping:
- `POST /auth/login`
- `GET /auth/users`
- `GET /api/events`
- `GET /api/events/{id}`
- `GET /api/seats/events/{eventId}`
- `POST /api/bookings`
- `GET /api/bookings/me`
- `GET /api/payments/{paymentReference}`
- `GET /api/notifications/bookings/{bookingReference}`
- `POST /api/events` (admin)

## Local setup

### 1. Install dependencies
```bash
npm install
```

### 2. Copy env file
```bash
cp .env.example .env.local
```

### 3. Set backend base URL
By default:
```env
API_BASE_URL=http://localhost:8080
```

### 4. Start the frontend
```bash
npm run dev
```

Frontend runs at:
```text
http://localhost:3000
```

## Backend assumptions

This UI assumes:
- Spring API Gateway is running on `localhost:8080`
- your event, seat, booking, and payment-notification services are live behind it
- RabbitMQ is running if you want event-created seat generation and notification flow to behave fully

## Demo credentials

- `admin / admin123`
- `customer / customer123`
- `user2 / user2123`

## Booking testing

On the event detail page:
- use card token `4242424242424242` for a success path
- use a token ending in `0000` for a simulated payment failure path

## Fallback behavior

If your backend is not reachable, the homepage and event catalog fall back to curated mock events so you can still present the UI during design review. Once the backend is up, the live API data takes over.
