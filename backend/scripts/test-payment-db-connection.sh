#!/usr/bin/env bash
# One-off: test Neon / Postgres connectivity for the payment DB using backend/.env
# Requires: Docker (uses postgres:16-alpine image for psql)
# Run from anywhere:  bash backend/scripts/test-payment-db-connection.sh
# Or from backend/:   bash scripts/test-payment-db-connection.sh
#
# Password logging:
#   By default prints length + masked value (first/last chars only).
#   To print the full password (unsafe—do not commit logs): run with
#     PAYMENT_DB_PRINT_PASSWORD=1 bash scripts/test-payment-db-connection.sh

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT}/.env"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "error: missing ${ENV_FILE}"
  exit 1
fi

# Load only PAYMENT_DB_* keys so unrelated .env lines (e.g. names with spaces) are not shell-parsed.
while IFS= read -r line || [[ -n "${line}" ]]; do
  line="${line//$'\r'/}"
  [[ "${line}" =~ ^[[:space:]]*# ]] && continue
  [[ -z "${line// }" ]] && continue
  case "${line}" in
    PAYMENT_DB_URL=*)
      PAYMENT_DB_URL="${line#PAYMENT_DB_URL=}"
      PAYMENT_DB_URL="${PAYMENT_DB_URL#\"}"
      PAYMENT_DB_URL="${PAYMENT_DB_URL%\"}"
      ;;
    PAYMENT_DB_USERNAME=*)
      PAYMENT_DB_USERNAME="${line#PAYMENT_DB_USERNAME=}"
      PAYMENT_DB_USERNAME="${PAYMENT_DB_USERNAME#\"}"
      PAYMENT_DB_USERNAME="${PAYMENT_DB_USERNAME%\"}"
      ;;
    PAYMENT_DB_PASSWORD=*)
      PAYMENT_DB_PASSWORD="${line#PAYMENT_DB_PASSWORD=}"
      PAYMENT_DB_PASSWORD="${PAYMENT_DB_PASSWORD#\"}"
      PAYMENT_DB_PASSWORD="${PAYMENT_DB_PASSWORD%\"}"
      ;;
  esac
done < "${ENV_FILE}"

: "${PAYMENT_DB_URL:?PAYMENT_DB_URL is not set in .env}"
: "${PAYMENT_DB_USERNAME:?PAYMENT_DB_USERNAME is not set in .env}"
: "${PAYMENT_DB_PASSWORD:?PAYMENT_DB_PASSWORD is not set in .env}"

if [[ "${PAYMENT_DB_URL}" != jdbc:postgresql://* ]]; then
  echo "error: PAYMENT_DB_URL must start with jdbc:postgresql://"
  exit 1
fi

# jdbc:postgresql://[host[:port]]/database[?query]
rest="${PAYMENT_DB_URL#jdbc:postgresql://}"
authority="${rest%%/*}"
pathquery="${rest#*/}"

if [[ "${pathquery}" == *"?"* ]]; then
  database="${pathquery%%\?*}"
  query="${pathquery#*\?}"
else
  database="${pathquery}"
  query=""
fi

if [[ "${authority}" == *:* ]]; then
  PGHOST="${authority%%:*}"
  PGPORT="${authority##*:}"
else
  PGHOST="${authority}"
  PGPORT="5432"
fi

PGSSLMODE="require"
CHANNEL_BINDING=""

if [[ -n "${query}" ]]; then
  IFS='&' read -r -a pairs <<< "${query}"
  for pair in "${pairs[@]}"; do
    [[ -z "${pair}" ]] && continue
    key="${pair%%=*}"
    val="${pair#*=}"
    case "${key}" in
      sslmode) PGSSLMODE="${val}" ;;
      channelBinding|channel_binding) CHANNEL_BINDING="${val}" ;;
    esac
  done
fi

echo "Using .env:     ${ENV_FILE}"
echo "Host:           ${PGHOST}"
echo "Port:           ${PGPORT}"
echo "Database:       ${database}"
echo "User:           ${PAYMENT_DB_USERNAME}"

pw="${PAYMENT_DB_PASSWORD}"
pw_len=${#pw}
if (( pw_len <= 6 )); then
  pw_masked="****** (too short to mask safely)"
else
  # Avoid ${var: -N} (needs bash 4+); last 3 bytes via tail for macOS bash 3.2
  pw_head="${pw:0:3}"
  pw_tail=$(printf '%s' "${pw}" | tail -c 3)
  pw_masked="${pw_head}…${pw_tail} (masked)"
fi
echo "Password len:   ${pw_len}"
echo "Password mask:  ${pw_masked}"
if [[ "${PAYMENT_DB_PRINT_PASSWORD:-}" == "1" ]]; then
  echo "Password full:  ${pw}   <-- PAYMENT_DB_PRINT_PASSWORD=1 (remove from shell history after debugging)"
fi

echo "SSL mode:       ${PGSSLMODE}"
if [[ -n "${CHANNEL_BINDING}" ]]; then
  echo "Channel bind:   ${CHANNEL_BINDING}"
fi
echo "Running SELECT 1 via psql in Docker..."
echo

CONNINFO="host=${PGHOST} port=${PGPORT} dbname=${database} user=${PAYMENT_DB_USERNAME} sslmode=${PGSSLMODE}"
if [[ "${CHANNEL_BINDING}" == "require" ]]; then
  CONNINFO="${CONNINFO} channel_binding=require"
fi

if ! docker info >/dev/null 2>&1; then
  echo "error: Docker does not appear to be running."
  exit 1
fi

exec docker run --rm \
  -e PGPASSWORD="${PAYMENT_DB_PASSWORD}" \
  postgres:16-alpine \
  psql "${CONNINFO}" \
  -v ON_ERROR_STOP=1 \
  -c "SELECT current_database() AS db, current_user AS role, 1 AS ok;"
