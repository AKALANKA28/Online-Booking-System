# Pulse Tickets Frontend

A modern, responsive ticketing platform for discovering and booking live events.

## Features

- Interactive seat selection with real-time availability
- Secure checkout with multiple payment methods
- User authentication and booking history
- Admin dashboard for event management
- Responsive design for all devices

## Tech Stack

- Next.js 16 with App Router
- TypeScript
- Tailwind CSS
- Custom component architecture

## Getting Started

### Prerequisites

- Node.js 18+
- The backend API running (see backend README)

### Installation

```bash
npm install
```

### Environment Variables

Create a `.env.local` file based on `.env.example` and set `API_BASE_URL` to your backend endpoint.

### Development

```bash
npm run dev
```

Visit `http://localhost:3000`.

### Build

```bash
npm run build
npm start
```

## Project Structure

- `app/` - Next.js pages (app router)
- `components/` - reusable UI components
- `lib/` - utility functions and API clients

## Pages

- `/` - Homepage with featured events
- `/events` - Browse and search events
- `/events/[id]` - Event details and seat selection
- `/login` - Sign in / register
- `/bookings` - My tickets and booking history
- `/admin/events` - Event management (admin only)

## Payment Integration

This demo uses a simple card token input. In production, integrate with a PCI-compliant payment processor.
