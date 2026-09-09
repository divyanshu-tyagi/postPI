# postPI

A miniature Backend-as-a-Service (BaaS) -- point it at a Postgres schema and it exposes a authenticated, policy-protected, rate-limited REST API over that schema automatically. No hand-written controllers per table.

**Built with **Kotlin + Spring Boot + PostgreSQL + Redis****

## What it does

- **Auto-generates REST endpoints** for any table via schema introspection — `GET/POST/PATCH/DELETE /api/{table}`.
- **JWT auth** for end users, plus **API keys** for server-to-server / admin access.
- **Row-level security policies** — restrict which rows a user can see or write, enforced server-side and un-bypassable by client-supplied filters.
- **Filtering, pagination, and sorting** on every generic endpoint (`?filter[col]=eq.value&limit=&offset=&order_by=`).
- **Audit logging** — every write is recorded with who did it (user or API key) and when.
- **Redis-backed rate limiting**, scoped per authenticated identity.
- **Fully containerized** — one `docker compose up` boots Postgres, Redis, and the app, with demo data already seeded.

## Architecture

```
Client
  │
  ▼
JwtAuthFilter / ApiKeyAuthFilter  ──▶  RateLimitFilter (Redis)
  │
  ▼
DataController  ──▶  DataService
                        │
                        ├─▶ SchemaIntrospector   (reads information_schema, validates table/column names)
                        ├─▶ PolicyRepository     (row-level rules: table + operation → column = current_user)
                        ├─▶ AuditLogService       (records every write)
                        └─▶ NamedParameterJdbcTemplate → PostgreSQL
```

Every request against `/api/{table}` is validated against the **live, introspected schema** before any SQL is built — table and column names are never trusted from the URL directly, only used after being confirmed to exist. Query *values* are always bound as JDBC parameters, never interpolated as strings.

## Running it

```bash
git clone divyanshu-tyagi/postPI
cd postPI
docker compose up --build
```

That's it. On first boot, Flyway runs all migrations, including a seed migration that creates a demo user, a demo project, and a few row-level policies — so you can start hitting the API immediately.

**Demo credentials:**
```
email:    demo3@postpi.dev
password: demo1234
```

## Try it

**1. Log in and get a token:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo3@postpi.dev","password":"demo1234"}'
```
Copy the `token` from the response.

**2. List your projects** (row-level policy restricts this to rows you own):
```bash
curl "http://localhost:8080/api/projects" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**3. Filter and paginate:**
```bash
curl "http://localhost:8080/api/projects?limit=5&order_by=name&filter%5Bname%5D=eq.Demo%20Project" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**4. Create, update, delete a row:**
```bash
# Create — owner_id is auto-filled from your JWT, you don't need to supply it
curl -X POST "http://localhost:8080/api/projects" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"New Project"}'

# Update (use the id returned above)
curl -X PATCH "http://localhost:8080/api/projects/PROJECT_ID" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Renamed"}'

# Delete
curl -X DELETE "http://localhost:8080/api/projects/PROJECT_ID" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**5. Generate an API key** (elevated, service-level access — bypasses row-level policies):
```bash
curl -X POST http://localhost:8080/auth/api-keys \
  -H "Content-Type: application/json" \
  -d '{"name":"my-service-key"}'
```
The `key` in the response is shown once — save it. Use it with the `X-API-Key` header instead of `Authorization: Bearer`.

**6. Check the audit trail:**
```bash
docker exec -it control-db psql -U postgres -d control_plane \
  -c "SELECT table_name, operation, principal_type, principal_id, row_id, created_at FROM audit_log ORDER BY created_at DESC LIMIT 10;"
```

## Design decisions worth knowing about

**Schema introspection as the security boundary.** Every table/column name that reaches SQL is checked against `information_schema.columns`, queried fresh per request, before being used. This is what makes it safe to accept a table name straight from a URL path.

**Row-level policies are structured, not free-form.** Rather than storing an arbitrary SQL expression (which reopens injection risk), a policy is just `(table, operation, column)` — meaning "this column must equal the current user's ID." Narrower than full Postgres RLS, but the pattern covers the vast majority of real ownership-based access rules, and can't be abused to inject arbitrary SQL.

**API keys intentionally bypass row-level policies.** They represent service/admin-level access, not a specific end user — so they read and write across all rows, subject only to schema validation. This mirrors how Supabase's service-role key works. It's real power, which is exactly why keys are hashed at rest and shown only once at creation.

**Passwords use BCrypt; API keys use SHA-256.** Passwords are low-entropy and need deliberately slow, salted hashing to resist brute force. API keys are 256 bits of random data — already unguessable — so a fast deterministic hash is both sufficient and enables direct `WHERE key_hash = ?` lookup (something BCrypt's salting makes impossible without iterating every stored key).

**Rate limiting is per-identity, not per-IP.** Each JWT user or API key gets its own Redis-backed quota (fixed-window counter, `INCR` + `EXPIRE`), so one legitimate high-traffic user can't accidentally throttle everyone behind the same NAT/IP.

**Known limitation, by design:** control-plane tables (`users`, `projects`, `table_policies`, `api_keys`, `audit_log`) currently live in the same Postgres database that gets introspected and exposed via the generic API. In a production version of this, the control-plane schema would be introspection-excluded or hosted in a fully separate database from whatever schema is being exposed as the public API — this was deferred here to keep the build loop fast.

