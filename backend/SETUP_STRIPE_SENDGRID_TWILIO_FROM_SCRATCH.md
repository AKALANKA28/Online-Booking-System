# Setup from scratch: Stripe (test), SendGrid (email), Twilio (SMS)

This guide assumes you have **never** used Stripe, SendGrid, or Twilio. Follow the parts you need; you can test **Stripe only**, **email only**, **SMS only**, or all three.

---

## 0. What you need on your computer

| Tool | Why |
|------|-----|
| **Docker Desktop** (or Docker Engine + Compose) | Runs the whole platform (`docker compose up`). |
| **Postman** (recommended) | Import `postman/Online-Booking-System.postman_collection.json` to call APIs easily. |
| **Web browser** | Sign up for Stripe, SendGrid, Twilio; check email. |

You do **not** need Java or Maven installed if you only use Docker.

---

## 1. What the app does (one minute)

1. You **log in** on the **API Gateway** → you get a **JWT** (includes **email** and **phone** for demo users).
2. You **create a booking** → **booking-service** calls **payment-notification-service** to charge (real **Stripe** if `STRIPE_SECRET_KEY` is set, otherwise the built-in **simulator**).
3. When the booking finishes, a message goes through **RabbitMQ** to the payment service, which:
   - writes a **notification log** in the database;
   - if **SendGrid** is configured → sends an **email** to the user’s email;
   - if **Twilio** is configured → sends an **SMS** to the user’s **phone** (from JWT / demo user).

All secrets are read by the **payment-notification-service** container from **environment variables** (we’ll use a `.env` file next to `docker-compose.yml`).

---

## Part A — Stripe (test mode, no real money)

### A.1 Create a Stripe account

