# mauri-pay-backend

Spring Boot monolithic payment backend for the `mauri_pay` app. Implements a **merchant
"pay-by-code" flow** with **webhook confirmation**:

1. A **merchant** (API-key auth) calls `POST /api/v1/payments` with a **fixed amount** → gets a **transaction code**.
2. A **logged-in user** (session auth) submits the code → backend debits their **internal balance** by the **stored, uneditable amount** and marks it `PAID`.
3. The backend **calls the merchant back** via a signed **webhook**.

## Stack

Java 21+, Spring Boot 3.4, Spring Security (session / `JSESSIONID` cookie), Spring Data JPA,
PostgreSQL, Flyway, Spring Session JDBC. All money is `NUMERIC`/`BigDecimal`.

## Run locally

```bash
docker compose up -d          # PostgreSQL on :5432
./mvnw spring-boot:run        # app on :8080, Flyway applies V1
```

On startup (with `app.dev.seed=true`, the default) a **Demo Merchant** is seeded and its
API key is printed to the log. Default key: `dev-merchant-key-please-change`.

## API

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/api/v1/auth/register` | public | create user + zeroed account |
| POST | `/api/v1/auth/login` | public | sets `JSESSIONID` cookie |
| POST | `/api/v1/auth/logout` | session | invalidate session |
| GET  | `/api/v1/me` | session | current user + balance |
| POST | `/api/v1/payments` | `X-Api-Key` | merchant creates code (fixed amount) |
| GET  | `/api/v1/payments/{code}/status` | `X-Api-Key` | merchant polls status |
| GET  | `/api/v1/payments/{code}` | session | user previews uneditable amount |
| POST | `/api/v1/payments/{code}/pay` | session | user pays — **no amount in body** |
| POST | `/api/v1/dev/topup` | session (dev only) | fund the current account |

## End-to-end with curl

```bash
KEY=dev-merchant-key-please-change
# register + login (cookie jar)
curl -sc cj.txt -X POST localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"phone":"22200001","fullName":"Alice","password":"secret123"}'
curl -sb cj.txt -c cj.txt -X POST localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"phone":"22200001","password":"secret123"}'
# fund + create a code
curl -sb cj.txt -X POST localhost:8080/api/v1/dev/topup -H 'Content-Type: application/json' -d '{"amount":500}'
CODE=$(curl -s -X POST localhost:8080/api/v1/payments -H "X-Api-Key: $KEY" \
  -H 'Content-Type: application/json' \
  -d '{"amount":120,"currency":"MRU","callbackUrl":"https://webhook.site/your-id"}' | jq -r .code)
# preview + pay
curl -sb cj.txt localhost:8080/api/v1/payments/$CODE
curl -sb cj.txt -X POST localhost:8080/api/v1/payments/$CODE/pay
```

## Webhook

On payment the backend POSTs to the merchant `callbackUrl`:

```
POST <callbackUrl>
Content-Type: application/json
X-MauriPay-Signature: <hex HMAC-SHA256 of the body, keyed with the merchant webhook secret>

{ "code", "status", "amount", "currency", "orderRef", "paidAt" }
```

Failed deliveries are retried with exponential backoff (`app.webhook.max-attempts`,
`app.webhook.retry-base-seconds`) by a scheduled job.

## Tests

```bash
./mvnw verify        # PaymentFlowIT runs the full flow against a Testcontainers Postgres (needs Docker)
```

## Production notes

- Set `APP_DEV_SEED=false` to disable the demo merchant + dev top-up endpoint.
- Generate real merchant API keys with a CSPRNG; only the SHA-256 **hash** is stored.
- Serve over HTTPS so the session cookie and API keys are protected in transit.
