# Frontend Process

## Product flow covered by the UI

### 1. Discovery
Users land on a premium homepage with a featured event block, category rails, and upcoming cards.

### 2. Browsing
Users filter the event catalog by title, venue, category, publish status, and sort order.

### 3. Event detail
The event page exposes:
- venue and timing
- pricing window
- seat footprint
- live seat status

### 4. Seat selection
The seat map supports:
- exact seat taps
- VIP / regular filtering
- price ceiling filter
- quick picks for adjacent seats
- sticky checkout rail

### 5. Authentication
Users sign in with JWT through the API gateway.

### 6. Booking
The frontend posts bookings to the backend and immediately reflects:
- confirmed orders
- failed orders
- payment references

### 7. Account center
Users can review booking history, payment status, and notification logs.

### 8. Organizer publishing
Admins can create events from the web UI instead of using Swagger in the live demo.

## Design principles used

- discovery-first layout
- strong typography over decorative noise
- premium but restrained color system
- clear conversion path from event page to checkout
- desktop sticky booking rail, mobile-first stacking
- no dependency on a generic component library

## Responsive strategy

- stacked sections on mobile
- card grids expand at tablet and desktop breakpoints
- seat map scrolls horizontally if needed on narrow screens
- checkout rail becomes sticky on large screens only
- header collapses into a compact mobile menu