1. Open [https://dashboard.stripe.com/register](https://dashboard.stripe.com/register).
2. Complete sign-up (email, password, business details as prompted).
3. Stay in **Test mode** (toggle in the Stripe Dashboard should say **Test**; test mode is free for development).

### A.2 Get your test secret key

1. In Stripe Dashboard: **Developers** (top right) → **API keys**.
2. Under **Standard keys**, find **Secret key** — click **Reveal test key**.
3. Copy the value. It starts with **`sk_test_`**.  
   - Treat it like a password: **do not** commit it to Git or paste it in public chats.

### A.3 (Optional) Webhooks for local testing

Webhooks let Stripe notify your app when a payment changes. **You can skip this** on the first run; payments still work **synchronously** when you create a booking.

If you want webhooks:

1. Install [Stripe CLI](https://stripe.com/docs/stripe-cli) (e.g. macOS: `brew install stripe/stripe-cli/stripe`).
2. In a terminal: `stripe login` (browser will confirm).
3. With Docker stack **running** and gateway on port **8080**, run:
   ```bash
   stripe listen --forward-to http://localhost:8080/webhooks/stripe
   ```
4. The CLI prints a **webhook signing secret** starting with **`whsec_`**. Copy it — this is your **`STRIPE_WEBHOOK_SECRET`** for local use only.

---

## Part B — SendGrid (email)

SendGrid is Twilio’s email product. You send mail **from** an address you verify.

### B.1 Create a SendGrid account

1. Go to [https://sendgrid.com](https://sendgrid.com) and sign up (free tier is enough for testing).

### B.2 Verify a sender email (required)

1. In SendGrid: **Settings** → **Sender Authentication**.
2. Choose **Verify a Single Sender** (simplest for demos).
3. Add an email you can open (e.g. your Gmail), complete the form, submit.
4. **Open that inbox** and click the **verification link** SendGrid sends.
5. After verification, note the exact **From email** you verified — this becomes **`SENDGRID_FROM_EMAIL`**.

### B.3 Create an API key

1. **Settings** → **API Keys** → **Create API Key**.
2. Name it (e.g. `ticketing-local`), choose **Full Access** *or* **Restricted Access** with **Mail Send** enabled.
3. Click **Create & View** and copy the key once (starts with **`SG.`**). You cannot see it again.

---

## Part C — Twilio (SMS)

### C.1 Create a Twilio account

1. Go to [https://www.twilio.com/try-twilio](https://www.twilio.com/try-twilio) and sign up.

### C.2 Get Account SID and Auth Token

1. Open [Twilio Console](https://console.twilio.com/).
2. On the **Account Info** panel copy:
   - **Account SID** (starts with `AC`)
   - **Auth Token** — click to reveal and copy

### C.3 Get a “From” phone number

1. In Console: **Phone Numbers** → **Manage** → **Buy a number** (trial accounts usually get trial credit).
2. Pick a number with **SMS** capability.
3. Copy it in **E.164** format, e.g. **`+15551234567`** — this is **`TWILIO_FROM_NUMBER`**.

### C.4 Trial account: verify the *destination* number (important)

On a **free trial**, Twilio only sends SMS to **verified** numbers.

1. Console → **Phone Numbers** → **Manage** → **Verified Caller IDs** (or **Twilio trial** onboarding may guide you).
2. **Add** your own mobile number and complete verification (SMS or call).
3. You must use **that same number** as the **customer’s phone** in the app, or SMS will fail.

**Match the app to your verified number**

Demo users have fixed phones in code (`+15555550102` for **customer**, etc.). For Twilio trial:

1. Open `api-gateway/src/main/java/com/ticketing/apigateway/service/DemoUserService.java`.
2. Change the **customer** (and optionally **admin** / **user2**) **`phone`** field to your **verified E.164** number (e.g. `+94771234567`).
3. Rebuild: `docker compose up --build` (so the gateway image includes the change).

Alternatively, run the gateway from your IDE with the edited code — the point is: **JWT phone must equal a Twilio-verified destination** on trial.

---

## Part D — Put everything in a `.env` file

1. Go to the **repository root** (the folder that contains **`docker-compose.yml`**).
2. Create a new file named **`.env`** (leading dot, no name before it).
3. Paste and fill in **your** values:

```env
# --- Stripe (Part A) — enables real test-mode PaymentIntents ---
STRIPE_SECRET_KEY=sk_test_paste_yours_here

# Optional — only if you ran `stripe listen` (Part A.3)
STRIPE_WEBHOOK_SECRET=whsec_paste_yours_here

# Currency for Stripe (default in app is usd)
STRIPE_CURRENCY=usd

# --- SendGrid (Part B) ---
SENDGRID_API_KEY=SG.paste_yours_here
SENDGRID_FROM_EMAIL=the_verified_sender@example.com
SENDGRID_FROM_NAME=Ticketing Demo

# --- Twilio (Part C) ---
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=paste_yours_here
TWILIO_FROM_NUMBER=+1xxxxxxxxxx
```

4. Save the file.
5. **Confirm `.env` is listed in `.gitignore`** so you never commit secrets (this repo should already ignore `.env`).

Docker Compose is already wired to pass these variables into **payment-notification-service** — see `docker-compose.yml`.

---

## Part E — Start the platform

1. Open a terminal in the repo root.
2. Run:
   ```bash
   docker compose up --build
   ```
3. Wait until containers are up (first time can take several minutes).
4. Optional: in another terminal, if using Stripe webhooks:
   ```bash
   stripe listen --forward-to http://localhost:8080/webhooks/stripe
   ```

---

## Part F — Test with Postman (recommended)

1. Open **Postman** → **Import** → select **`postman/Online-Booking-System.postman_collection.json`**.
2. Collection variables like **`gatewayBaseUrl`** should be `http://localhost:8080` (default).

### F.1 Create an event (admin)

1. **1 - API Gateway** → **Auth** → **Login (Admin)** → **Send**.
2. **Events** → **Create event (ADMIN)** → **Send** (uses `{{adminToken}}`).

### F.2 Book as customer (triggers Stripe + email + SMS)

1. **Auth** → **Login (Customer)** → **Send** (refreshes token and **`customerPhone`** if API returns it).
2. **Seats** → **List seats for event** → pick **available** seat IDs (e.g. `A1`, `A2`).
3. **Bookings** → **Create booking (CUSTOMER)** → in the JSON body set **`seatNumbers`** to seats that are **AVAILABLE**.  
   - Use **`"cardToken": "4242424242424242"`** (or any non-decline token) for **success** with Stripe: the server maps it to Stripe’s instant test PM **`pm_card_visa`** — it does **not** send the card number to Stripe (avoids **402** “raw card data” errors).
4. **Send**.

### F.3 Check that each integration worked

| Integration | What to check |
|-------------|----------------|
| **Stripe** | [Stripe Dashboard](https://dashboard.stripe.com/test/payments) → **Payments** (test mode) — you should see a **PaymentIntent** / payment for the booking amount. |
| **SendGrid** | SendGrid → **Activity** (or **Email API** logs) — outbound email to **`customer@ticketing.local`** (demo customer email). Also check **spam** folder. |
| **Twilio** | Twilio → **Monitor** → **Logs** → **Messaging** — look for **delivered** or an error. Your **phone** (verified + matching `DemoUserService`) should receive SMS. |
| **App DB / API** | **Payments & notifications** → **Get notification logs by booking** — `status` **`SENT`** means dispatch succeeded; **`FAILED`** includes an error snippet in the stored message. |

### F.4 If you only enabled some providers

- **Only `STRIPE_SECRET_KEY`**: payments use Stripe; email/SMS stay off (notification row may still be **SENT** with no external send — see `NotificationApplicationService` logic: external send only when keys are set).
- **Only SendGrid**: email sends; SMS skipped if Twilio not set.
- **Only Twilio**: SMS sends if **phone** is present and valid; email skipped if SendGrid not set.

---

## Part G — Test with `curl` (no Postman)

Replace tokens and IDs from your responses.

```bash
# 1) Admin login
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r .accessToken)

# 2) Create event
curl -s -X POST http://localhost:8080/api/events \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Test Event","description":"Demo","venue":"Hall","startsAt":"2026-08-01T18:00:00Z","endsAt":"2026-08-01T22:00:00Z","totalRows":4,"seatsPerRow":6,"vipRows":1,"vipPrice":100,"regularPrice":50}'

# 3) Customer login (JWT includes phone for demo users)
CUSTOMER_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"customer","password":"customer123"}' | jq -r .accessToken)

# 4) Booking (adjust eventId / seats from list seats response)
curl -s -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"eventId":1,"seatNumbers":["A1"],"paymentMethod":"CARD","cardToken":"4242424242424242"}'
```

---

## Troubleshooting (very common issues)

| Symptom | Likely cause | What to do |
|---------|----------------|------------|
| **401** on `/api/bookings` | Old JWT without `phone` claim | **Log in again** after code changes; use a fresh token. |
| **SMS failed** / Twilio error 21608 / similar | Trial + unverified destination | Verify your mobile in Twilio; set **same** E.164 in **`DemoUserService`** for **customer** phone; rebuild gateway. |
| **SendGrid 403** / email not sent | Unverified sender | Finish **Single Sender Verification**; `SENDGRID_FROM_EMAIL` must match exactly. |
| **Stripe error** / payment declined | Wrong key or live key in test | Use **`sk_test_`** only in test mode. |
| **Webhook** endpoint **404** | No `STRIPE_WEBHOOK_SECRET` | Normal if unset — skip webhooks for first tests, or set `whsec_` from `stripe listen`. |
| **Seat already reserved** | Re-running same seats | **List seats** again; pick **AVAILABLE** codes. |

---

## Quick checklist

- [ ] `.env` in repo root with keys filled (never committed).
- [ ] SendGrid **sender verified**; API key created.
- [ ] Twilio **From** number + **verified** destination if on trial; **`DemoUserService`** phone matches destination.
- [ ] `docker compose up --build` successful.
- [ ] Postman: **Login (Customer)** then **Create booking** with test card `4242424242424242`.
- [ ] Stripe Dashboard (test) shows payment; SendGrid/Twilio logs show delivery (or fix using table above).

For shorter reference (env var table, `cardToken` rules), see **[INTEGRATIONS.md](./INTEGRATIONS.md)**.
