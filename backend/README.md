# EchoOwl Spring Boot Backend

Spring Boot + Maven migration of the existing Next.js/Hono backend. The frontend has not been changed; this service preserves the same route names and response shapes under `/api`.

## Run Locally

Use Java 21+ and Maven:

```powershell
cd backend
mvn spring-boot:run
```

Required environment can be set in `backend/.env`, exported in your shell, or reused from the existing repo-root `.env`:

```env
DATABASE_URL=postgresql://...
CLERK_SECRET_KEY=...
DISCORD_BOT_TOKEN=...
STRIPE_SECRET_KEY=...
STRIPE_WEBHOOK_SECRET=...
NEXT_PUBLIC_APP_URL=http://localhost:3000
```

`DATABASE_URL` can be either the existing Prisma-style `postgresql://...` URL or a JDBC `jdbc:postgresql://...` URL.

## Ported Endpoints

- `GET /api/auth/getDatabaseSyncStatus`
- `GET /api/category/getEventCategories`
- `POST /api/category/createEventCategory`
- `POST /api/category/deleteCategory`
- `POST /api/category/insertQuickstartCategories`
- `GET /api/category/pollCategory`
- `GET /api/category/getEventsByCategoryName`
- `POST /api/payment/createCheckoutSession`
- `GET /api/payment/getUserPlan`
- `GET /api/project/getUsage`
- `POST /api/project/setDiscordID`
- `POST /api/events`
- `POST /api/webhooks/stripe`

## Auth Notes

The original backend used Clerk's Next.js `currentUser()` helper. This standalone backend accepts:

- `Authorization: Bearer <api-key>` for existing API-key workflows.
- `Authorization: Bearer <clerk-jwt>` for Clerk-authenticated app calls.
- `X-Clerk-User-Id` and `X-Clerk-User-Email` headers if a reverse proxy or Next middleware forwards Clerk identity.

That keeps the frontend screens unchanged while leaving a clean bridge for routing `/api/**` to this service.
