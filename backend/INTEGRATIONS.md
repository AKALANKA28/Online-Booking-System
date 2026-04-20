# Stripe, SendGrid, and Twilio integrations

The **payment-notification-service** can use:

- **Stripe (test mode)** — real PaymentIntents against Stripe test API when `STRIPE_SECRET_KEY` is set; otherwise the original in-memory **simulator** is used.
- **SendGrid** — sends booking confirmation/failure **email** when `SENDGRID_API_KEY` and `SENDGRID_FROM_EMAIL` are set.
- **Twilio** — sends **SMS** when `TWILIO_*` variables are set **and** the booking has a **user phone** from the JWT / **`X-User-Phone`** (demo users each have a fixed E.164 number; change them in `DemoUserService` to match **Twilio-verified** numbers on trial accounts).

## Prerequisites

### Stripe

1. [Stripe Dashboard](https://dashboard.stripe.com) → **Developers** → **API keys** → copy **Secret key** (`sk_test_…`).
2. Optional **webhooks** (for async updates on the same payment record):
   - Install [Stripe CLI](https://stripe.com/docs/stripe-cli).
   - Local: `stripe listen --forward-to localhost:8084/webhooks/stripe` (or `http://localhost:8080/webhooks/stripe` through the gateway).
   - Copy the **webhook signing secret** (`whsec_…`) into `STRIPE_WEBHOOK_SECRET`.
   - Deployed: add endpoint `https://<your-public-host>/webhooks/stripe` and paste the dashboard signing secret.

Without `STRIPE_WEBHOOK_SECRET`, the endpoint returns **404** and Stripe CLI/dashboard should not point at it.

### SendGrid (email)

1. Create a [Twilio SendGrid](https://sendgrid.com) API key with **Mail Send** permission.
2. Complete **Single Sender Verification** (or domain auth) and set `SENDGRID_FROM_EMAIL` to that sender.

### Twilio (SMS)

1. [Twilio Console](https://console.twilio.com/) → **Account SID** and **Auth Token**.
2. **From** number: use a Twilio phone number in E.164 (`TWILIO_FROM_NUMBER`).
3. **Trial accounts** can only SMS **verified** destination numbers — verify the customer’s phone in the Twilio console or upgrade the account.

## Configuration (environment variables)

| Variable | Purpose |
|----------|---------|
| `STRIPE_SECRET_KEY` | `sk_test_…` — enables Stripe instead of simulator |
| `STRIPE_WEBHOOK_SECRET` | `whsec_…` — enables `POST /webhooks/stripe` |
| `STRIPE_CURRENCY` | Default `usd` (Stripe smallest unit = cents) |
| `SENDGRID_API_KEY` | SendGrid API key |
| `SENDGRID_FROM_EMAIL` | Verified sender email |
| `SENDGRID_FROM_NAME` | Display name (optional) |
| `TWILIO_ACCOUNT_SID` | Twilio Account SID |
| `TWILIO_AUTH_TOKEN` | Twilio Auth Token |
| `TWILIO_FROM_NUMBER` | E.164 sender |

Docker Compose forwards these from your shell or a root `.env` file (see `docker-compose.yml` on `payment-notification-service`).

## How to test with Postman

1. Start the stack: `docker compose up --build` (or run services locally with the same env vars on **payment-notification-service**).
2. Import `postman/Online-Booking-System.postman_collection.json`.
3. **Without** provider keys: flows behave as before (simulator + notification rows only, no real email/SMS).
4. **With Stripe:** set `STRIPE_SECRET_KEY` in the environment running the payment container. Use **Create booking** with `cardToken` `4242424242424242` (server creates a test PaymentMethod) or a Stripe test **PaymentMethod id** (`pm_…`).
5. **With SendGrid:** set `SENDGRID_*`. Complete a booking → check inbox for `customer@ticketing.local` (or the JWT user’s email).
6. **With Twilio:** set `TWILIO_*`. Demo user phones are **+15555550101** (admin), **+15555550102** (customer), **+15555550103** (user2) — replace in `api-gateway` `DemoUserService` with your **verified** trial destinations if needed. Complete a booking via the gateway (JWT includes `phone`) or direct booking with **`X-User-Phone`**. SMS is sent after Rabbit consumes the booking result.
7. **Get notification logs** (`GET …/api/notifications/bookings/{bookingReference}`): `status` is **SENT** if dispatch succeeded, **FAILED** if SendGrid/Twilio threw (see stored `message` tail).

## `cardToken` behavior (payment)

| Mode | `cardToken` | Result |
|------|-------------|--------|
| Simulator (no Stripe key) | `FAIL` or ends with `0000` | Declined |
| Simulator | Other non-empty | Approved |
| Stripe | `pm_…` | Uses that PaymentMethod id as-is |
| Stripe | 12–19 digits (e.g. `4242…4242`) | Mapped to instant test PM **`pm_card_visa`** (raw PAN is **not** sent to Stripe; see Stripe 402 / raw-card API policy) |
| Stripe | `FAIL` / ends `0000` | Mapped to **`pm_card_visa_chargeDeclined`** |
| Stripe | Other non-blank | **`pm_card_visa`** (success) |

## Deploying (Azure or elsewhere)

1. **Secrets:** Store keys in **Azure Key Vault**, **Container Apps secrets**, or **App Service** application settings — never commit them.
2. **Stripe webhook URL:** must be **public HTTPS**, e.g. `https://<api-gateway>/webhooks/stripe` (gateway route is already configured) or direct to the payment service URL if you expose only that service.
3. **Outbound HTTPS:** containers must reach `api.stripe.com`, `api.sendgrid.com`, `api.twilio.com`.
4. **SendGrid / Twilio:** no inbound firewall rules required unless you add Twilio status callbacks later.
5. **Live mode:** use `sk_live_…` only with proper PCI flow (client-side Elements/Checkout); this codebase uses **server-side** test cards for demos — revise before production.

## Security note

Direct access to **payment-notification-service** port **8084** bypasses the gateway JWT for `GET /api/payments/**` and `GET /api/notifications/**`. In production, expose only the **gateway** (or a WAF) to the internet.
