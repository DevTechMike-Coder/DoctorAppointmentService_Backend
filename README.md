# Doctor Appointment Booking System — Backend

Spring Boot backend for a doctor appointment booking system. Patients register, browse doctors, book available time slots, and manage their appointments. Doctors manage their profile and availability.

## Tech Stack

- **Java 26**, Spring Boot 4.1.0
- **Spring Data JPA** + PostgreSQL (hosted on Neon)
- **Flyway** for schema migrations
- **Spring Security** + JWT (`jjwt` 0.12.6) for stateless auth
- **Lombok** for boilerplate reduction
- **Bucket4j** (`bucket4j_jdk17-core`) for rate limiting
- **Testcontainers** + JUnit for integration testing

## Architecture

```
entity/       JPA entities — User, DoctorProfile, AvailabilitySlot, Appointment
repository/   Spring Data JPA interfaces
dto/          Request/response records
security/     JWT auth (JwtService, JwtAuthFilter, SecurityConfig) + IDOR guards
              (AppointmentSecurity, AvailabilitySecurity)
service/      Business logic, transaction boundaries
controller/   REST endpoints
exception/    Custom exceptions + @RestControllerAdvice global handler
```

### Key design decisions

- **Slot-based booking, not recurring patterns.** `AvailabilitySlot` rows represent concrete bookable instants (not a weekly recurrence rule). A doctor (or admin) creates slots directly; patients book against a `slotId`.
- **Double-booking prevented two ways:**
    - `@Version` optimistic locking on `AvailabilitySlot`
    - `AvailabilitySlotRepository.findByIdForUpdate()` — `PESSIMISTIC_WRITE` row lock, used in `AppointmentService.bookAppointment()`
    - `Appointment.slot` is `@OneToOne` with a unique `slot_id` — a slot can back at most one appointment at the schema level
- **IDOR protection** via method security: `AppointmentSecurity.isOwner()` and `AvailabilitySecurity.isOwnerOfDoctorProfile/isOwnerOfSlot()`, wired through `@PreAuthorize("@appointmentSecurity.isOwner(#id, authentication)")` on controller endpoints. Never trust a `patientId`/`doctorId` from a request body — always resolve from the authenticated principal.
- **DTOs are records**, not classes — immutable, no Lombok needed on them.

## Environment Variables

Never commit secrets. Set these via your run config, shell export, or a gitignored `.env`:

| Variable | Description |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host>/<dbname>?sslmode=require` (Neon requires `sslmode=require`) |
| `DB_USERNAME` | Postgres role username |
| `DB_PASSWORD` | Postgres role password |
| `JWT_SECRET` | 256-bit random secret, base64 or hex. Generate with `openssl rand -base64 32` (or PowerShell `RandomNumberGenerator` if `openssl` isn't on PATH) |

`application.yml` reads these with no fallback for secrets — the app fails to boot rather than silently running with a missing/default secret. This is intentional.

## Local Setup

1. Provision a Postgres instance — recommended: [Neon](https://neon.tech) (via `neonctl` CLI or the Vercel Marketplace integration if you already have a Vercel project).
2. Set the four env vars above.
3. Flyway migrations run automatically on startup (`src/main/resources/db/migration/`).
4. `mvn spring-boot:run`

## Deployment Topology

This is a traditional long-running Spring Boot process (JVM, connection pooling, Flyway on boot) — **it does not run on Vercel's serverless model.** If pairing with a live frontend:

```
Frontend (React/Next)  → Vercel
Backend  (Spring Boot) → Railway / Render / Fly.io   (NOT Vercel)
Database (Postgres)    → Neon (via Vercel integration, or standalone via neonctl)
```

**CORS must be configured** before a deployed frontend can reach this API — see `SecurityConfig` for where to add `CorsConfigurationSource` once the frontend origin is known. Point the frontend's API base URL at wherever the backend deploys (e.g. Railway's generated domain), not `localhost:8080`.

## Known Issues / Review Notes

Tracking items from the last code review — check these off as they're fixed:

- [ ] **Password max length bug** — `RegisterRequest.password` is currently `@Size(min = 6, max = 8)`. The `max = 8` cap is a security anti-pattern (rejects strong passphrases). Fix: `@Size(min = 8, max = 72)` (72 = BCrypt's byte limit).
- [ ] **Unmapped exceptions return 500 instead of proper status codes.** `AuthService.register()` throws `IllegalArgumentException` (duplicate email / invalid role); `AvailabilityService.deleteSlot()` throws `IllegalStateException`. Neither has a `GlobalExceptionHandler` mapping yet — add handlers returning 409 Conflict.
- [ ] **`JwtAuthFilter` doesn't catch `UsernameNotFoundException`.** A token whose subject email no longer exists (deleted user, stale token) currently causes an uncaught 500 instead of falling through as unauthenticated.
- [ ] **No Flyway migrations committed yet.** Schema needs `V1__init_schema.sql` matching current entities before this will actually boot against a fresh database.
- [ ] **No `spring.datasource.*` / `app.jwt.*` values in `application.properties` beyond the property keys** — confirm env vars are wired in every environment (local, CI, deployed) before assuming this boots cleanly.
- [ ] **No CORS configuration** — required before any deployed frontend (Vercel) can call this API cross-origin.

## API Overview

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login

GET    /api/v1/doctors
GET    /api/v1/doctors/{id}
GET    /api/v1/doctors/search?specialty=
PUT    /api/v1/doctors/profile                    (DOCTOR only)

GET    /api/v1/doctors/{doctorId}/slots?from=&to=
POST   /api/v1/doctors/{doctorId}/slots            (DOCTOR owner / ADMIN)
DELETE /api/v1/doctors/{doctorId}/slots/{slotId}   (DOCTOR owner / ADMIN)

POST   /api/v1/appointments                        (PATIENT)
GET    /api/v1/appointments/{id}                   (owner / ADMIN)
GET    /api/v1/appointments/me                     (PATIENT)
GET    /api/v1/appointments/doctor/{doctorId}      (DOCTOR / ADMIN)
PATCH  /api/v1/appointments/{id}/status             (DOCTOR / ADMIN)
DELETE /api/v1/appointments/{id}                    (owner / ADMIN)
```