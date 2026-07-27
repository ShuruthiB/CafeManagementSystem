# Cafe Management System

Full-stack cafe ordering system with three roles: **USER**, **WORKER**, **ADMIN**.

- Backend: Spring Boot 3.2 (Java 17), Spring Security + JWT, Spring Data JPA, MySQL, Redis
- Frontend: Angular 17 (standalone components), TypeScript

## What each role can do

| Feature | USER | WORKER | ADMIN |
|---|---|---|---|
| Browse menu | ✅ | ✅ | ✅ |
| Place order | ✅ | ❌ | ❌ |
| View/cancel own orders | ✅ | ❌ | ✅ (any order) |
| View live order queue | ❌ | ✅ | ✅ |
| Accept order / update status | ❌ | ✅ | ✅ |
| Update stock quantity | ❌ | ✅ | ✅ |
| Create/edit/delete menu items | ❌ | ❌ | ✅ |
| Create WORKER/ADMIN accounts | ❌ | ❌ | ✅ |
| Enable/disable users | ❌ | ❌ | ✅ |
| View all orders (reports) | ❌ | ❌ | ✅ |

## Project structure

```
cafe-management-system/
├── backend/     Spring Boot API (port 8080)
└── frontend/    Angular app (port 4200)
```

## 1. Backend setup

**Prerequisites:** Java 17, Maven, MySQL running locally, Redis running locally (optional if you comment out the Redis dependency usage — it's wired in for future caching but not required for the app to function today).

1. Create the database (or let it auto-create — see `application.properties`):
   ```sql
   CREATE DATABASE cafe_db;
   ```

2. Edit `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   ```
   Also replace `app.jwt.secret` with your own generated 256-bit base64 secret before deploying anywhere real.

3. Run it:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. On first run, a default admin account is seeded automatically:
   ```
   email: admin@cafe.com
   password: Admin@123
   ```
   **Change this password immediately** — the seeder is for local dev convenience only, not production.

5. Test with Postman: import the endpoints below, hit `/api/auth/login`, copy the `token` from the response, and set it as a Bearer token on subsequent requests.

### Key API endpoints

| Method | Endpoint | Who |
|---|---|---|
| POST | `/api/auth/register` | Public (always creates USER) |
| POST | `/api/auth/login` | Public |
| GET | `/api/menu` | Public |
| GET | `/api/menu/staff-view` | WORKER, ADMIN |
| POST/PUT/DELETE | `/api/menu/**` | ADMIN (PUT also allowed for WORKER on `/api/worker/menu/{id}/stock`) |
| POST | `/api/orders` | USER |
| GET | `/api/orders/my` | USER |
| PUT | `/api/orders/{id}/cancel` | USER, ADMIN |
| GET | `/api/worker/orders/queue` | WORKER, ADMIN |
| PUT | `/api/worker/orders/{id}/accept` | WORKER, ADMIN |
| PUT | `/api/worker/orders/{id}/status` | WORKER, ADMIN |
| PUT | `/api/worker/menu/{id}/stock` | WORKER, ADMIN |
| POST | `/api/admin/staff` | ADMIN — create WORKER/ADMIN account |
| GET | `/api/admin/users` | ADMIN |
| GET | `/api/admin/orders` | ADMIN — full order history |

## 2. Frontend setup

**Prerequisites:** Node.js 18+, npm.

```bash
cd frontend
npm install
npm start
```

App runs at `http://localhost:4200` and expects the backend at `http://localhost:8080` (see the `BASE_URL` constants in `src/app/services/*.ts` — change these if you deploy the backend elsewhere).

## 3. Try the full flow

1. Start backend, start frontend.
2. Log in as `admin@cafe.com` / `Admin@123` → go to **Menu editor**, add a few menu items.
3. Go to **Staff** → create a WORKER account.
4. Log out, register a new account (becomes USER) → browse menu, add items to cart, place an order.
5. Log out, log in as the WORKER you created → go to **Queue** → accept the order, move it through Preparing → Ready → Completed.
6. Log back in as USER → check **My orders** to see the status update.

## Design notes worth knowing for interviews

- **JWT carries the role claim**, so authorization checks don't need a DB hit per request.
- **Pessimistic locking** (`findByIdForUpdate`) on the `Order` entity prevents two workers from accepting the same order simultaneously — same pattern as your producer-consumer BlockingQueue work.
- **Optimistic locking** (`@Version`) on `MenuItem` and `Order` guards against lost updates during concurrent stock changes.
- **CORS** is scoped to `http://localhost:4200` only — update this list before deploying the frontend elsewhere.
- **Route guards** on the frontend hide UI the user shouldn't see, but the real security boundary is the backend's `@PreAuthorize` / `SecurityConfig` rules — never trust the frontend alone.
- Public registration always creates a `USER`; WORKER/ADMIN accounts can only be created by an existing admin through `/api/admin/staff`. This mirrors how real POS/staff systems prevent privilege escalation via self-signup.

## Known gaps to extend later (good talking points / next steps)

- No refresh token — JWT just expires after `app.jwt.expiration-ms` (currently 24h) and the user has to log in again.
- No WebSocket push yet — the worker queue and "my orders" screens use manual/polling refresh rather than real-time updates. Wiring in STOMP over WebSocket would let order status changes push to the user's screen live.
- No pagination on `/api/admin/orders` or `/api/menu` — fine for a demo, but add `Pageable` before using with large datasets.
- Flyway/Liquibase migrations aren't set up — `ddl-auto=update` is used for simplicity, which is fine for dev but not how real teams manage schema changes in production.
