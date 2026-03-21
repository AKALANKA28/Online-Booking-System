#!/usr/bin/env bash
set -euo pipefail

BASE_URL=${BASE_URL:-http://localhost:8080}

ADMIN_TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

echo "Admin token acquired"

CREATE_EVENT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/events" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "title":"Colombo Tech Expo",
    "description":"Annual technology showcase",
    "venue":"BMICH",
    "startsAt":"2026-09-20T10:00:00Z",
    "endsAt":"2026-09-20T18:00:00Z",
    "totalRows":6,
    "seatsPerRow":8,
    "vipRows":2,
    "vipPrice":100.00,
    "regularPrice":50.00
  }')

echo "Created event: $CREATE_EVENT_RESPONSE"

sleep 2

echo "Listing generated seats for event 1"
curl -s "$BASE_URL/api/seats/events/1"
echo

CUSTOMER_TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"customer","password":"customer123"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

echo "Customer token acquired"

BOOKING_RESPONSE=$(curl -s -X POST "$BASE_URL/api/bookings" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId":1,
    "seatNumbers":["A1","A2"],
    "paymentMethod":"CARD",
    "cardToken":"4242424242424242"
  }')

echo "Booking response: $BOOKING_RESPONSE"
